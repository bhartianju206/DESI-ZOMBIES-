package com.example

import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.audio.GameAudio
import com.example.game.GameEngine
import com.example.ui.screens.GamePlayScreen
import com.example.ui.screens.HowToPlayDialog
import com.example.ui.screens.TitleScreen
import com.example.ui.theme.MyApplicationTheme

enum class AppScreen {
    TITLE,
    GAMEPLAY
}

class MainActivity : ComponentActivity() {
    private lateinit var audio: GameAudio
    private lateinit var gameEngine: GameEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideSystemBars()

        audio = GameAudio(this)
        gameEngine = GameEngine(this, audio)

        setContent {
            MyApplicationTheme {
                var currentScreen by remember { mutableStateOf(AppScreen.TITLE) }
                var showHowToPlay by remember { mutableStateOf(false) }
                val uiState by gameEngine.uiState.collectAsState()

                BackHandler {
                    if (showHowToPlay) {
                        showHowToPlay = false
                    } else if (currentScreen == AppScreen.GAMEPLAY) {
                        if (!uiState.isPaused) {
                            gameEngine.togglePause()
                        } else {
                            currentScreen = AppScreen.TITLE
                        }
                    } else {
                        finish()
                    }
                }

                when (currentScreen) {
                    AppScreen.TITLE -> {
                        TitleScreen(
                            highScore = uiState.highScore,
                            isSoundEnabled = gameEngine.isSoundEnabled(),
                            onPlayClick = {
                                gameEngine.startNewRun()
                                currentScreen = AppScreen.GAMEPLAY
                            },
                            onHowToPlayClick = {
                                showHowToPlay = true
                            },
                            onToggleSoundClick = {
                                gameEngine.setSoundEnabled(!gameEngine.isSoundEnabled())
                            }
                        )

                        if (showHowToPlay) {
                            HowToPlayDialog(
                                onDismiss = { showHowToPlay = false },
                                onTestAudio = { type ->
                                    gameEngine.audio.testPlayDialogue(type)
                                }
                            )
                        }
                    }

                    AppScreen.GAMEPLAY -> {
                        GamePlayScreen(
                            engine = gameEngine,
                            onNavigateToMenu = {
                                currentScreen = AppScreen.TITLE
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    private fun hideSystemBars() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
    }

    override fun onPause() {
        super.onPause()
        if (::audio.isInitialized) {
            audio.stopAll()
        }
        if (::gameEngine.isInitialized && !gameEngine.isPaused) {
            gameEngine.isPaused = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::audio.isInitialized) {
            audio.release()
        }
    }
}
