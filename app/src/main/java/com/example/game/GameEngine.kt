package com.example.game

import android.content.Context
import android.os.SystemClock
import com.example.audio.GameAudio
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.*
import kotlin.random.Random

data class GameUIState(
    val playerHp: Float = 100f,
    val playerMaxHp: Float = 100f,
    val currentScore: Int = 0,
    val highScore: Int = 0,
    val currentWave: Int = 1,
    val zombiesRemaining: Int = 0,
    val totalZombiesInWave: Int = 0,
    val currentWeapon: WeaponType = WeaponType.PISTOL,
    val currentClipAmmo: Int = 12,
    val reserveAmmo: Int = 60,
    val isReloading: Boolean = false,
    val reloadProgress: Float = 0f,
    val nearbyPickup: Pickup? = null,
    val isWaveIntermission: Boolean = false,
    val freeAbilitiesToChoose: List<AbilityType> = emptyList(),
    val ownedAbilities: Map<AbilityType, Int> = emptyMap(),
    val isGameOver: Boolean = false,
    val isPaused: Boolean = false,
    val totalZombiesKilled: Int = 0
)

class GameEngine(
    private val context: Context,
    val audio: GameAudio
) {
    private val prefs = context.getSharedPreferences("desi_zombies_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(GameUIState())
    val uiState: StateFlow<GameUIState> = _uiState.asStateFlow()

    // High score persistence
    var highScore: Int = prefs.getInt("key_high_score", 0)
        private set

    // Player State
    var playerX = 1700f
    var playerY = 650f
    var playerVx = 0f
    var playerVy = 0f
    var playerFacingAngle = 0f // Radians
    var playerHp = 100f
    var playerMaxHp = 100f
    var isMoving = false
    var walkAnimTimer = 0f
    var walkFrame = 0
    var playerHurtTimer = 0f
    var playerShootTimer = 0f
    var playerIsDead = false

    // Weapons Inventory & Ammo
    val inventoryWeapons = mutableListOf(WeaponType.PISTOL)
    var currentWeaponIndex = 0
    val currentWeapon: WeaponType get() = inventoryWeapons[currentWeaponIndex]
    val weaponClipAmmo = mutableMapOf<WeaponType, Int>()
    val weaponReserveAmmo = mutableMapOf<WeaponType, Int>()
    var reloadTimerMs = 0L
    var lastShotTimeMs = 0L

    // Inputs from Joysticks
    var moveJoystickX = 0f
    var moveJoystickY = 0f
    var aimJoystickX = 0f
    var aimJoystickY = 0f

    // Camera
    var cameraX = 1700f
    var cameraY = 650f

    // Game Entities
    val zombies = mutableListOf<Zombie>()
    val bullets = mutableListOf<Bullet>()
    val pickups = mutableListOf<Pickup>()
    val particles = mutableListOf<Particle>()
    val floatingTexts = mutableListOf<FloatingText>()

    // Wave Progression
    var currentWave = 1
    var waveZombieQuota = 8
    var waveZombiesSpawned = 0
    var waveZombiesKilled = 0
    var nextSpawnCooldown = 1.0f
    var totalKills = 0
    var currentScore = 0

    // Ability Perks & Multipliers
    val ownedAbilities = mutableMapOf<AbilityType, Int>()
    var damageMultiplier = 1.0f
    var fireRateMultiplier = 1.0f
    var reloadSpeedMultiplier = 1.0f
    var speedMultiplier = 1.0f
    var medkitMultiplier = 1.0f
    var ammoMultiplier = 1.0f
    var defenseMultiplier = 1.0f
    var healthRegenPerSec = 0.5f
    var scoreMultiplier = 1.0f

    // State flags
    var isPaused = false
    var isWaveIntermission = false
    var freeAbilityChoices = listOf<AbilityType>()

    private var nextEntityId = 1L

    init {
        initWeapons()
        startNewRun()
    }

    private fun initWeapons() {
        weaponClipAmmo[WeaponType.PISTOL] = WeaponType.PISTOL.magCapacity
        weaponReserveAmmo[WeaponType.PISTOL] = 60

        weaponClipAmmo[WeaponType.SMG] = WeaponType.SMG.magCapacity
        weaponReserveAmmo[WeaponType.SMG] = 120

        weaponClipAmmo[WeaponType.SHOTGUN] = WeaponType.SHOTGUN.magCapacity
        weaponReserveAmmo[WeaponType.SHOTGUN] = 30

        weaponClipAmmo[WeaponType.ASSAULT_RIFLE] = WeaponType.ASSAULT_RIFLE.magCapacity
        weaponReserveAmmo[WeaponType.ASSAULT_RIFLE] = 90
    }

    fun startNewRun() {
        // Reset player
        playerX = 1700f
        playerY = 650f
        cameraX = 1700f
        cameraY = 650f
        playerVx = 0f
        playerVy = 0f
        playerFacingAngle = 0f
        playerMaxHp = 100f
        playerHp = playerMaxHp
        playerIsDead = false
        playerHurtTimer = 0f
        playerShootTimer = 0f

        // Reset inventory
        inventoryWeapons.clear()
        inventoryWeapons.add(WeaponType.PISTOL)
        currentWeaponIndex = 0
        initWeapons()

        // Reset entities
        zombies.clear()
        bullets.clear()
        pickups.clear()
        particles.clear()
        floatingTexts.clear()

        // Seed initial thematic pickups at key city landmarks
        spawnLandmarkPickups()

        // Reset abilities
        ownedAbilities.clear()
        recalculateAbilityMultipliers()

        // Reset wave and scoring
        currentWave = 1
        currentScore = 0
        totalKills = 0
        isPaused = false
        isWaveIntermission = false
        setupWave(currentWave)

        updateUIState()
    }

    private fun spawnLandmarkPickups() {
        // Hospital emergency triage: First Aid Medkit
        pickups.add(Pickup(nextEntityId++, PickupType.MEDKIT, 240f, 530f, 60, null, "Hospital First Aid"))
        // Chai Tapri: Cutting Chai stamina & speed
        pickups.add(Pickup(nextEntityId++, PickupType.MEDKIT, 340f, 990f, 40, null, "Cutting Chai Energizer"))
        // General Store: Surplus 9mm ammunition
        pickups.add(Pickup(nextEntityId++, PickupType.AMMO, 780f, 1080f, 60, null, "General Store Ammo"))
        // Medical Pharmacy: Clinical Medkit
        pickups.add(Pickup(nextEntityId++, PickupType.MEDKIT, 1010f, 1080f, 50, null, "Pharmacy Medkit"))
        // Indian Oil Station: Rapid SMG
        pickups.add(Pickup(nextEntityId++, PickupType.WEAPON_SMG, 1380f, 960f, 90, WeaponType.SMG, "Indian Oil SMG"))
        // Railway Station: Heavy 12-Gauge Shotgun
        pickups.add(Pickup(nextEntityId++, PickupType.WEAPON_SHOTGUN, 420f, 1500f, 24, WeaponType.SHOTGUN, "Railway Shotgun"))
        // School Playground: Assault Rifle cache
        pickups.add(Pickup(nextEntityId++, PickupType.WEAPON_RIFLE, 930f, 1560f, 60, WeaponType.ASSAULT_RIFLE, "School Yard AK-Rifle"))
        // Temple Sanctum: Sacred Blessing Prasad
        pickups.add(Pickup(nextEntityId++, PickupType.AMMO, 1850f, 520f, 80, null, "Temple Ammo Blessing"))
    }

    private fun recalculateAbilityMultipliers() {
        val dmgRank = ownedAbilities[AbilityType.BULLET_POWER] ?: 0
        damageMultiplier = 1.0f + (dmgRank * 0.25f)

        val fireRank = ownedAbilities[AbilityType.RAPID_TRIGGER] ?: 0
        fireRateMultiplier = 1.0f / (1.0f + (fireRank * 0.30f))

        val reloadRank = ownedAbilities[AbilityType.QUICK_RELOAD] ?: 0
        reloadSpeedMultiplier = 1.0f / (1.0f + (reloadRank * 0.40f))

        val speedRank = ownedAbilities[AbilityType.CHAI_SPEED] ?: 0
        speedMultiplier = 1.0f + (speedRank * 0.20f)

        val medkitRank = ownedAbilities[AbilityType.AYURVEDIC_MEDS] ?: 0
        medkitMultiplier = 1.0f + (medkitRank * 0.50f)

        val ammoRank = ownedAbilities[AbilityType.AMMO_SCAVENGER] ?: 0
        ammoMultiplier = 1.0f + (ammoRank * 0.50f)

        val defRank = ownedAbilities[AbilityType.BULLETPROOF] ?: 0
        defenseMultiplier = (0.80f).pow(defRank.toFloat())

        val regenRank = ownedAbilities[AbilityType.YOGA_REGEN] ?: 0
        healthRegenPerSec = 0.5f + (regenRank * 1.5f)

        val scoreRank = ownedAbilities[AbilityType.BOUNTY_HUNTER] ?: 0
        scoreMultiplier = 1.0f + (scoreRank * 0.35f)
    }

    private fun setupWave(wave: Int) {
        waveZombiesSpawned = 0
        waveZombiesKilled = 0
        // Wave 1 starts with 8, increases predictably
        waveZombieQuota = 6 + (wave * 3)
        nextSpawnCooldown = 0.5f
    }

    /**
     * Core update loop called every frame with delta seconds (e.g. ~0.016s).
     */
    fun update(dt: Float) {
        if (isPaused || isWaveIntermission || playerIsDead) return

        val clampedDt = dt.coerceIn(0.001f, 0.05f)

        updatePlayer(clampedDt)
        updateBullets(clampedDt)
        updateZombies(clampedDt)
        updatePickups()
        updateParticles(clampedDt)
        updateFloatingTexts(clampedDt)
        checkWaveProgression(clampedDt)
        updateCamera(clampedDt)

        updateUIState()
    }

    private fun updatePlayer(dt: Float) {
        // Passive Health Regeneration (capped at maxHealth)
        if (playerHp > 0 && playerHp < playerMaxHp) {
            playerHp = min(playerMaxHp, playerHp + (healthRegenPerSec * dt))
        }

        // Joystick movement: Left stick
        val moveMagnitude = hypot(moveJoystickX, moveJoystickY)
        if (moveMagnitude > 0.15f) {
            isMoving = true
            // Normalize direction so diagonal movement is not faster!
            val normX = moveJoystickX / moveMagnitude
            val normY = moveJoystickY / moveMagnitude
            val moveSpeed = 240f * speedMultiplier

            val targetX = playerX + (normX * moveSpeed * dt)
            val targetY = playerY + (normY * moveSpeed * dt)

            // Resolve collision using GameMap separate-axis sliding
            val resolved = GameMap.resolveMovement(playerX, playerY, targetX, targetY, radius = 24f)
            playerX = resolved.first
            playerY = resolved.second

            // Cycle walk frames
            walkAnimTimer += dt * 8f
            walkFrame = (walkAnimTimer.toInt()) % 2
        } else {
            isMoving = false
            walkFrame = 0
        }

        // Aiming & Facing direction
        val aimMagnitude = hypot(aimJoystickX, aimJoystickY)
        if (aimMagnitude > 0.25f) {
            playerFacingAngle = atan2(aimJoystickY, aimJoystickX)
            // Auto-fire while aiming!
            tryShootCurrentWeapon()
        } else if (moveMagnitude > 0.2f) {
            playerFacingAngle = atan2(moveJoystickY, moveJoystickX)
        }

        // Timers
        if (playerHurtTimer > 0f) playerHurtTimer -= dt
        if (playerShootTimer > 0f) playerShootTimer -= dt

        // Reload Timer update
        if (reloadTimerMs > 0L) {
            reloadTimerMs -= (dt * 1000f).toLong()
            if (reloadTimerMs <= 0L) {
                finishReload()
            }
        }
    }

    private fun tryShootCurrentWeapon() {
        if (reloadTimerMs > 0L) return

        val now = SystemClock.uptimeMillis()
        val effectiveFireRate = (currentWeapon.fireRateMs * fireRateMultiplier).toLong()
        if (now - lastShotTimeMs < effectiveFireRate) return

        val clip = weaponClipAmmo[currentWeapon] ?: 0
        if (clip <= 0) {
            // Auto reload when out of ammo
            startReload()
            return
        }

        // Consume ammo
        weaponClipAmmo[currentWeapon] = clip - 1
        lastShotTimeMs = now
        playerShootTimer = 0.12f

        // Play SFX
        audio.playSfx(currentWeapon.sfxKey, 0.8f)

        // Spawn bullet projectile(s)
        val pellets = currentWeapon.pelletCount
        val spread = currentWeapon.spreadAngle
        val baseAngle = playerFacingAngle

        for (i in 0 until pellets) {
            val angleOffset = if (pellets > 1) {
                (Random.nextFloat() - 0.5f) * spread * 2f
            } else {
                (Random.nextFloat() - 0.5f) * spread
            }
            val finalAngle = baseAngle + angleOffset
            val speed = currentWeapon.bulletSpeed * 60f // units per second
            val vx = cos(finalAngle) * speed
            val vy = sin(finalAngle) * speed

            val spawnX = playerX + cos(baseAngle) * 32f
            val spawnY = playerY + sin(baseAngle) * 32f

            val bulletDamage = currentWeapon.baseDamage * damageMultiplier

            bullets.add(
                Bullet(
                    x = spawnX,
                    y = spawnY,
                    vx = vx,
                    vy = vy,
                    damage = bulletDamage,
                    distanceRemaining = currentWeapon.range,
                    weaponType = currentWeapon
                )
            )
        }

        // Muzzle particles
        for (p in 0..2) {
            particles.add(
                Particle(
                    x = playerX + cos(baseAngle) * 34f,
                    y = playerY + sin(baseAngle) * 34f,
                    vx = cos(baseAngle) * 120f + Random.nextFloat() * 40f - 20f,
                    vy = sin(baseAngle) * 120f + Random.nextFloat() * 40f - 20f,
                    color = 0xFFFFEE55,
                    alpha = 1.0f,
                    size = 5f,
                    life = 0f,
                    maxLife = 0.15f
                )
            )
        }
    }

    fun startReload() {
        if (reloadTimerMs > 0L) return
        val currentClip = weaponClipAmmo[currentWeapon] ?: 0
        val maxClip = currentWeapon.magCapacity
        val reserve = weaponReserveAmmo[currentWeapon] ?: 0

        if (currentClip >= maxClip || reserve <= 0) return

        reloadTimerMs = (currentWeapon.reloadTimeMs * reloadSpeedMultiplier).toLong()
        audio.playSfx("sfx_reload", 0.7f)
    }

    private fun finishReload() {
        val currentClip = weaponClipAmmo[currentWeapon] ?: 0
        val maxClip = currentWeapon.magCapacity
        val reserve = weaponReserveAmmo[currentWeapon] ?: 0
        val needed = maxClip - currentClip
        val toTake = min(needed, reserve)

        weaponClipAmmo[currentWeapon] = currentClip + toTake
        weaponReserveAmmo[currentWeapon] = reserve - toTake
        reloadTimerMs = 0L
    }

    fun switchWeapon() {
        if (inventoryWeapons.size <= 1) return
        currentWeaponIndex = (currentWeaponIndex + 1) % inventoryWeapons.size
        reloadTimerMs = 0L // cancel active reload on switch
        audio.playSfx("sfx_pickup", 0.5f)
        updateUIState()
    }

    private fun updateBullets(dt: Float) {
        val iterator = bullets.iterator()
        while (iterator.hasNext()) {
            val b = iterator.next()
            val stepDist = hypot(b.vx * dt, b.vy * dt)
            b.x += b.vx * dt
            b.y += b.vy * dt
            b.distanceRemaining -= stepDist

            // Check obstacle collision
            if (GameMap.isPointBlocked(b.x, b.y) || b.distanceRemaining <= 0f) {
                // Spark particle
                particles.add(
                    Particle(
                        x = b.x,
                        y = b.y,
                        vx = (Random.nextFloat() - 0.5f) * 60f,
                        vy = (Random.nextFloat() - 0.5f) * 60f,
                        color = 0xFFFFCC00,
                        alpha = 1.0f,
                        size = 4f,
                        life = 0f,
                        maxLife = 0.2f
                    )
                )
                iterator.remove()
                continue
            }

            // Check zombie collision
            var hitZombie = false
            for (z in zombies) {
                if (z.state == ZombieState.DYING || z.state == ZombieState.DEAD) continue
                val dx = b.x - z.x
                val dy = b.y - z.y
                val distSq = dx * dx + dy * dy
                val hitRadius = 32f

                if (distSq < hitRadius * hitRadius) {
                    hitZombie = true
                    damageZombie(z, b.damage)
                    // Blood-free impact sparks and thud SFX
                    audio.playSfx("sfx_hit", 0.4f)
                    for (k in 0..3) {
                        particles.add(
                            Particle(
                                x = z.x,
                                y = z.y,
                                vx = (Random.nextFloat() - 0.5f) * 90f,
                                vy = (Random.nextFloat() - 0.5f) * 90f,
                                color = 0xFFFF7043,
                                alpha = 1.0f,
                                size = 5f,
                                life = 0f,
                                maxLife = 0.25f
                            )
                        )
                    }
                    break
                }
            }

            if (hitZombie) {
                iterator.remove()
            }
        }
    }

    private fun damageZombie(zombie: Zombie, damage: Float) {
        zombie.hp -= damage
        zombie.hurtTimer = 0.15f

        // Floating damage number
        floatingTexts.add(
            FloatingText(
                text = "-${damage.toInt()}",
                x = zombie.x + (Random.nextFloat() - 0.5f) * 16f,
                y = zombie.y - 20f,
                color = 0xFFFFEE55,
                life = 0f,
                maxLife = 0.7f
            )
        )

        if (zombie.hp <= 0f && zombie.state != ZombieState.DYING && zombie.state != ZombieState.DEAD) {
            killZombie(zombie)
        }
    }

    private fun killZombie(zombie: Zombie) {
        zombie.state = ZombieState.DYING
        zombie.deathTimer = 0.6f

        // Award points EXACTLY ONCE
        val earnedScore = (zombie.type.pointReward * scoreMultiplier).toInt()
        currentScore += earnedScore
        totalKills++
        waveZombiesKilled++

        if (currentScore > highScore) {
            highScore = currentScore
            prefs.edit().putInt("key_high_score", highScore).apply()
        }

        // Float score text
        floatingTexts.add(
            FloatingText(
                text = "+$earnedScore",
                x = zombie.x,
                y = zombie.y - 36f,
                color = 0xFF00E676,
                life = 0f,
                maxLife = 0.9f
            )
        )

        // TRIGGER MEME DEATH SOUND EXACTLY ONCE PER DEATH
        if (!zombie.deathSoundPlayed) {
            zombie.deathSoundPlayed = true
            audio.playZombieDeathSound(zombie.type)
        }

        // Drop pickups
        spawnPickupAt(zombie.x, zombie.y)

        // Death particle burst
        for (i in 0..8) {
            particles.add(
                Particle(
                    x = zombie.x,
                    y = zombie.y,
                    vx = (Random.nextFloat() - 0.5f) * 140f,
                    vy = (Random.nextFloat() - 0.5f) * 140f,
                    color = 0xFF00E676,
                    alpha = 1.0f,
                    size = 6f,
                    life = 0f,
                    maxLife = 0.5f
                )
            )
        }
    }

    private fun spawnPickupAt(x: Float, y: Float) {
        val roll = Random.nextFloat()
        val pickup = when {
            // 35% ammo drop
            roll < 0.35f -> {
                val amt = (25 * ammoMultiplier).toInt()
                Pickup(
                    id = nextEntityId++,
                    type = PickupType.AMMO,
                    x = x,
                    y = y,
                    amount = amt,
                    promptText = "AMMO (+$amt)"
                )
            }
            // 15% medkit drop
            roll < 0.50f -> {
                val healAmt = (35 * medkitMultiplier).toInt()
                Pickup(
                    id = nextEntityId++,
                    type = PickupType.MEDKIT,
                    x = x,
                    y = y,
                    amount = healAmt,
                    promptText = "MEDKIT (+$healAmt HP)"
                )
            }
            // 6% weapon drop (if not already acquired)
            roll < 0.56f -> {
                val unowned = WeaponType.values().filter { it !in inventoryWeapons }
                if (unowned.isNotEmpty()) {
                    val w = unowned.random()
                    val pType = when (w) {
                        WeaponType.SMG -> PickupType.WEAPON_SMG
                        WeaponType.SHOTGUN -> PickupType.WEAPON_SHOTGUN
                        WeaponType.ASSAULT_RIFLE -> PickupType.WEAPON_RIFLE
                        else -> null
                    }
                    if (pType != null) {
                        Pickup(
                            id = nextEntityId++,
                            type = pType,
                            x = x,
                            y = y,
                            weaponType = w,
                            promptText = "NEW: ${w.displayName}"
                        )
                    } else null
                } else null
            }
            else -> null
        }

        if (pickup != null) {
            pickups.add(pickup)
        }
    }

    private fun updateZombies(dt: Float) {
        // Spawn queue
        nextSpawnCooldown -= dt
        if (nextSpawnCooldown <= 0f && waveZombiesSpawned < waveZombieQuota) {
            spawnNextZombie()
            nextSpawnCooldown = max(0.8f, 2.2f - (currentWave * 0.15f))
        }

        val zIterator = zombies.iterator()
        while (zIterator.hasNext()) {
            val z = zIterator.next()

            if (z.state == ZombieState.DYING) {
                z.deathTimer -= dt
                if (z.deathTimer <= 0f) {
                    z.state = ZombieState.DEAD
                    zIterator.remove()
                    continue
                }
            }

            if (z.state == ZombieState.DEAD) {
                zIterator.remove()
                continue
            }

            if (z.hurtTimer > 0f) z.hurtTimer -= dt
            if (z.attackCooldown > 0f) z.attackCooldown -= dt

            // AI Movement towards player
            val dx = playerX - z.x
            val dy = playerY - z.y
            val distToPlayer = hypot(dx, dy)
            z.facingLeft = dx < 0

            // Separation force from other zombies to prevent stacking
            var sepX = 0f
            var sepY = 0f
            for (other in zombies) {
                if (other.id == z.id || other.state == ZombieState.DYING) continue
                val odx = z.x - other.x
                val ody = z.y - other.y
                val odistSq = odx * odx + ody * ody
                if (odistSq in 1.0f..1600f) { // Within 40 units
                    val odist = sqrt(odistSq)
                    sepX += (odx / odist) * (40f - odist) * 2.5f
                    sepY += (ody / odist) * (40f - odist) * 2.5f
                }
            }

            if (distToPlayer > 36f) {
                // Move towards player
                val dirX = (dx / distToPlayer)
                val dirY = (dy / distToPlayer)
                val currentSpeed = z.speed * 60f

                val targetX = z.x + (dirX * currentSpeed + sepX) * dt
                val targetY = z.y + (dirY * currentSpeed + sepY) * dt

                // Separate axis movement resolution to slide smoothly around walls
                val resolved = GameMap.resolveMovement(z.x, z.y, targetX, targetY, radius = 22f)
                z.x = resolved.first
                z.y = resolved.second

                z.animTimer += dt * 6f
                z.animFrame = (z.animTimer.toInt()) % 2
            } else {
                // In melee range! Attack with cooldown!
                if (z.attackCooldown <= 0f) {
                    z.attackCooldown = 1.0f // 1 second cooldown
                    damagePlayer(z.damage)
                }
            }
        }
    }

    private fun spawnNextZombie() {
        val sp = GameMap.spawnPoints.random()
        // Eligible zombie types based on wave progression:
        // Wave 1: MODI, ACP
        // Wave 2: MODI, ACP, AMITABH, RAVI
        // Wave 3+: ALL 6 TYPES (MODI, ACP, AMITABH, RAVI, SALMAN, SRK)
        val eligibleTypes = when {
            currentWave == 1 -> listOf(ZombieType.MODI, ZombieType.ACP)
            currentWave == 2 -> listOf(ZombieType.MODI, ZombieType.ACP, ZombieType.AMITABH, ZombieType.RAVI)
            else -> ZombieType.values().toList()
        }

        val type = eligibleTypes.random()
        val waveScalingHp = type.baseHp * (1f + (currentWave - 1) * 0.12f)
        val waveScalingSpeed = type.baseSpeed * (1f + min(0.4f, (currentWave - 1) * 0.04f))

        val zombie = Zombie(
            id = nextEntityId++,
            type = type,
            x = sp.first + (Random.nextFloat() - 0.5f) * 40f,
            y = sp.second + (Random.nextFloat() - 0.5f) * 40f,
            hp = waveScalingHp,
            maxHp = waveScalingHp,
            speed = waveScalingSpeed,
            damage = type.attackDamage
        )

        zombies.add(zombie)
        waveZombiesSpawned++
    }

    private fun damagePlayer(amount: Float) {
        if (playerIsDead) return
        val finalDamage = amount * defenseMultiplier
        playerHp = max(0f, playerHp - finalDamage)
        playerHurtTimer = 0.25f

        audio.playSfx("sfx_player_hurt", 0.8f)

        floatingTexts.add(
            FloatingText(
                text = "-${finalDamage.toInt()} HP",
                x = playerX,
                y = playerY - 30f,
                color = 0xFFFF2A2A,
                life = 0f,
                maxLife = 0.8f
            )
        )

        if (playerHp <= 0f) {
            handlePlayerDeath()
        }
    }

    private fun handlePlayerDeath() {
        playerIsDead = true
        audio.playSfx("sfx_player_hurt", 1.0f)
        audio.playPlayerDeath()
        updateUIState()
    }

    private fun updatePickups() {
        // Check closest pickup to player for manual interact prompt
        // Pickups are NOT automatically collected. Player must press INTERACT button!
        var closest: Pickup? = null
        var closestDistSq = Float.MAX_VALUE

        for (p in pickups) {
            val dx = playerX - p.x
            val dy = playerY - p.y
            val dSq = dx * dx + dy * dy
            if (dSq < 64f * 64f) { // Within 64 units interaction radius
                if (dSq < closestDistSq) {
                    closestDistSq = dSq
                    closest = p
                }
            }
        }
        _uiState.value = _uiState.value.copy(nearbyPickup = closest)
    }

    /**
     * Called when player presses the INTERACT button on the HUD.
     */
    fun interactWithNearbyPickup() {
        val pickup = _uiState.value.nearbyPickup ?: return
        when (pickup.type) {
            PickupType.AMMO -> {
                val res = weaponReserveAmmo[currentWeapon] ?: 0
                weaponReserveAmmo[currentWeapon] = res + pickup.amount
                audio.playSfx("sfx_pickup", 0.8f)
                floatingTexts.add(
                    FloatingText(
                        text = "+${pickup.amount} AMMO",
                        x = playerX,
                        y = playerY - 30f,
                        color = 0xFF00E676,
                        life = 0f,
                        maxLife = 0.8f
                    )
                )
            }
            PickupType.MEDKIT -> {
                playerHp = min(playerMaxHp, playerHp + pickup.amount)
                audio.playSfx("sfx_medkit", 0.8f)
                floatingTexts.add(
                    FloatingText(
                        text = "+${pickup.amount} HP",
                        x = playerX,
                        y = playerY - 30f,
                        color = 0xFF00E5FF,
                        life = 0f,
                        maxLife = 0.8f
                    )
                )
            }
            PickupType.WEAPON_SMG, PickupType.WEAPON_SHOTGUN, PickupType.WEAPON_RIFLE -> {
                pickup.weaponType?.let { w ->
                    if (w !in inventoryWeapons) {
                        inventoryWeapons.add(w)
                        currentWeaponIndex = inventoryWeapons.indexOf(w)
                    }
                    audio.playSfx("sfx_pickup", 0.9f)
                    floatingTexts.add(
                        FloatingText(
                            text = "EQUIPPED ${w.displayName}!",
                            x = playerX,
                            y = playerY - 35f,
                            color = 0xFFFFD700,
                            life = 0f,
                            maxLife = 1.0f
                        )
                    )
                }
            }
        }
        pickups.remove(pickup)
        updatePickups()
        updateUIState()
    }

    private fun checkWaveProgression(dt: Float) {
        if (waveZombiesSpawned >= waveZombieQuota && zombies.isEmpty() && !isWaveIntermission) {
            // Wave cleared!
            onWaveCompleted()
        }
    }

    private fun onWaveCompleted() {
        isWaveIntermission = true
        audio.playSfx("sfx_wave_complete", 1.0f)

        // Select 3 random abilities for free choice
        val allAbilities = AbilityType.values().toList()
        freeAbilityChoices = allAbilities.shuffled().take(3)

        updateUIState()
    }

    /**
     * Called when user picks one of the 3 free ability cards.
     */
    fun selectFreeAbility(ability: AbilityType) {
        val currentRank = ownedAbilities[ability] ?: 0
        ownedAbilities[ability] = currentRank + 1
        if (ability == AbilityType.MAX_HEALTH) {
            playerMaxHp += 50f
            playerHp = min(playerMaxHp, playerHp + 50f)
        }
        recalculateAbilityMultipliers()
        freeAbilityChoices = emptyList() // Free pick consumed
        updateUIState()
    }

    /**
     * Called when user buys an ability in the shop using run points.
     */
    fun buyAbility(ability: AbilityType): Boolean {
        if (currentScore < ability.cost) return false
        currentScore -= ability.cost
        val currentRank = ownedAbilities[ability] ?: 0
        ownedAbilities[ability] = currentRank + 1

        if (ability == AbilityType.MAX_HEALTH) {
            playerMaxHp += 50f
            playerHp = min(playerMaxHp, playerHp + 50f)
        }
        recalculateAbilityMultipliers()
        audio.playSfx("sfx_pickup", 0.7f)
        updateUIState()
        return true
    }

    /**
     * Resumes active combat for the next wave.
     */
    fun resumeNextWave() {
        isWaveIntermission = false
        currentWave++
        setupWave(currentWave)
        updateUIState()
    }

    var viewportHalfW = 350f
    var viewportHalfH = 220f

    fun setViewportDimensions(screenW: Float, screenH: Float, zoom: Float) {
        viewportHalfW = (screenW / 2f) / zoom
        viewportHalfH = (screenH / 2f) / zoom
    }

    private fun updateCamera(dt: Float) {
        // Smooth camera lerp following player
        val lerpFactor = 0.12f
        cameraX += (playerX - cameraX) * lerpFactor
        cameraY += (playerY - cameraY) * lerpFactor

        // Clamp camera to map boundaries based on zoomed viewport
        val minX = viewportHalfW + 40f
        val maxX = GameMap.MAP_WIDTH - viewportHalfW - 40f
        val minY = viewportHalfH + 40f
        val maxY = GameMap.MAP_HEIGHT - viewportHalfH - 40f

        cameraX = if (maxX >= minX) cameraX.coerceIn(minX, maxX) else GameMap.MAP_WIDTH / 2f
        cameraY = if (maxY >= minY) cameraY.coerceIn(minY, maxY) else GameMap.MAP_HEIGHT / 2f
    }

    private fun updateParticles(dt: Float) {
        val pIter = particles.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            p.life += dt
            if (p.life >= p.maxLife) {
                pIter.remove()
                continue
            }
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.alpha = max(0f, 1.0f - (p.life / p.maxLife))
        }
    }

    private fun updateFloatingTexts(dt: Float) {
        val fIter = floatingTexts.iterator()
        while (fIter.hasNext()) {
            val ft = fIter.next()
            ft.life += dt
            if (ft.life >= ft.maxLife) {
                fIter.remove()
                continue
            }
            ft.y -= 28f * dt
        }
    }

    fun togglePause() {
        isPaused = !isPaused
        if (isPaused) {
            audio.stopAll()
        } else {
            audio.resumeAll()
        }
        updateUIState()
    }

    fun setSoundEnabled(enabled: Boolean) {
        audio.isSoundEnabled = enabled
        prefs.edit().putBoolean("key_sound_enabled", enabled).apply()
    }

    fun isSoundEnabled(): Boolean {
        return audio.isSoundEnabled
    }

    private fun updateUIState() {
        val totalInWave = waveZombieQuota
        val remaining = max(0, waveZombieQuota - waveZombiesKilled)
        val reloadProg = if (reloadTimerMs > 0L) {
            val totalTime = currentWeapon.reloadTimeMs * reloadSpeedMultiplier
            1.0f - (reloadTimerMs.toFloat() / totalTime)
        } else 0f

        _uiState.value = GameUIState(
            playerHp = playerHp,
            playerMaxHp = playerMaxHp,
            currentScore = currentScore,
            highScore = highScore,
            currentWave = currentWave,
            zombiesRemaining = remaining,
            totalZombiesInWave = totalInWave,
            currentWeapon = currentWeapon,
            currentClipAmmo = weaponClipAmmo[currentWeapon] ?: 0,
            reserveAmmo = weaponReserveAmmo[currentWeapon] ?: 0,
            isReloading = reloadTimerMs > 0L,
            reloadProgress = reloadProg,
            nearbyPickup = _uiState.value.nearbyPickup,
            isWaveIntermission = isWaveIntermission,
            freeAbilitiesToChoose = freeAbilityChoices,
            ownedAbilities = ownedAbilities.toMap(),
            isGameOver = playerIsDead,
            isPaused = isPaused,
            totalZombiesKilled = totalKills
        )
    }
}
