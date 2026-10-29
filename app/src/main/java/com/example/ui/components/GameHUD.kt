package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.GameUIState
import com.example.ui.theme.*

@Composable
fun GameHUD(
    state: GameUIState,
    onReloadClick: () -> Unit,
    onSwitchWeaponClick: () -> Unit,
    onInteractClick: () -> Unit,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // TOP STATUS BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Wave Indicator
            Box(
                modifier = Modifier
                    .background(Color(0xCC0D0E15), RoundedCornerShape(6.dp))
                    .border(BorderStroke(1.dp, DesiCardBorder), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Column {
                    Text(
                        text = "WAVE ${state.currentWave}",
                        color = DesiChaiGold,
                        fontSize = 11.sp,
                        fontFamily = PixelFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "LEFT: ${state.zombiesRemaining}/${state.totalZombiesInWave}",
                        color = DesiToxicGreen,
                        fontSize = 9.sp,
                        fontFamily = PixelFontFamily
                    )
                }
            }

            // Center: Score & High Score
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "SCORE: ${state.currentScore}",
                    color = DesiTextBright,
                    fontSize = 13.sp,
                    fontFamily = PixelFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "BEST: ${state.highScore}",
                    color = DesiTextDim,
                    fontSize = 9.sp,
                    fontFamily = PixelFontFamily
                )
            }

            // Right: Pause Button
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Color(0xCC171926), RoundedCornerShape(8.dp))
                    .border(BorderStroke(1.5.dp, DesiCardBorder), RoundedCornerShape(8.dp))
                    .clickable { onPauseClick() }
                    .testTag("pause_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = "Pause",
                    tint = DesiChaiGold,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // BOTTOM-CENTER WEAPON & HEALTH CONTROLS
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // INTERACT BUTTON (Prompt when near pickup)
            AnimatedVisibility(
                visible = state.nearbyPickup != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                state.nearbyPickup?.let { pickup ->
                    val infiniteTransition = rememberInfiniteTransition(label = "interact_pulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 0.95f,
                        targetValue = 1.05f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "scale"
                    )

                    Box(
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                            .scale(pulseScale)
                            .background(Color(0xE600E5FF), RoundedCornerShape(8.dp))
                            .border(BorderStroke(2.dp, Color.White), RoundedCornerShape(8.dp))
                            .clickable { onInteractClick() }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("interact_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = "Interact",
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "INTERACT: ${pickup.promptText}",
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontFamily = PixelFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Health Bar with Player Avatar
            val healthPercent = (state.playerHp / state.playerMaxHp).coerceIn(0f, 1f)
            val hpColor = when {
                healthPercent > 0.5f -> DesiToxicGreen
                healthPercent > 0.25f -> DesiChaiGold
                else -> DesiBloodRed
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Character Avatar Portrait from concept sheet
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xDD171926))
                        .border(BorderStroke(1.5.dp, DesiChaiGold), RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.player_avatar),
                        contentDescription = "Player Avatar",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Box(
                    modifier = Modifier
                        .width(200.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xCC0D0E15))
                        .border(BorderStroke(1.5.dp, Color(0xFF2C304A)), RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(healthPercent)
                            .background(hpColor)
                    )
                    Text(
                        text = "HP: ${state.playerHp.toInt()} / ${state.playerMaxHp.toInt()}",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontFamily = PixelFontFamily,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Weapon & Action Bar
            Row(
                modifier = Modifier
                    .background(Color(0xDD171926), RoundedCornerShape(8.dp))
                    .border(BorderStroke(1.5.dp, DesiCardBorder), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Weapon Name & Ammo
                Column(modifier = Modifier.widthIn(min = 120.dp)) {
                    Text(
                        text = state.currentWeapon.displayName,
                        color = DesiChaiGold,
                        fontSize = 9.sp,
                        fontFamily = PixelFontFamily,
                        maxLines = 1
                    )
                    Text(
                        text = if (state.isReloading) "RELOADING..." else "${state.currentClipAmmo} / ${state.reserveAmmo}",
                        color = if (state.isReloading) DesiBloodRed else DesiToxicGreen,
                        fontSize = 11.sp,
                        fontFamily = PixelFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    if (state.isReloading) {
                        LinearProgressIndicator(
                            progress = { state.reloadProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .padding(top = 2.dp),
                            color = DesiBloodRed,
                            trackColor = Color(0x44FF2A2A)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Reload Button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(0xFF2A2D40), RoundedCornerShape(6.dp))
                        .border(BorderStroke(1.dp, DesiCardBorder), RoundedCornerShape(6.dp))
                        .clickable { onReloadClick() }
                        .testTag("reload_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reload",
                        tint = DesiTextBright,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Switch Weapon Button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(0xFF2A2D40), RoundedCornerShape(6.dp))
                        .border(BorderStroke(1.dp, DesiCardBorder), RoundedCornerShape(6.dp))
                        .clickable { onSwitchWeaponClick() }
                        .testTag("switch_weapon_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Switch Weapon",
                        tint = DesiCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
