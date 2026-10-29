package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.ZombieType
import com.example.ui.theme.*

@Composable
fun HowToPlayDialog(
    onDismiss: () -> Unit,
    onTestAudio: ((ZombieType) -> Unit)? = null
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.9f)
                .testTag("how_to_play_dialog"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DesiSurfaceDark),
            border = BorderStroke(2.dp, DesiCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SURVIVAL MANUAL",
                        color = DesiChaiGold,
                        fontSize = 14.sp,
                        fontFamily = PixelFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF2A2D40), RoundedCornerShape(6.dp))
                            .clickable { onDismiss() }
                            .testTag("close_guide_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = DesiBloodRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // CONTROLS SECTION
                    item {
                        SectionHeader(title = "1. DUAL JOYSTICK CONTROLS")
                        Spacer(modifier = Modifier.height(6.dp))
                        ControlItem("LEFT JOYSTICK", "Move in all directions (normalized 360°).")
                        ControlItem("RIGHT JOYSTICK", "Aim & AUTO-FIRE while aiming in direction.")
                        ControlItem("RELOAD BUTTON", "Reload weapon when clip is running low.")
                        ControlItem("SWITCH BUTTON", "Switch between Pistol, SMG, Shotgun, Rifle.")
                        ControlItem("INTERACT BUTTON", "Manually pick up nearby Medkits, Ammo, and Guns.")
                    }

                    // WAVE & SHOP
                    item {
                        SectionHeader(title = "2. WAVES & ABILITY SHOP")
                        Spacer(modifier = Modifier.height(6.dp))
                        ControlItem("WAVE CLEAR", "Defeat all zombies in wave to pause combat.")
                        ControlItem("FREE ABILITY", "Pick 1 of 3 free ability cards every wave!")
                        ControlItem("RUN SHOP", "Spend earned points on upgrades. High score persists!")
                    }

                    // 6 MEME ZOMBIE PARODIES
                    item {
                        SectionHeader(title = "3. THE 6 DESI ZOMBIE PARODIES")
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    items(ZombieType.values().toList()) { zombie ->
                        ZombieRogueCard(zombie = zombie, onTestAudio = onTestAudio)
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF232738), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = title,
            color = DesiToxicGreen,
            fontSize = 10.sp,
            fontFamily = PixelFontFamily,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ControlItem(title: String, desc: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            text = "• $title: ",
            color = DesiChaiGold,
            fontSize = 9.sp,
            fontFamily = PixelFontFamily,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = desc,
            color = DesiTextBright,
            fontSize = 9.sp,
            fontFamily = PixelFontFamily
        )
    }
}

@Composable
fun ZombieRogueCard(zombie: ZombieType, onTestAudio: ((ZombieType) -> Unit)?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1B1D2C), RoundedCornerShape(8.dp))
            .border(BorderStroke(1.dp, DesiCardBorder), RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = zombie.portraitRes),
            contentDescription = zombie.characterName,
            modifier = Modifier
                .size(54.dp, 75.dp)
                .background(Color(0xFF0F101A), RoundedCornerShape(4.dp))
                .border(BorderStroke(1.dp, Color(0xFF333852)), RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = zombie.characterName,
                    color = DesiBloodRed,
                    fontSize = 11.sp,
                    fontFamily = PixelFontFamily,
                    fontWeight = FontWeight.Bold
                )

                if (onTestAudio != null) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF2A2E44), RoundedCornerShape(4.dp))
                            .border(BorderStroke(1.dp, DesiChaiGold), RoundedCornerShape(4.dp))
                            .clickable { onTestAudio(zombie) }
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                            .testTag("test_audio_${zombie.name}")
                    ) {
                        Text(
                            text = "▶ TEST AUDIO",
                            color = DesiChaiGold,
                            fontSize = 7.sp,
                            fontFamily = PixelFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Text(
                text = zombie.comedyTitle,
                color = DesiChaiGold,
                fontSize = 9.sp,
                fontFamily = PixelFontFamily
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "\"${zombie.funQuote}\"",
                color = DesiToxicGreen,
                fontSize = 8.sp,
                fontFamily = PixelFontFamily
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Audio: \"${zombie.memeFilename}\" • HP: ${zombie.baseHp.toInt()} • Dmg: ${zombie.attackDamage.toInt()}",
                color = DesiTextDim,
                fontSize = 8.sp,
                fontFamily = PixelFontFamily
            )
        }
    }
}
