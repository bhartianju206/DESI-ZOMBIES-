package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.GameUIState
import com.example.ui.theme.*

@Composable
fun GameOverScreen(
    state: GameUIState,
    onRestartClick: () -> Unit,
    onMainMenuClick: () -> Unit
) {
    val isNewHighScore = state.currentScore >= state.highScore && state.currentScore > 0

    val infiniteTransition = rememberInfiniteTransition(label = "game_over_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF00A0A10))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .background(Color(0xFF161824), RoundedCornerShape(12.dp))
                .border(BorderStroke(2.dp, DesiBloodRed), RoundedCornerShape(12.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Fallen Player Image
            Image(
                painter = painterResource(id = R.drawable.player_dead),
                contentDescription = "Fallen Survivor",
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "SURVIVOR HAS FALLEN",
                color = DesiBloodRed,
                fontSize = 20.sp,
                fontFamily = PixelFontFamily,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.scale(pulseScale)
            )

            Text(
                text = "ZOMBIES OVERRAN THE MOHALLA",
                color = DesiTextDim,
                fontSize = 9.sp,
                fontFamily = PixelFontFamily,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Run Statistics Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F101A), RoundedCornerShape(8.dp))
                    .border(BorderStroke(1.dp, DesiCardBorder), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatRow("FINAL SCORE:", "${state.currentScore}", DesiToxicGreen)
                if (isNewHighScore) {
                    Text(
                        text = "★ NEW ALL-TIME RECORD! ★",
                        color = DesiChaiGold,
                        fontSize = 9.sp,
                        fontFamily = PixelFontFamily,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                StatRow("HIGHEST SCORE:", "${state.highScore}", DesiChaiGold)
                StatRow("WAVES SURVIVED:", "${state.currentWave}", DesiCyan)
                StatRow("ZOMBIES KILLED:", "${state.totalZombiesKilled}", DesiBloodRed)
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Buttons: RESTART RUN & MAIN MENU
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Button(
                    onClick = onRestartClick,
                    colors = ButtonDefaults.buttonColors(containerColor = DesiToxicGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("restart_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restart",
                            tint = DesiStreetNight,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "RESTART",
                            color = DesiStreetNight,
                            fontSize = 11.sp,
                            fontFamily = PixelFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Button(
                    onClick = onMainMenuClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF282C40)),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, DesiCardBorder),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("main_menu_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Menu",
                            tint = DesiTextBright,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "MAIN MENU",
                            color = DesiTextBright,
                            fontSize = 11.sp,
                            fontFamily = PixelFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = DesiTextDim,
            fontSize = 9.sp,
            fontFamily = PixelFontFamily
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 11.sp,
            fontFamily = PixelFontFamily,
            fontWeight = FontWeight.Bold
        )
    }
}
