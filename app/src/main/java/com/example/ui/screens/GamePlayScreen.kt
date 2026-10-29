package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.game.GameEngine
import com.example.game.GameMap
import com.example.model.*
import com.example.ui.components.GameHUD
import com.example.ui.components.VirtualJoystick
import com.example.ui.theme.DesiStreetNight
import kotlinx.coroutines.isActive
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GamePlayScreen(
    engine: GameEngine,
    onNavigateToMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by engine.uiState.collectAsState()

    // 60 FPS Frame ticker with withFrameNanos
    LaunchedEffect(Unit) {
        var lastTime = withFrameNanos { it }
        while (isActive) {
            val currentTime = withFrameNanos { it }
            val dt = (currentTime - lastTime) / 1_000_000_000f
            lastTime = currentTime
            engine.update(dt)
        }
    }

    // Load Bitmaps for hardware-accelerated Canvas rendering
    val gameMap = ImageBitmap.imageResource(id = R.drawable.game_map)

    // Player Directional Sprites (from Desi Survivor concept sheet)
    val playerIdleDown = ImageBitmap.imageResource(id = R.drawable.player_idle_down)
    val playerIdleUp = ImageBitmap.imageResource(id = R.drawable.player_idle_up)
    val playerIdleLeft = ImageBitmap.imageResource(id = R.drawable.player_idle_left)
    val playerIdleRight = ImageBitmap.imageResource(id = R.drawable.player_idle_right)

    val playerRunDown = ImageBitmap.imageResource(id = R.drawable.player_run_down)
    val playerRunUp = ImageBitmap.imageResource(id = R.drawable.player_run_up)
    val playerRunLeft = ImageBitmap.imageResource(id = R.drawable.player_run_left)
    val playerRunRight = ImageBitmap.imageResource(id = R.drawable.player_run_right)

    val playerShootDown = ImageBitmap.imageResource(id = R.drawable.player_shoot_down)
    val playerShootUp = ImageBitmap.imageResource(id = R.drawable.player_shoot_up)
    val playerShootLeft = ImageBitmap.imageResource(id = R.drawable.player_shoot_left)
    val playerShootRight = ImageBitmap.imageResource(id = R.drawable.player_shoot_right)

    val playerHurt = ImageBitmap.imageResource(id = R.drawable.player_hurt)
    val playerDead = ImageBitmap.imageResource(id = R.drawable.player_dead)

    val zombieModi = ImageBitmap.imageResource(id = R.drawable.zombie_modi)
    val zombieAmitabh = ImageBitmap.imageResource(id = R.drawable.zombie_amitabh)
    val zombieAcp = ImageBitmap.imageResource(id = R.drawable.zombie_acp)
    val zombieSrk = ImageBitmap.imageResource(id = R.drawable.zombie_srk)
    val zombieSalman = ImageBitmap.imageResource(id = R.drawable.zombie_salman)
    val zombieRavi = ImageBitmap.imageResource(id = R.drawable.zombie_ravi)

    val zombieSprites = remember(zombieModi, zombieAmitabh, zombieAcp, zombieSrk, zombieSalman, zombieRavi) {
        mapOf(
            ZombieType.MODI to zombieModi,
            ZombieType.AMITABH to zombieAmitabh,
            ZombieType.ACP to zombieAcp,
            ZombieType.SRK to zombieSrk,
            ZombieType.SALMAN to zombieSalman,
            ZombieType.RAVI to zombieRavi
        )
    }

    val itemMedkit = ImageBitmap.imageResource(id = R.drawable.item_medkit)
    val itemAmmo = ImageBitmap.imageResource(id = R.drawable.item_ammo)
    val itemSmg = ImageBitmap.imageResource(id = R.drawable.item_smg)
    val itemShotgun = ImageBitmap.imageResource(id = R.drawable.item_shotgun)
    val itemRifle = ImageBitmap.imageResource(id = R.drawable.item_rifle)

    BoxWithConstraints(modifier = modifier.fillMaxSize().background(DesiStreetNight)) {
        val screenW = constraints.maxWidth.toFloat()
        val screenH = constraints.maxHeight.toFloat()

        // Dynamic viewport scale: substantially enlarges the game world & characters across all phone sizes
        val zoom = (screenH / 420f).coerceIn(1.75f, 2.5f)

        LaunchedEffect(screenW, screenH, zoom) {
            engine.setViewportDimensions(screenW, screenH, zoom)
        }

        // 2D CANVAS GAME VIEWPORT
        Canvas(modifier = Modifier.fillMaxSize()) {
            val camX = engine.cameraX
            val camY = engine.cameraY
            val originX = (screenW / 2f) / zoom - camX
            val originY = (screenH / 2f) / zoom - camY

            withTransform({
                scale(zoom, zoom, pivot = Offset.Zero)
            }) {
                // 1. Draw Native High-Res Pixel-Art City Map (Hospital, Gym, Chai Tapri, Petrol Station, Railway, School, Docks, Temple)
                drawImage(
                    image = gameMap,
                    dstOffset = IntOffset(originX.toInt(), originY.toInt()),
                    dstSize = IntSize(GameMap.MAP_WIDTH.toInt(), GameMap.MAP_HEIGHT.toInt())
                )

                // 2. Draw Pickups
                drawPickups(engine.pickups, originX, originY, itemMedkit, itemAmmo, itemSmg, itemShotgun, itemRifle)

                // 3. Draw Zombies
                drawZombies(engine.zombies, originX, originY, zombieSprites)

                // 4. Draw Desi Survivor Player Character with 12 Directional Sprites
                drawPlayer(
                    engine = engine,
                    originX = originX,
                    originY = originY,
                    playerIdleDown = playerIdleDown,
                    playerIdleUp = playerIdleUp,
                    playerIdleLeft = playerIdleLeft,
                    playerIdleRight = playerIdleRight,
                    playerRunDown = playerRunDown,
                    playerRunUp = playerRunUp,
                    playerRunLeft = playerRunLeft,
                    playerRunRight = playerRunRight,
                    playerShootDown = playerShootDown,
                    playerShootUp = playerShootUp,
                    playerShootLeft = playerShootLeft,
                    playerShootRight = playerShootRight,
                    playerHurt = playerHurt,
                    playerDead = playerDead
                )

                // 6. Draw Bullets
                drawBullets(engine.bullets, originX, originY)

                // 7. Draw Particles
                drawParticles(engine.particles, originX, originY)

                // 8. Draw Floating Numbers & Texts
                drawFloatingTexts(engine.floatingTexts, originX, originY)
            }
        }

        // HUD & CONTROLS OVERLAY
        GameHUD(
            state = uiState,
            onReloadClick = { engine.startReload() },
            onSwitchWeaponClick = { engine.switchWeapon() },
            onInteractClick = { engine.interactWithNearbyPickup() },
            onPauseClick = { engine.togglePause() }
        )

        // DUAL VIRTUAL JOYSTICKS
        // Left Joystick (Movement) - bottom-left corner with translucent base
        VirtualJoystick(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 14.dp),
            size = 106.dp,
            baseColor = Color(0x2200E676),
            knobColor = Color(0xFF00E676),
            isEnabled = !uiState.isPaused && !uiState.isGameOver && !uiState.isWaveIntermission,
            testTag = "move_joystick",
            onValueChange = { x, y ->
                engine.moveJoystickX = x
                engine.moveJoystickY = y
            }
        )

        // Right Joystick (Aiming & Auto-fire) - bottom-right corner with translucent base
        VirtualJoystick(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 14.dp),
            size = 106.dp,
            baseColor = Color(0x22FF2A2A),
            knobColor = Color(0xFFFF2A2A),
            isEnabled = !uiState.isPaused && !uiState.isGameOver && !uiState.isWaveIntermission,
            testTag = "aim_joystick",
            onValueChange = { x, y ->
                engine.aimJoystickX = x
                engine.aimJoystickY = y
            }
        )

        // OVERLAYS & MODALS
        // 1. Pause Dialog
        if (uiState.isPaused && !uiState.isGameOver && !uiState.isWaveIntermission) {
            PauseDialog(
                isSoundEnabled = engine.isSoundEnabled(),
                onResumeClick = { engine.togglePause() },
                onRestartClick = { engine.startNewRun() },
                onMainMenuClick = onNavigateToMenu,
                onToggleSoundClick = {
                    engine.setSoundEnabled(!engine.isSoundEnabled())
                }
            )
        }

        // 2. Wave Reward & Shop Modal
        if (uiState.isWaveIntermission && !uiState.isGameOver) {
            WaveRewardScreen(
                state = uiState,
                onSelectFreeAbility = { ability -> engine.selectFreeAbility(ability) },
                onBuyAbility = { ability -> engine.buyAbility(ability) },
                onResumeWave = { engine.resumeNextWave() }
            )
        }

        // 3. Game Over Screen
        if (uiState.isGameOver) {
            GameOverScreen(
                state = uiState,
                onRestartClick = { engine.startNewRun() },
                onMainMenuClick = onNavigateToMenu
            )
        }
    }
}

// RENDER HELPERS

private fun DrawScope.drawPickups(
    pickups: List<Pickup>,
    originX: Float,
    originY: Float,
    itemMedkit: ImageBitmap,
    itemAmmo: ImageBitmap,
    itemSmg: ImageBitmap,
    itemShotgun: ImageBitmap,
    itemRifle: ImageBitmap
) {
    for (p in pickups) {
        val screenX = originX + p.x
        val screenY = originY + p.y
        val img = when (p.type) {
            PickupType.MEDKIT -> itemMedkit
            PickupType.AMMO -> itemAmmo
            PickupType.WEAPON_SMG -> itemSmg
            PickupType.WEAPON_SHOTGUN -> itemShotgun
            PickupType.WEAPON_RIFLE -> itemRifle
        }

        // Glowing circle under pickup
        drawCircle(
            color = Color(0x6600E5FF),
            radius = 24.dp.toPx(),
            center = Offset(screenX, screenY)
        )

        drawImage(
            image = img,
            dstOffset = IntOffset((screenX - 22f).toInt(), (screenY - 22f).toInt()),
            dstSize = IntSize(44, 44)
        )
    }
}

private fun DrawScope.drawZombies(
    zombies: List<Zombie>,
    originX: Float,
    originY: Float,
    zombieSprites: Map<ZombieType, ImageBitmap>
) {
    for (z in zombies) {
        val screenX = originX + z.x
        val screenY = originY + z.y
        val sprite = zombieSprites[z.type] ?: continue

        val w = z.type.width
        val h = z.type.height

        withTransform({
            if (z.facingLeft) {
                scale(-1f, 1f, pivot = Offset(screenX, screenY))
            }
            if (z.state == ZombieState.DYING) {
                rotate(90f * (1f - z.deathTimer / 0.6f), pivot = Offset(screenX, screenY))
            }
        }) {
            drawImage(
                image = sprite,
                dstOffset = IntOffset((screenX - w / 2f).toInt(), (screenY - h / 2f).toInt()),
                dstSize = IntSize(w.toInt(), h.toInt())
            )

            // Red damage flash overlay
            if (z.hurtTimer > 0f) {
                drawRect(
                    color = Color(0x77FF0000),
                    topLeft = Offset(screenX - w / 2f, screenY - h / 2f),
                    size = Size(w, h)
                )
            }
        }

        // Health bar above head (shown when damaged)
        if (z.hp < z.maxHp && z.state != ZombieState.DYING) {
            val barW = 40f
            val barH = 5f
            val pct = (z.hp / z.maxHp).coerceIn(0f, 1f)
            val barX = screenX - barW / 2f
            val barY = screenY - h / 2f - 10f

            drawRect(
                color = Color.Black,
                topLeft = Offset(barX, barY),
                size = Size(barW, barH)
            )
            drawRect(
                color = Color(0xFFFF2A2A),
                topLeft = Offset(barX, barY),
                size = Size(barW * pct, barH)
            )
        }
    }
}

private fun DrawScope.drawPlayer(
    engine: GameEngine,
    originX: Float,
    originY: Float,
    playerIdleDown: ImageBitmap,
    playerIdleUp: ImageBitmap,
    playerIdleLeft: ImageBitmap,
    playerIdleRight: ImageBitmap,
    playerRunDown: ImageBitmap,
    playerRunUp: ImageBitmap,
    playerRunLeft: ImageBitmap,
    playerRunRight: ImageBitmap,
    playerShootDown: ImageBitmap,
    playerShootUp: ImageBitmap,
    playerShootLeft: ImageBitmap,
    playerShootRight: ImageBitmap,
    playerHurt: ImageBitmap,
    playerDead: ImageBitmap
) {
    val screenX = originX + engine.playerX
    val screenY = originY + engine.playerY

    val deg = Math.toDegrees(engine.playerFacingAngle.toDouble()).let { if (it < 0) it + 360 else it }
    val isShoot = engine.playerShootTimer > 0f
    val isMove = engine.isMoving
    val isDead = engine.playerIsDead
    val isHurt = engine.playerHurtTimer > 0f

    // Select crisp directional pixel-art sprite based on player aim/movement angle
    val sprite = when {
        isDead -> playerDead
        isHurt -> playerHurt
        isShoot -> when {
            deg in 45.0..135.0 -> playerShootDown
            deg in 135.0..225.0 -> playerShootLeft
            deg in 225.0..315.0 -> playerShootUp
            else -> playerShootRight
        }
        isMove -> when {
            deg in 45.0..135.0 -> playerRunDown
            deg in 135.0..225.0 -> playerRunLeft
            deg in 225.0..315.0 -> playerRunUp
            else -> playerRunRight
        }
        else -> when {
            deg in 45.0..135.0 -> playerIdleDown
            deg in 135.0..225.0 -> playerIdleLeft
            deg in 225.0..315.0 -> playerIdleUp
            else -> playerIdleRight
        }
    }

    val pw = 68f
    val ph = 80f

    drawImage(
        image = sprite,
        dstOffset = IntOffset((screenX - pw / 2f).toInt(), (screenY - ph / 2f).toInt()),
        dstSize = IntSize(pw.toInt(), ph.toInt())
    )

    // Damage flash overlay
    if (isHurt) {
        drawRect(
            color = Color(0x66FF0000),
            topLeft = Offset(screenX - pw / 2f, screenY - ph / 2f),
            size = Size(pw, ph)
        )
    }

    // Directional aim pointer
    val aimDist = 50f
    val pointerX = screenX + cos(engine.playerFacingAngle) * aimDist
    val pointerY = screenY + sin(engine.playerFacingAngle) * aimDist
    drawCircle(
        color = Color(0xCC00E5FF),
        radius = 3.dp.toPx(),
        center = Offset(pointerX, pointerY)
    )
}

private fun DrawScope.drawBullets(
    bullets: List<Bullet>,
    originX: Float,
    originY: Float
) {
    for (b in bullets) {
        val screenX = originX + b.x
        val screenY = originY + b.y

        // Bullet projectile
        drawCircle(
            color = Color(0xFFFFEB3B),
            radius = 3.5.dp.toPx(),
            center = Offset(screenX, screenY)
        )
        // Tracer tail
        val tailX = screenX - b.vx * 0.02f
        val tailY = screenY - b.vy * 0.02f
        drawLine(
            color = Color(0x88FF9800),
            start = Offset(screenX, screenY),
            end = Offset(tailX, tailY),
            strokeWidth = 2.dp.toPx()
        )
    }
}

private fun DrawScope.drawParticles(
    particles: List<Particle>,
    originX: Float,
    originY: Float
) {
    for (p in particles) {
        val screenX = originX + p.x
        val screenY = originY + p.y
        drawCircle(
            color = Color(p.color).copy(alpha = p.alpha),
            radius = p.size,
            center = Offset(screenX, screenY)
        )
    }
}

private fun DrawScope.drawFloatingTexts(
    floatingTexts: List<FloatingText>,
    originX: Float,
    originY: Float
) {
    val paint = android.graphics.Paint().apply {
        textSize = 16f
        isFakeBoldText = true
        textAlign = android.graphics.Paint.Align.CENTER
    }

    for (ft in floatingTexts) {
        val screenX = originX + ft.x
        val screenY = originY + ft.y
        val alphaInt = ((1f - ft.life / ft.maxLife) * 255).toInt().coerceIn(0, 255)
        paint.color = Color(ft.color).copy(alpha = alphaInt / 255f).hashCode()

        drawContext.canvas.nativeCanvas.drawText(
            ft.text,
            screenX,
            screenY,
            paint
        )
    }
}
