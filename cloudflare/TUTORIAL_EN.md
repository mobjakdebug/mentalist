# 🚀 Step-by-Step Cloudflare Worker & D1 Database Setup Guide

This `cloudflare/` folder contains all the necessary backend files for the **Siahbazi** game:

| File | Description |
| :--- | :--- |
| `worker.js` | Cloudflare Worker script containing all endpoints (Authentication with SHA-256 password hashing, room creation, secret answer submissions, live interrogation chat, and mentalist guesses) |
| `schema.sql` | D1 SQL database schema with 8 tables (`users`, `rooms`, `players`, `questions`, `answers`, `messages`, `reports`) and query indexes |
| `wrangler.toml` | Wrangler configuration file for D1 binding |
| `package.json` | Convenient npm scripts to run wrangler commands |
| `TUTORIAL.md` | Persian step-by-step tutorial |

---

## ⚡ Deployment in 5 Simple Steps:

### Step 1: Open terminal in this folder and log in
```bash
cd cloudflare
npx wrangler login
```

### Step 2: Create the D1 Database
```bash
npx wrangler d1 create siahbazi-db
```
Copy the `database_id` generated in the terminal output.

### Step 3: Put your `database_id` into `wrangler.toml`
Open `wrangler.toml` in this folder and update line 9:
```toml
[[d1_databases]]
binding = "DB"
database_name = "siahbazi-db"
database_id = "<your-copied-database-id>"
```

### Step 4: Execute the SQL schema on the cloud database
```bash
npx wrangler d1 execute siahbazi-db --file=schema.sql --remote
```

### Step 5: Deploy the Worker
```bash
npx wrangler deploy
```
Copy your published worker URL:
`https://siahbazi-backend.<your-subdomain>.workers.dev`

---

## 📱 Connect to the Android App:
1. Open the Siahbazi app on your phone or emulator.
2. Tap the **D1 Status Badge / Settings Gear icon** at the top right of the Auth screen or Home screen.
3. Paste your Worker URL and tap **"تست اتصال و ذخیره"** (Test & Save).
4. The status turns green with your server latency (Ping). You are all set!
