package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.QuestionBank
import com.example.model.RankTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("سیاه بازی", appName)
  }

  @Test
  fun `verify question bank loaded with psychological questions`() {
    assertTrue(QuestionBank.defaultQuestions.size >= 10)
    assertTrue(QuestionBank.defaultQuestions.any { it.questionText.contains("دروغ") })
  }

  @Test
  fun `verify rank tiers progression`() {
    assertEquals(RankTier.PROFILER, RankTier.fromLevel(3))
    assertEquals(RankTier.TACTICIAN, RankTier.fromLevel(15))
    assertEquals(RankTier.SHADOW, RankTier.fromLevel(30))
    assertEquals(RankTier.ILLUSIONIST, RankTier.fromLevel(45))
    assertEquals(RankTier.MENTALIST, RankTier.fromLevel(70))
  }
}

