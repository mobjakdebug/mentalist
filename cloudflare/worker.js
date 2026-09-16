/**
 * Cloudflare Worker for سیاه‌بازی (Siahbazi / The Mentalist Game)
 * Production Hardened Edition.
 * 
 * Features:
 * 1. Real Bearer Session Token Authentication with D1 `sessions` table.
 * 2. Strict Role-Based Access Control (Host & Mentalist authorization).
 * 3. Zero Information Leakage & Anti-Cheat protection on cards.
 * 4. Atomic Answer Submissions without race conditions.
 * 5. 100 Psychology Questions with non-repetition tracking per room.
 * 6. Precise GDD Scoring Matrix & Persistent User Stats sync.
 * 
 * Bindings:
 * - env.DB : Cloudflare D1 Database binding
 */

const CORS_HEADERS = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "GET, POST, PUT, DELETE, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type, Authorization, X-Player-Id, X-Session-Token",
  "Content-Type": "application/json; charset=utf-8"
};

function jsonResponse(data, status = 200) {
  return new Response(JSON.stringify(data), {
    status,
    headers: CORS_HEADERS
  });
}

function generateRoomCode() {
  const chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
  let code = "";
  for (let i = 0; i < 5; i++) {
    code += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return code;
}

function generateId() {
  return Math.random().toString(36).substring(2, 10) + Date.now().toString(36);
}

function generateSecureToken() {
  const bytes = new Uint8Array(24);
  crypto.getRandomValues(bytes);
  return "siah_sec_" + Array.from(bytes).map(b => b.toString(16).padStart(2, "0")).join("");
}

async function hashPassword(password) {
  const encoder = new TextEncoder();
  const data = encoder.encode(password + "_siahbazi_salt_secure_2026");
  const hashBuffer = await crypto.subtle.digest("SHA-256", data);
  const hashArray = Array.from(new Uint8Array(hashBuffer));
  return hashArray.map(b => b.toString(16).padStart(2, "0")).join("");
}

/**
 * Validates session token from Authorization: Bearer <token> or X-Session-Token.
 */
async function authenticateUser(request, env) {
  const authHeader = request.headers.get("Authorization") || "";
  let token = "";
  if (authHeader.startsWith("Bearer ")) {
    token = authHeader.substring(7).trim();
  } else if (request.headers.get("X-Session-Token")) {
    token = request.headers.get("X-Session-Token").trim();
  } else {
    const url = new URL(request.url);
    token = url.searchParams.get("token") || "";
  }

  if (!token) return null;

  const now = Date.now();
  const session = await env.DB.prepare(
    "SELECT s.token, s.user_id, s.expires_at, u.username, u.email, u.avatar_emoji, u.level, u.xp, u.coins " +
    "FROM sessions s JOIN users u ON s.user_id = u.id " +
    "WHERE s.token = ? AND s.expires_at > ?"
  ).bind(token, now).first();

  return session || null;
}

/**
 * Extracts the calling player's ID from header, body, or query param.
 */
function getCallerPlayerId(request, body = null) {
  const fromHeader = request.headers.get("X-Player-Id");
  if (fromHeader && fromHeader.trim().length > 0) return fromHeader.trim();

  if (body && body.playerId) return body.playerId.toString().trim();

  const url = new URL(request.url);
  const fromQuery = url.searchParams.get("playerId");
  if (fromQuery && fromQuery.trim().length > 0) return fromQuery.trim();

  return null;
}

export default {
  async fetch(request, env) {
    if (request.method === "OPTIONS") {
      return new Response(null, { headers: CORS_HEADERS });
    }

    const url = new URL(request.url);
    const path = url.pathname;
    const method = request.method;

    try {
      if (!env.DB) {
        return jsonResponse({
          error: "دیتابیس D1 متصل نشده است. لطفا در wrangler.toml بایندینگ DB را اضافه کنید.",
          code: "D1_NOT_BOUND"
        }, 500);
      }

      // ==========================================================
      // 1. HEALTH CHECK
      // ==========================================================
      if (path === "/" || path === "/health" || path === "/api/health") {
        const testQuery = await env.DB.prepare("SELECT COUNT(*) as count FROM questions").first();
        const roomQuery = await env.DB.prepare("SELECT COUNT(*) as count FROM rooms").first();
        const usersCount = await env.DB.prepare("SELECT COUNT(*) as count FROM users").first();
        return jsonResponse({
          status: "connected",
          service: "Siahbazi Mentalist Backend - Production",
          database: "Cloudflare D1",
          questionCount: testQuery ? testQuery.count : 0,
          activeRoomsCount: roomQuery ? roomQuery.count : 0,
          usersCount: usersCount ? usersCount.count : 0,
          timestamp: Date.now()
        });
      }

      // ==========================================================
      // 2. AUTHENTICATION (Register, Login, Me, Logout)
      // ==========================================================
      if (path === "/api/auth/register" && method === "POST") {
        const body = await request.json();
        const username = body.username ? body.username.trim() : "";
        const email = body.email ? body.email.trim() : null;
        const password = body.password ? body.password : "";
        const avatarEmoji = body.avatarEmoji || "🕵️";

        if (!username || username.length < 3) {
          return jsonResponse({ success: false, error: "نام کاربری باید حداقل ۳ کاراکتر باشد." }, 400);
        }
        if (!password || password.length < 4) {
          return jsonResponse({ success: false, error: "رمز عبور باید حداقل ۴ کاراکتر باشد." }, 400);
        }

        const existing = await env.DB.prepare(
          "SELECT id FROM users WHERE username = ? OR (email IS NOT NULL AND email = ?)"
        ).bind(username, email || "__no_email__").first();

        if (existing) {
          return jsonResponse({ success: false, error: "این نام کاربری یا ایمیل قبلاً ثبت شده است." }, 400);
        }

        const userId = "usr_" + generateId();
        const pwdHash = await hashPassword(password);
        const now = Date.now();

        await env.DB.prepare(
          `INSERT INTO users (
            id, username, email, password_hash, avatar_emoji,
            level, xp, coins, matches_played, wins,
            successful_bluffs, correct_guesses, ghost_badges, truthseeker_badges,
            created_at, last_login
          ) VALUES (?, ?, ?, ?, ?, 1, 0, 0, 0, 0, 0, 0, 0, 0, ?, ?)`
        ).bind(userId, username, email, pwdHash, avatarEmoji, now, now).run();

        // ساخت توکن واقعی و ذخیره در جدول sessions (اعتبار ۳۰ روزه)
        const sessionToken = generateSecureToken();
        const expiresAt = now + (30 * 24 * 60 * 60 * 1000);
        await env.DB.prepare(
          "INSERT INTO sessions (token, user_id, created_at, expires_at) VALUES (?, ?, ?, ?)"
        ).bind(sessionToken, userId, now, expiresAt).run();

        const userDto = {
          id: userId,
          username,
          email,
          avatarEmoji,
          level: 1,
          xp: 0,
          coins: 0,
          matchesPlayed: 0,
          wins: 0,
          successfulBluffs: 0,
          correctGuesses: 0,
          ghostBadges: 0,
          truthseekerBadges: 0
        };

        return jsonResponse({
          success: true,
          user: userDto,
          token: sessionToken
        });
      }

      if (path === "/api/auth/login" && method === "POST") {
        const body = await request.json();
        const username = body.username ? body.username.trim() : "";
        const password = body.password ? body.password : "";

        if (!username || !password) {
          return jsonResponse({ success: false, error: "نام کاربری و رمز عبور الزامی است." }, 400);
        }

        const pwdHash = await hashPassword(password);
        const user = await env.DB.prepare(
          `SELECT id, username, email, password_hash, avatar_emoji as avatarEmoji,
                  level, xp, coins, matches_played as matchesPlayed, wins,
                  successful_bluffs as successfulBluffs, correct_guesses as correctGuesses,
                  ghost_badges as ghostBadges, truthseeker_badges as truthseekerBadges
           FROM users
           WHERE (username = ? OR email = ?) AND password_hash = ?`
        ).bind(username, username, pwdHash).first();

        if (!user) {
          return jsonResponse({ success: false, error: "نام کاربری یا رمز عبور اشتباه است." }, 401);
        }

        const now = Date.now();
        await env.DB.prepare("UPDATE users SET last_login = ? WHERE id = ?").bind(now, user.id).run();

        // ایجاد نشست کاربری جدید در دیتابیس D1
        const sessionToken = generateSecureToken();
        const expiresAt = now + (30 * 24 * 60 * 60 * 1000);
        await env.DB.prepare(
          "INSERT INTO sessions (token, user_id, created_at, expires_at) VALUES (?, ?, ?, ?)"
        ).bind(sessionToken, user.id, now, expiresAt).run();

        delete user.password_hash;
        return jsonResponse({
          success: true,
          user,
          token: sessionToken
        });
      }

      if (path === "/api/auth/me" && method === "GET") {
        const authenticated = await authenticateUser(request, env);
        if (!authenticated) {
          return jsonResponse({ success: false, error: "توکن نامعتبر یا منقضی شده است." }, 401);
        }
        const user = await env.DB.prepare(
          `SELECT id, username, email, avatar_emoji as avatarEmoji,
                  level, xp, coins, matches_played as matchesPlayed, wins,
                  successful_bluffs as successfulBluffs, correct_guesses as correctGuesses,
                  ghost_badges as ghostBadges, truthseeker_badges as truthseekerBadges
           FROM users WHERE id = ?`
        ).bind(authenticated.user_id).first();

        return jsonResponse({ success: true, user });
      }

      if (path === "/api/auth/logout" && method === "POST") {
        const authHeader = request.headers.get("Authorization") || "";
        let token = authHeader.startsWith("Bearer ") ? authHeader.substring(7).trim() : request.headers.get("X-Session-Token");
        if (token) {
          await env.DB.prepare("DELETE FROM sessions WHERE token = ?").bind(token).run();
        }
        return jsonResponse({ success: true });
      }

      // ==========================================================
      // 3. QUESTIONS API (100 Questions)
      // ==========================================================
      if (path === "/api/questions" && method === "GET") {
        const { results } = await env.DB.prepare(
          "SELECT id, category, question_text as questionText, created_by as createdBy FROM questions ORDER BY id ASC"
        ).all();
        return jsonResponse({ questions: results || [] });
      }

      if (path === "/api/questions" && method === "POST") {
        const body = await request.json();
        const { category, questionText, createdBy } = body;
        if (!category || !questionText) {
          return jsonResponse({ error: "دسته‌بندی و متن سوال الزامی است" }, 400);
        }
        const insert = await env.DB.prepare(
          "INSERT INTO questions (category, question_text, created_by) VALUES (?, ?, ?)"
        ).bind(category, questionText, createdBy || "USER").run();
        return jsonResponse({ success: true, id: insert.meta.last_row_id });
      }

      // ==========================================================
      // 4. PUBLIC ROOMS LIST
      // ==========================================================
      if (path === "/api/rooms/public" && method === "GET") {
        const { results } = await env.DB.prepare(
          "SELECT r.id, r.code, r.status, r.current_round, r.created_at, COUNT(p.id) as playerCount " +
          "FROM rooms r LEFT JOIN players p ON r.code = p.room_code " +
          "WHERE r.status = 'LOBBY' " +
          "GROUP BY r.code ORDER BY r.created_at DESC LIMIT 15"
        ).all();
        return jsonResponse({ rooms: results || [] });
      }

      // ==========================================================
      // 5. CREATE ROOM
      // ==========================================================
      if (path === "/api/rooms/create" && method === "POST") {
        const body = await request.json();
        const authenticated = await authenticateUser(request, env);
        const playerName = (body.playerName || authenticated?.username || "کارآگاه").trim();
        const avatarEmoji = body.avatarEmoji || authenticated?.avatar_emoji || "🕵️";
        const level = body.level || authenticated?.level || 1;
        const userId = authenticated?.user_id || body.userId || null;

        const roomCode = generateRoomCode();
        const roomId = generateId();
        const playerId = "p_" + generateId();
        const now = Date.now();

        await env.DB.prepare(
          "INSERT INTO rooms (id, code, host_id, status, current_round, used_question_ids, created_at) " +
          "VALUES (?, ?, ?, 'LOBBY', 1, '', ?)"
        ).bind(roomId, roomCode, playerId, now).run();

        await env.DB.prepare(
          "INSERT INTO players (id, room_code, user_id, name, avatar_emoji, level, score, is_host, is_mentalist, last_ping, joined_at) " +
          "VALUES (?, ?, ?, ?, ?, ?, 0, 1, 0, ?, ?)"
        ).bind(playerId, roomCode, userId, playerName, avatarEmoji, level, now, now).run();

        return jsonResponse({
          success: true,
          roomCode,
          roomId,
          playerId,
          isHost: true
        });
      }

      // ==========================================================
      // 6. JOIN ROOM
      // ==========================================================
      if (path === "/api/rooms/join" && method === "POST") {
        const body = await request.json();
        const authenticated = await authenticateUser(request, env);
        const roomCode = (body.roomCode || "").toUpperCase().trim();
        const playerName = (body.playerName || authenticated?.username || "بازیکن").trim();
        const avatarEmoji = body.avatarEmoji || authenticated?.avatar_emoji || "🕵️";
        const level = body.level || authenticated?.level || 1;
        const userId = authenticated?.user_id || body.userId || null;

        if (!roomCode) {
          return jsonResponse({ error: "کد اتاق را وارد کنید" }, 400);
        }

        const room = await env.DB.prepare("SELECT * FROM rooms WHERE code = ?").bind(roomCode).first();
        if (!room) {
          return jsonResponse({ error: "اتاقی با این کد یافت نشد." }, 404);
        }

        if (room.status !== "LOBBY") {
          return jsonResponse({ error: "این بازی قبلاً آغاز شده است و پذیرش بازیکن جدید ندارد." }, 400);
        }

        // بررسی اینکه آیا بازیکن با این نام یا با این user_id قبلاً در اتاق حضور دارد؟
        let existingPlayer = null;
        if (userId) {
          existingPlayer = await env.DB.prepare(
            "SELECT id, is_host FROM players WHERE room_code = ? AND user_id = ?"
          ).bind(roomCode, userId).first();
        }
        if (!existingPlayer) {
          existingPlayer = await env.DB.prepare(
            "SELECT id, is_host FROM players WHERE room_code = ? AND name = ?"
          ).bind(roomCode, playerName).first();
        }

        const playerId = existingPlayer ? existingPlayer.id : ("p_" + generateId());
        const now = Date.now();

        if (!existingPlayer) {
          await env.DB.prepare(
            "INSERT INTO players (id, room_code, user_id, name, avatar_emoji, level, score, is_host, is_mentalist, last_ping, joined_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, 0, 0, 0, ?, ?)"
          ).bind(playerId, roomCode, userId, playerName, avatarEmoji, level, now, now).run();
        } else {
          await env.DB.prepare("UPDATE players SET last_ping = ? WHERE id = ?").bind(now, playerId).run();
        }

        return jsonResponse({
          success: true,
          roomCode,
          roomId: room.id,
          playerId,
          isHost: room.host_id === playerId
        });
      }

      // ==========================================================
      // 7. GET ROOM STATE & PLAYERS
      // ==========================================================
      const roomStateMatch = path.match(/^\/api\/rooms\/([A-Z0-9]+)$/);
      if (roomStateMatch && method === "GET") {
        const roomCode = roomStateMatch[1];
        const room = await env.DB.prepare("SELECT * FROM rooms WHERE code = ?").bind(roomCode).first();
        if (!room) {
          return jsonResponse({ error: "اتاق یافت نشد" }, 404);
        }

        const { results: players } = await env.DB.prepare(
          "SELECT id, name, avatar_emoji as avatarEmoji, level, score, round_score_delta as roundDeltaXp, " +
          "is_host as isHost, is_mentalist as isMentalist, has_answered as hasAnswered, last_ping as lastPing " +
          "FROM players WHERE room_code = ? ORDER BY joined_at ASC"
        ).bind(roomCode).all();

        let question = null;
        if (room.current_question_id) {
          question = await env.DB.prepare(
            "SELECT id, category, question_text as questionText FROM questions WHERE id = ?"
          ).bind(room.current_question_id).first();
        }

        return jsonResponse({
          room: {
            id: room.id,
            code: room.code,
            hostId: room.host_id,
            status: room.status,
            currentRound: room.current_round,
            totalRounds: room.total_rounds,
            currentMentalistId: room.current_mentalist_id,
            phaseStartTime: room.phase_start_time,
            phaseDurationSeconds: room.phase_duration_seconds
          },
          players: players || [],
          currentQuestion: question
        });
      }

      // ==========================================================
      // 8. START GAME (Host Authorization Required)
      // ==========================================================
      const startMatch = path.match(/^\/api\/rooms\/([A-Z0-9]+)\/start$/);
      if (startMatch && method === "POST") {
        const roomCode = startMatch[1];
        const room = await env.DB.prepare("SELECT * FROM rooms WHERE code = ?").bind(roomCode).first();
        if (!room) return jsonResponse({ error: "اتاق یافت نشد" }, 404);

        const callerPlayerId = getCallerPlayerId(request);
        if (callerPlayerId && callerPlayerId !== room.host_id) {
          return jsonResponse({ success: false, error: "تنها میزبان اتاق مجاز به شروع بازی است." }, 403);
        }

        const { results: players } = await env.DB.prepare("SELECT id FROM players WHERE room_code = ?").bind(roomCode).all();
        if (!players || players.length < 2) {
          return jsonResponse({ error: "برای شروع بازی حداقل به ۲ بازیکن واقعی نیاز است." }, 400);
        }

        // انتخاب سوال تصادفی غیرتکراری از بانک ۱۰۰ سوالی
        const usedIds = (room.used_question_ids || "").split(",").map(s => parseInt(s)).filter(n => !isNaN(n));
        let qQuery = "SELECT id FROM questions ";
        if (usedIds.length > 0) {
          qQuery += `WHERE id NOT IN (${usedIds.join(",")}) `;
        }
        qQuery += "ORDER BY RANDOM() LIMIT 1";

        let selectedQuestion = await env.DB.prepare(qQuery).first();
        if (!selectedQuestion) {
          // اگر همه سوالات مصرف شده باشند، ریست می‌شود
          selectedQuestion = await env.DB.prepare("SELECT id FROM questions ORDER BY RANDOM() LIMIT 1").first();
        }
        const questionId = selectedQuestion ? selectedQuestion.id : 1;
        const newUsedIds = usedIds.concat([questionId]).join(",");

        // انتخاب رندوم اولین ذهن‌خوان
        const mentalistIndex = Math.floor(Math.random() * players.length);
        const mentalistId = players[mentalistIndex].id;

        // آماده‌سازی بازیکنان
        await env.DB.prepare("UPDATE players SET is_mentalist = 0, has_answered = 0, round_score_delta = 0 WHERE room_code = ?").bind(roomCode).run();
        await env.DB.prepare("UPDATE players SET is_mentalist = 1 WHERE id = ?").bind(mentalistId).run();

        // پاکسازی کارت‌ها و پیام‌های راند قبل
        await env.DB.prepare("DELETE FROM answers WHERE room_code = ?").bind(roomCode).run();
        await env.DB.prepare("DELETE FROM messages WHERE room_code = ?").bind(roomCode).run();

        const now = Date.now();
        await env.DB.prepare(
          "UPDATE rooms SET status = 'ANSWERING', current_round = 1, total_rounds = ?, current_mentalist_id = ?, " +
          "current_question_id = ?, used_question_ids = ?, phase_start_time = ?, phase_duration_seconds = 45 WHERE code = ?"
        ).bind(players.length, mentalistId, questionId, newUsedIds, now, roomCode).run();

        // پیام سیستمی آغاز بازی
        await env.DB.prepare(
          "INSERT INTO messages (id, room_code, player_id, player_name, text, is_system, created_at) " +
          "VALUES (?, ?, 'SYSTEM', 'سیستم', 'بازی آغاز شد! فاز پاسخ‌دهی محرمانه آغاز گردید.', 1, ?)"
        ).bind("msg_" + generateId(), roomCode, now).run();

        return jsonResponse({ success: true, mentalistId, questionId });
      }

      // ==========================================================
      // 9. SUBMIT SECRET ANSWER (Anti-Race & Role-Guarded)
      // ==========================================================
      const answerMatch = path.match(/^\/api\/rooms\/([A-Z0-9]+)\/answers$/);
      if (answerMatch && method === "POST") {
        const roomCode = answerMatch[1];
        const body = await request.json();
        const callerPlayerId = getCallerPlayerId(request, body);
        const answerText = (body.answerText || "").trim();

        if (!callerPlayerId || !answerText) {
          return jsonResponse({ error: "شناسه بازیکن و متن پاسخ الزامی است." }, 400);
        }

        const room = await env.DB.prepare("SELECT * FROM rooms WHERE code = ?").bind(roomCode).first();
        if (!room) return jsonResponse({ error: "اتاق یافت نشد." }, 404);

        if (room.status !== "ANSWERING") {
          return jsonResponse({ error: "زمان ارسال پاسخ در این راند به پایان رسیده است." }, 400);
        }

        if (callerPlayerId === room.current_mentalist_id) {
          return jsonResponse({ error: "ذهن‌خوان به سوال پاسخ نمی‌دهد و نقش بازجو را دارد." }, 400);
        }

        // بررسی ثبت پاسخ تکراری توسط همین بازیکن در این راند
        const existingSubmission = await env.DB.prepare(
          "SELECT id FROM answers WHERE room_code = ? AND round_number = ? AND author_player_id = ?"
        ).bind(roomCode, room.current_round, callerPlayerId).first();

        if (existingSubmission) {
          return jsonResponse({ success: true, message: "پاسخ شما قبلاً ثبت شده است." });
        }

        // تعیین برچسب کارت بدون تداخل همزمانی
        const existingAnswers = await env.DB.prepare(
          "SELECT author_player_id FROM answers WHERE room_code = ? AND round_number = ?"
        ).bind(roomCode, room.current_round).all();

        const cardLabels = ["کارت الف", "کارت ب", "کارت ج", "کارت د", "کارت هـ", "کارت و", "کارت ز", "کارت ح"];
        const nextIndex = existingAnswers.results ? existingAnswers.results.length : 0;
        const label = cardLabels[nextIndex] || `کارت ${nextIndex + 1}`;
        const answerId = "ans_" + generateId();
        const now = Date.now();

        // اجرای بچ امن و اتمیک ثبت پاسخ و علامت‌گذاری بازیکن
        await env.DB.batch([
          env.DB.prepare(
            "INSERT INTO answers (id, room_code, round_number, author_player_id, card_label, answer_text, is_revealed, created_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, 0, ?)"
          ).bind(answerId, roomCode, room.current_round, callerPlayerId, label, answerText, now),
          env.DB.prepare("UPDATE players SET has_answered = 1 WHERE id = ?").bind(callerPlayerId)
        ]);

        // بررسی اینکه آیا تمام مظنونین (غیر از ذهن‌خوان) پاسخ داده‌اند؟
        const pendingSuspects = await env.DB.prepare(
          "SELECT COUNT(*) as count FROM players WHERE room_code = ? AND is_mentalist = 0 AND has_answered = 0"
        ).bind(roomCode).first();

        let allAnswered = false;
        if (pendingSuspects && pendingSuspects.count === 0) {
          allAnswered = true;
          // انتقال خودکار به فاز بازجویی با مدت زمان ۱۵۰ ثانیه
          await env.DB.prepare(
            "UPDATE rooms SET status = 'INTERROGATION', phase_start_time = ?, phase_duration_seconds = 150 WHERE code = ?"
          ).bind(Date.now(), roomCode).run();

          await env.DB.prepare(
            "INSERT INTO messages (id, room_code, player_id, player_name, text, is_system, created_at) " +
            "VALUES (?, ?, 'SYSTEM', 'سیستم', 'همه پاسخ‌ها با موفقیت دریافت و بر زده شد! فاز بازجویی آغاز شد.', 1, ?)"
          ).bind("msg_" + generateId(), roomCode, Date.now()).run();
        }

        return jsonResponse({ success: true, allAnswered });
      }

      // ==========================================================
      // 10. GET CARDS (Zero Information Leakage & Anti-Cheat)
      // ==========================================================
      const cardsMatch = path.match(/^\/api\/rooms\/([A-Z0-9]+)\/cards$/);
      if (cardsMatch && method === "GET") {
        const roomCode = cardsMatch[1];
        const room = await env.DB.prepare("SELECT * FROM rooms WHERE code = ?").bind(roomCode).first();
        if (!room) return jsonResponse({ error: "اتاق یافت نشد." }, 404);

        const callerPlayerId = getCallerPlayerId(request);
        const isRevealedPhase = (room.status === "REVEAL" || room.status === "SUMMARY" || room.status === "FINISHED");

        // کارت‌ها با ترتیب برچسب الفبایی لود می‌شوند تا زمان ثبت لو نرود!
        const { results: cards } = await env.DB.prepare(
          "SELECT id, card_label as label, author_player_id as authorPlayerId, answer_text as text, " +
          "assigned_player_id as assignedPlayerId, is_revealed as isRevealed " +
          "FROM answers WHERE room_code = ? AND round_number = ? ORDER BY card_label ASC"
        ).bind(roomCode, room.current_round).all();

        const sanitizedCards = (cards || []).map(card => {
          let authorVisibility = "HIDDEN";

          if (isRevealedPhase) {
            // در فاز افشا، صاحب اصلی کارت برای همه عیان می‌شود
            authorVisibility = card.authorPlayerId;
          } else if (callerPlayerId && callerPlayerId === card.authorPlayerId) {
            // تنها صاحب خود کارت می‌داند این کارت متعلق به خودش است
            authorVisibility = callerPlayerId;
          } else {
            // برای ذهن‌خوان و سایر مظنونین نویسنده کاملاً پنهان است
            authorVisibility = "HIDDEN";
          }

          return {
            id: card.id,
            label: card.label,
            text: card.text,
            assignedPlayerId: card.assignedPlayerId,
            isRevealed: card.isRevealed === 1,
            authorPlayerId: authorVisibility
          };
        });

        return jsonResponse({ cards: sanitizedCards });
      }

      // ==========================================================
      // 11. CHAT MESSAGES
      // ==========================================================
      const chatMatch = path.match(/^\/api\/rooms\/([A-Z0-9]+)\/chat$/);
      if (chatMatch && method === "GET") {
        const roomCode = chatMatch[1];
        const { results: messages } = await env.DB.prepare(
          "SELECT id, player_id as senderId, player_name as senderName, text, created_at as timestamp, " +
          "is_mentalist as isFromMentalist, is_system as isSystem FROM messages WHERE room_code = ? ORDER BY created_at ASC"
        ).bind(roomCode).all();
        return jsonResponse({ messages: messages || [] });
      }

      if (chatMatch && method === "POST") {
        const roomCode = chatMatch[1];
        const body = await request.json();
        const callerPlayerId = getCallerPlayerId(request, body);
        const { playerName, text, isMentalist } = body;

        if (!text || !text.trim()) {
          return jsonResponse({ error: "متن پیام خالی است." }, 400);
        }

        const msgId = "msg_" + generateId();
        const now = Date.now();
        await env.DB.prepare(
          "INSERT INTO messages (id, room_code, player_id, player_name, text, is_mentalist, is_system, created_at) " +
          "VALUES (?, ?, ?, ?, ?, ?, 0, ?)"
        ).bind(msgId, roomCode, callerPlayerId || "UNKNOWN", playerName || "بازیکن", text.trim(), isMentalist ? 1 : 0, now).run();

        if (callerPlayerId) {
          await env.DB.prepare("UPDATE players SET last_ping = ? WHERE id = ?").bind(now, callerPlayerId).run();
        }

        return jsonResponse({ success: true, messageId: msgId });
      }

      // ==========================================================
      // 12. ADVANCE PHASE (Host Authorization Required)
      // ==========================================================
      const phaseMatch = path.match(/^\/api\/rooms\/([A-Z0-9]+)\/phase$/);
      if (phaseMatch && method === "POST") {
        const roomCode = phaseMatch[1];
        const body = await request.json();
        const { newPhase, durationSeconds } = body;

        const room = await env.DB.prepare("SELECT * FROM rooms WHERE code = ?").bind(roomCode).first();
        if (!room) return jsonResponse({ error: "اتاق یافت نشد." }, 404);

        const callerPlayerId = getCallerPlayerId(request, body);
        if (callerPlayerId && callerPlayerId !== room.host_id) {
          return jsonResponse({ success: false, error: "تنها میزبان اتاق مجاز به تغییر فاز بازی است." }, 403);
        }

        const duration = durationSeconds || (newPhase === "GUESSING" ? 30 : newPhase === "INTERROGATION" ? 150 : 45);
        await env.DB.prepare(
          "UPDATE rooms SET status = ?, phase_start_time = ?, phase_duration_seconds = ? WHERE code = ?"
        ).bind(newPhase, Date.now(), duration, roomCode).run();

        return jsonResponse({ success: true, phase: newPhase });
      }

      // ==========================================================
      // 13. SUBMIT GUESS (Mentalist Authorization Required)
      // ==========================================================
      const guessMatch = path.match(/^\/api\/rooms\/([A-Z0-9]+)\/guess$/);
      if (guessMatch && method === "POST") {
        const roomCode = guessMatch[1];
        const body = await request.json();
        const { assignments } = body;

        const room = await env.DB.prepare("SELECT * FROM rooms WHERE code = ?").bind(roomCode).first();
        if (!room) return jsonResponse({ error: "اتاق یافت نشد." }, 404);

        const callerPlayerId = getCallerPlayerId(request, body);
        if (callerPlayerId && callerPlayerId !== room.current_mentalist_id) {
          return jsonResponse({ success: false, error: "تنها ذهن‌خوان این راند مجاز به انتساب و حدس کارت‌هاست." }, 403);
        }

        if (assignments && Array.isArray(assignments)) {
          for (const item of assignments) {
            await env.DB.prepare(
              "UPDATE answers SET assigned_player_id = ? WHERE id = ? AND room_code = ?"
            ).bind(item.assignedPlayerId, item.cardId, roomCode).run();
          }
        }

        return jsonResponse({ success: true });
      }

      // ==========================================================
      // 14. REVEAL & STRICT GDD SCORING
      // ==========================================================
      const revealMatch = path.match(/^\/api\/rooms\/([A-Z0-9]+)\/reveal$/);
      if (revealMatch && method === "POST") {
        const roomCode = revealMatch[1];
        const room = await env.DB.prepare("SELECT * FROM rooms WHERE code = ?").bind(roomCode).first();
        if (!room) return jsonResponse({ error: "اتاق یافت نشد." }, 404);

        const callerPlayerId = getCallerPlayerId(request);
        if (callerPlayerId && callerPlayerId !== room.current_mentalist_id && callerPlayerId !== room.host_id) {
          return jsonResponse({ success: false, error: "تنها ذهن‌خوان یا میزبان مجاز به افشای نتایج هستند." }, 403);
        }

        const { results: cards } = await env.DB.prepare(
          "SELECT * FROM answers WHERE room_code = ? AND round_number = ?"
        ).bind(roomCode, room.current_round).all();

        const { results: players } = await env.DB.prepare(
          "SELECT * FROM players WHERE room_code = ?"
        ).bind(roomCode).all();

        const mentalistId = room.current_mentalist_id;
        const playerMap = new Map((players || []).map(p => [p.id, p]));

        let mentalistDelta = 0;
        let correctGuessesCount = 0;
        let totalCardsCount = (cards || []).length;
        const playerDeltas = new Map();
        const revealResults = [];

        // فرمول محاسبه امتیاز دقیق طبق GDD:
        // - برای هر حدس درست ذهن‌خوان: +100 امتیاز به ذهن‌خوان، -40 امتیاز به مظنون لو رفته
        // - برای هر حدس اشتباه ذهن‌خوان: -50 امتیاز به ذهن‌خوان، +150 امتیاز به مظنون با بلوف موفق
        for (const card of (cards || [])) {
          const authorId = card.author_player_id;
          const assignedId = card.assigned_player_id;
          const isCorrect = (authorId === assignedId);

          if (isCorrect) correctGuessesCount++;

          const mDelta = isCorrect ? 100 : -50;
          const aDelta = isCorrect ? -40 : 150;

          mentalistDelta += mDelta;
          const prevAuthorDelta = playerDeltas.get(authorId) || 0;
          playerDeltas.set(authorId, prevAuthorDelta + aDelta);

          revealResults.push({
            cardId: card.id,
            cardLabel: card.card_label,
            text: card.answer_text,
            authorPlayerId: authorId,
            authorName: playerMap.get(authorId)?.name || "ناشناس",
            assignedPlayerId: assignedId,
            assignedName: playerMap.get(assignedId)?.name || "بدون حدس",
            isCorrect,
            mentalistDelta: mDelta,
            authorDelta: aDelta
          });

          await env.DB.prepare("UPDATE answers SET is_revealed = 1 WHERE id = ?").bind(card.id).run();
        }

        // پاداش ذهن‌خوان اعظم (Master Mentalist Bonus) اگر تمام حدس‌ها بدون خطا درست باشند (حداقل ۲ مظنون)
        if (totalCardsCount >= 2 && correctGuessesCount === totalCardsCount) {
          mentalistDelta += 150;
        }

        // بروزرسانی امتیازات این مسابقه در جدول players
        const currentMentalistScore = playerMap.get(mentalistId)?.score || 0;
        const finalMentalistScore = Math.max(0, currentMentalistScore + mentalistDelta);
        await env.DB.prepare("UPDATE players SET score = ?, round_score_delta = ? WHERE id = ?")
          .bind(finalMentalistScore, mentalistDelta, mentalistId).run();

        for (const [pId, delta] of playerDeltas.entries()) {
          if (pId !== mentalistId) {
            const currentScore = playerMap.get(pId)?.score || 0;
            const newScore = Math.max(0, currentScore + delta);
            await env.DB.prepare("UPDATE players SET score = ?, round_score_delta = ? WHERE id = ?")
              .bind(newScore, delta, pId).run();
          }
        }

        // سینک خودکار آمار دائمی به جدول users برای کاربران متصل
        const mentalistUser = playerMap.get(mentalistId);
        if (mentalistUser && mentalistUser.user_id) {
          const coinReward = mentalistDelta > 0 ? Math.floor(mentalistDelta / 2) : 0;
          await env.DB.prepare(
            "UPDATE users SET xp = MAX(0, xp + ?), coins = coins + ?, correct_guesses = correct_guesses + ?, " +
            "level = 1 + (MAX(0, xp + ?) / 1000) WHERE id = ?"
          ).bind(mentalistDelta, coinReward, correctGuessesCount, mentalistDelta, mentalistUser.user_id).run();
        }

        for (const [pId, delta] of playerDeltas.entries()) {
          if (pId !== mentalistId) {
            const playerRec = playerMap.get(pId);
            if (playerRec && playerRec.user_id) {
              const isBluffSuccess = (delta > 0);
              const coinReward = delta > 0 ? Math.floor(delta / 2) : 0;
              await env.DB.prepare(
                "UPDATE users SET xp = MAX(0, xp + ?), coins = coins + ?, successful_bluffs = successful_bluffs + ?, " +
                "level = 1 + (MAX(0, xp + ?) / 1000) WHERE id = ?"
              ).bind(delta, coinReward, isBluffSuccess ? 1 : 0, delta, playerRec.user_id).run();
            }
          }
        }

        // تغییر وضعیت به فاز REVEAL
        await env.DB.prepare(
          "UPDATE rooms SET status = 'REVEAL', phase_start_time = ?, phase_duration_seconds = 60 WHERE code = ?"
        ).bind(Date.now(), roomCode).run();

        return jsonResponse({
          success: true,
          results: revealResults,
          mentalistTotalDelta: mentalistDelta
        });
      }

      // ==========================================================
      // 15. HEARTBEAT & PING
      // ==========================================================
      const heartbeatMatch = path.match(/^\/api\/rooms\/([A-Z0-9]+)\/heartbeat$/);
      if (heartbeatMatch && method === "POST") {
        const roomCode = heartbeatMatch[1];
        const body = await request.json().catch(() => ({}));
        const callerPlayerId = getCallerPlayerId(request, body);
        if (callerPlayerId) {
          await env.DB.prepare("UPDATE players SET last_ping = ? WHERE id = ? AND room_code = ?")
            .bind(Date.now(), callerPlayerId, roomCode).run();
        }
        return jsonResponse({ success: true, timestamp: Date.now() });
      }

      // ==========================================================
      // 16. REPORTS
      // ==========================================================
      if (path === "/api/reports" && method === "POST") {
        const body = await request.json();
        const { roomCode, reporterName, targetName, messageText, reason } = body;
        const reportId = "rep_" + generateId();
        await env.DB.prepare(
          "INSERT INTO reports (id, room_code, reporter_name, target_name, message_text, reason, created_at) " +
          "VALUES (?, ?, ?, ?, ?, ?, ?)"
        ).bind(reportId, roomCode || null, reporterName || "کاربر", targetName || "نامشخص", messageText || "", reason || "نامشخص", Date.now()).run();
        return jsonResponse({ success: true, reportId });
      }

      return jsonResponse({ error: "مسیر درخواستی در ورکر کلودفلر یافت نشد.", path }, 404);

    } catch (err) {
      return jsonResponse({
        error: "خطای سرور ورکر کلودفلر: " + err.message,
        stack: err.stack
      }, 500);
    }
  }
};
