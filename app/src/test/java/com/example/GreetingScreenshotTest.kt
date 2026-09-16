package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.local.UserProfileEntity
import com.example.data.remote.D1ConnectionState
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun home_screen_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        HomeScreen(
          userProfile = UserProfileEntity(
            id = "test_user",
            username = "کارآگاه_آزمایشی",
            level = 1,
            xp = 0,
            coins = 0
          ),
          badges = emptyList(),
          d1State = D1ConnectionState.Connected(latencyMs = 45, database = "siahbazi-db", questionCount = 12, activeRoomsCount = 2),
          workerUrl = "https://siahbazi-backend.workers.dev",
          publicRooms = emptyList(),
          isRefreshingRooms = false,
          onCreateRoom = {},
          onOpenJoinDialog = {},
          onRefreshRooms = {},
          onOpenD1Settings = {},
          onOpenD1Guide = {},
          onOpenQuests = {},
          onOpenQuestions = {},
          onOpenRules = {},
          onLogout = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/home_screen.png")
  }
}
