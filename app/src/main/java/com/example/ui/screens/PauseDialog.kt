package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

@Composable
fun PauseDialog(
    isSoundEnabled: Boolean,
    onResumeClick: () -> Unit,
    onRestartClick: () -> Unit,
    onMainMenuClick: () -> Unit,
    onToggleSoundClick: () -> Unit
) {
    Dialog(onDismissRequest = onResumeClick) {
        Card(
            modifier = Modifier
                .width(340.dp)
                .testTag("pause_dialog"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DesiSurfaceDark),
            border = BorderStroke(2.dp, DesiCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "GAME PAUSED",
                    color = DesiChaiGold,
                    fontSize = 16.sp,
                    fontFamily = PixelFontFamily,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // RESUME
                PauseButton(
                    text = "RESUME",
                    icon = Icons.Default.PlayArrow,
                    bgColor = DesiToxicGreen,
                    contentColor = DesiStreetNight,
                    testTag = "resume_button",
                    onClick = onResumeClick
                )

                // RESTART RUN
                PauseButton(
                    text = "RESTART RUN",
                    icon = Icons.Default.Refresh,
                    bgColor = Color(0xFF262A3E),
                    borderColor = DesiBloodRed,
                    contentColor = DesiBloodRed,
                    testTag = "pause_restart_button",
                    onClick = onRestartClick
                )

                // SOUND ON/OFF
                PauseButton(
                    text = if (isSoundEnabled) "SOUND: ON" else "SOUND: OFF",
                    icon = if (isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    bgColor = Color(0xFF262A3E),
                    borderColor = DesiChaiGold,
                    contentColor = DesiChaiGold,
                    testTag = "pause_sound_button",
                    onClick = onToggleSoundClick
                )

                // MAIN MENU
                PauseButton(
                    text = "MAIN MENU",
                    icon = Icons.Default.Home,
                    bgColor = Color(0xFF262A3E),
                    borderColor = DesiCardBorder,
                    contentColor = DesiTextBright,
                    testTag = "pause_main_menu_button",
                    onClick = onMainMenuClick
                )
            }
        }
    }
}

@Composable
fun PauseButton(
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
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(8.dp))
            .then(
                if (borderColor != null) Modifier.border(
                    BorderStroke(1.5.dp, borderColor),
                    RoundedCornerShape(8.dp)
                ) else Modifier
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp)
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
                fontSize = 10.sp,
                fontFamily = PixelFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
