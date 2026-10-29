package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.GameUIState
import com.example.model.AbilityType
import com.example.ui.theme.*

@Composable
fun WaveRewardScreen(
    state: GameUIState,
    onSelectFreeAbility: (AbilityType) -> Unit,
    onBuyAbility: (AbilityType) -> Unit,
    onResumeWave: () -> Unit
) {
    // Stage 1: Free Ability Selection (if choices exist)
    // Stage 2: Ability Shop
    val isFreeSelectionStage = state.freeAbilitiesToChoose.isNotEmpty()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xEE0A0B12))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "WAVE ${state.currentWave} CLEARED!",
                        color = DesiToxicGreen,
                        fontSize = 16.sp,
                        fontFamily = PixelFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isFreeSelectionStage) "CHOOSE 1 FREE ABILITY" else "DESI ABILITY SHOP",
                        color = DesiChaiGold,
                        fontSize = 10.sp,
                        fontFamily = PixelFontFamily
                    )
                }

                // Run Score / Points balance
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1B1D2C), RoundedCornerShape(8.dp))
                        .border(BorderStroke(1.5.dp, DesiChaiGold), RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "RUN POINTS: ${state.currentScore}",
                        color = DesiChaiGold,
                        fontSize = 11.sp,
                        fontFamily = PixelFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // STAGE 1: 3 FREE CARDS
            if (isFreeSelectionStage) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(state.freeAbilitiesToChoose) { ability ->
                        FreeAbilityCard(
                            ability = ability,
                            currentRank = state.ownedAbilities[ability] ?: 0,
                            onPick = { onSelectFreeAbility(ability) }
                        )
                    }
                }
            } else {
                // STAGE 2: SHOP BROWSING
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(AbilityType.values().toList()) { ability ->
                        val rank = state.ownedAbilities[ability] ?: 0
                        val canAfford = state.currentScore >= ability.cost

                        ShopAbilityCard(
                            ability = ability,
                            currentRank = rank,
                            canAfford = canAfford,
                            onBuy = { onBuyAbility(ability) }
                        )
                    }
                }
            }

            // FOOTER ACTION
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isFreeSelectionStage) {
                    Button(
                        onClick = onResumeWave,
                        colors = ButtonDefaults.buttonColors(containerColor = DesiToxicGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("resume_wave_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "START WAVE ${state.currentWave + 1}",
                                color = DesiStreetNight,
                                fontSize = 11.sp,
                                fontFamily = PixelFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Next Wave",
                                tint = DesiStreetNight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Tap any card above to claim your FREE upgrade!",
                        color = DesiTextDim,
                        fontSize = 9.sp,
                        fontFamily = PixelFontFamily
                    )
                }
            }
        }
    }
}

@Composable
fun FreeAbilityCard(
    ability: AbilityType,
    currentRank: Int,
    onPick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(175.dp)
            .clickable { onPick() }
            .testTag("free_card_${ability.name}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1D2C)),
        border = BorderStroke(2.dp, DesiToxicGreen)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .background(DesiToxicGreen, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "FREE REWARD",
                    color = DesiStreetNight,
                    fontSize = 8.sp,
                    fontFamily = PixelFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = ability.title,
                    color = DesiChaiGold,
                    fontSize = 11.sp,
                    fontFamily = PixelFontFamily,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = ability.description,
                    color = DesiTextBright,
                    fontSize = 9.sp,
                    fontFamily = PixelFontFamily,
                    textAlign = TextAlign.Center
                )
            }

            Text(
                text = "TAP TO CLAIM (LVL ${currentRank + 1})",
                color = DesiToxicGreen,
                fontSize = 8.sp,
                fontFamily = PixelFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ShopAbilityCard(
    ability: AbilityType,
    currentRank: Int,
    canAfford: Boolean,
    onBuy: () -> Unit
) {
    val borderColor = if (canAfford) DesiChaiGold else Color(0xFF333852)
    val btnColor = if (canAfford) DesiChaiGold else Color(0xFF232738)

    Card(
        modifier = Modifier
            .width(185.dp)
            .height(175.dp)
            .testTag("shop_card_${ability.name}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171926)),
        border = BorderStroke(1.5.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LVL $currentRank",
                    color = DesiCyan,
                    fontSize = 8.sp,
                    fontFamily = PixelFontFamily
                )
                Text(
                    text = "${ability.cost} PTS",
                    color = if (canAfford) DesiChaiGold else DesiBloodRed,
                    fontSize = 9.sp,
                    fontFamily = PixelFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = ability.title,
                    color = DesiTextBright,
                    fontSize = 10.sp,
                    fontFamily = PixelFontFamily,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = ability.description,
                    color = DesiTextDim,
                    fontSize = 8.sp,
                    fontFamily = PixelFontFamily,
                    textAlign = TextAlign.Center
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(btnColor, RoundedCornerShape(6.dp))
                    .clickable(enabled = canAfford) { onBuy() }
                    .padding(vertical = 8.dp)
                    .testTag("buy_${ability.name}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (canAfford) "UPGRADE" else "LOCKED",
                    color = if (canAfford) DesiStreetNight else DesiTextDim,
                    fontSize = 9.sp,
                    fontFamily = PixelFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
