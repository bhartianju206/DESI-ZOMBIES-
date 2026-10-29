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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

@Composable
fun TitleScreen(
    highScore: Int,
    isSoundEnabled: Boolean,
    onPlayClick: () -> Unit,
    onHowToPlayClick: () -> Unit,
    onToggleSoundClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "title_fx")
    val titlePulse by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DesiStreetNight)
    ) {
        // Pixel Art City Background
        Image(
            painter = painterResource(id = R.drawable.bg_title_city),
            contentDescription = "Desi City Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Dark gradient overlay to preserve contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x990A0B12))
        )

        // Silhouettes of zombies & player at bottom
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 20.dp, start = 30.dp, end = 30.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Player character holding pistol
            Image(
                painter = painterResource(id = R.drawable.player_shoot),
                contentDescription = "Desi Survivor",
                modifier = Modifier
                    .size(90.dp)
                    .scale(1.2f)
            )

            // Zombie Parody silhouettes
            Row(horizontalArrangement = Arrangement.spacedBy((-15).dp)) {
                Image(
                    painter = painterResource(id = R.drawable.zombie_modi),
                    contentDescription = "Zombie",
                    modifier = Modifier.size(75.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.zombie_acp),
                    contentDescription = "Zombie",
                    modifier = Modifier.size(75.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.zombie_amitabh),
                    contentDescription = "Zombie",
                    modifier = Modifier.size(78.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.zombie_salman),
                    contentDescription = "Zombie",
                    modifier = Modifier.size(75.dp)
                )
            }
        }

        // MAIN CONTENT COLUMN
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. TOP OF SCREEN: "CREATER : Raunak_.gpt_"
            Box(
                modifier = Modifier
                    .background(Color(0xDD171926), RoundedCornerShape(12.dp))
                    .border(BorderStroke(1.5.dp, DesiChaiGold), RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .testTag("creator_badge")
            ) {
                Text(
                    text = "CREATER : Raunak_.gpt_",
                    color = DesiChaiGold,
                    fontSize = 13.sp,
                    fontFamily = PixelFontFamily,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            // 2. MAIN TITLE & SUBTITLE
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.scale(titlePulse)
            ) {
                Text(
                    text = "DESI ZOMBIES",
                    color = DesiBloodRed,
                    fontSize = 32.sp,
                    fontFamily = PixelFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "SURVIVE THE DESI APOCALYPSE",
                    color = DesiToxicGreen,
                    fontSize = 11.sp,
                    fontFamily = PixelFontFamily,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )

                if (highScore > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "★ ALL-TIME HIGH SCORE: $highScore ★",
                        color = DesiChaiGold,
                        fontSize = 10.sp,
                        fontFamily = PixelFontFamily
                    )
                }
            }

            // 3. MAIN MENU BUTTONS
            // PLAY, HOW TO PLAY, SOUND: ON/OFF
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // PLAY BUTTON
                MenuButton(
                    text = "PLAY",
                    icon = Icons.Default.PlayArrow,
                    bgColor = DesiToxicGreen,
                    contentColor = DesiStreetNight,
                    testTag = "play_button",
                    onClick = onPlayClick
                )

                // HOW TO PLAY BUTTON
                MenuButton(
                    text = "HOW TO PLAY",
                    icon = Icons.Default.Info,
                    bgColor = Color(0xFF232738),
                    borderColor = DesiCyan,
                    contentColor = DesiCyan,
                    testTag = "how_to_play_button",
                    onClick = onHowToPlayClick
                )

                // SOUND ON/OFF BUTTON
                MenuButton(
                    text = if (isSoundEnabled) "SOUND: ON" else "SOUND: OFF",
                    icon = if (isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    bgColor = Color(0xFF232738),
                    borderColor = if (isSoundEnabled) DesiChaiGold else DesiBloodRed,
                    contentColor = if (isSoundEnabled) DesiChaiGold else DesiBloodRed,
                    testTag = "sound_toggle_button",
                    onClick = onToggleSoundClick
                )
            }
        }
    }
}

@Composable
fun MenuButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    bgColor: Color,
    contentColor: Color,
    testTag: String,
    borderColor: Color? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .then(
                if (borderColor != null) Modifier.border(
                    BorderStroke(2.dp, borderColor),
                    RoundedCornerShape(8.dp)
                ) else Modifier
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = contentColor,
                fontSize = 11.sp,
                fontFamily = PixelFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
