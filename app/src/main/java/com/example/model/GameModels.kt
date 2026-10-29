package com.example.model

import androidx.annotation.DrawableRes
import com.example.R

enum class ZombieType(
    val characterName: String,
    val comedyTitle: String,
    val memeFilename: String,
    val baseHp: Float,
    val baseSpeed: Float,
    val attackDamage: Float,
    val pointReward: Int,
    val funQuote: String,
    @DrawableRes val spriteRes: Int,
    @DrawableRes val portraitRes: Int,
    val width: Float = 56f,
    val height: Float = 76f
) {
    MODI(
        characterName = "Narendra Modi",
        comedyTitle = "Mitron Walker",
        memeFilename = "modi.mp3",
        baseHp = 130f,
        baseSpeed = 1.35f,
        attackDamage = 14f,
        pointReward = 100,
        funQuote = "Mitron! 56-inch resistance!",
        spriteRes = R.drawable.zombie_modi,
        portraitRes = R.drawable.portrait_modi
    ),
    AMITABH(
        characterName = "Amitabh Bachchan",
        comedyTitle = "Shahenshah Ghoul",
        memeFilename = "Amitabh.mp3",
        baseHp = 180f,
        baseSpeed = 1.15f,
        attackDamage = 22f,
        pointReward = 120,
        funQuote = "Rishte mein toh hum tumhare baap...",
        spriteRes = R.drawable.zombie_amitabh,
        portraitRes = R.drawable.portrait_amitabh
    ),
    ACP(
        characterName = "ACP Pradyuman",
        comedyTitle = "Daya Investigator",
        memeFilename = "ACP.mp3",
        baseHp = 140f,
        baseSpeed = 1.4f,
        attackDamage = 16f,
        pointReward = 90,
        funQuote = "Kuch toh gadbad hai, Daya!",
        spriteRes = R.drawable.zombie_acp,
        portraitRes = R.drawable.portrait_acp
    ),
    SRK(
        characterName = "Hakla Shah Rukh Khan",
        comedyTitle = "K-k-k-Kiran Stalker",
        memeFilename = "shah-rukh-khan.mp3",
        baseHp = 90f,
        baseSpeed = 2.1f,
        attackDamage = 12f,
        pointReward = 85,
        funQuote = "K-k-k-Kiran! Darr lagta hai!",
        spriteRes = R.drawable.zombie_srk,
        portraitRes = R.drawable.portrait_srk
    ),
    SALMAN(
        characterName = "Salman Khan",
        comedyTitle = "Bhai Berserker",
        memeFilename = "Salman.mp3",
        baseHp = 220f,
        baseSpeed = 1.55f,
        attackDamage = 26f,
        pointReward = 150,
        funQuote = "Swag se karenge sabka swagat!",
        spriteRes = R.drawable.zombie_salman,
        portraitRes = R.drawable.portrait_salman
    ),
    RAVI(
        characterName = "Ravi Kishan",
        comedyTitle = "Bhojpuri Ravager",
        memeFilename = "RAVI.mp3",
        baseHp = 115f,
        baseSpeed = 1.6f,
        attackDamage = 15f,
        pointReward = 95,
        funQuote = "Zindagi jhandwa, fir bhi ghamandwa!",
        spriteRes = R.drawable.zombie_ravi,
        portraitRes = R.drawable.portrait_ravi
    )
}

enum class WeaponType(
    val displayName: String,
    val fireRateMs: Long,
    val baseDamage: Float,
    val magCapacity: Int,
    val reloadTimeMs: Long,
    val range: Float,
    val spreadAngle: Float,
    val bulletSpeed: Float,
    val sfxKey: String,
    val pelletCount: Int = 1,
    @DrawableRes val iconRes: Int
) {
    PISTOL(
        displayName = "Desi 9mm Pistol",
        fireRateMs = 280L,
        baseDamage = 35f,
        magCapacity = 12,
        reloadTimeMs = 1100L,
        range = 650f,
        spreadAngle = 0.03f,
        bulletSpeed = 19f,
        sfxKey = "sfx_shoot_pistol",
        pelletCount = 1,
        iconRes = R.drawable.player_shoot
    ),
    SMG(
        displayName = "Mumbai Sweeper SMG",
        fireRateMs = 110L,
        baseDamage = 22f,
        magCapacity = 30,
        reloadTimeMs = 1500L,
        range = 580f,
        spreadAngle = 0.08f,
        bulletSpeed = 21f,
        sfxKey = "sfx_shoot_smg",
        pelletCount = 1,
        iconRes = R.drawable.item_smg
    ),
    SHOTGUN(
        displayName = "Dabang 12-Gauge",
        fireRateMs = 700L,
        baseDamage = 28f, // per pellet
        magCapacity = 6,
        reloadTimeMs = 2100L,
        range = 430f,
        spreadAngle = 0.22f,
        bulletSpeed = 17f,
        sfxKey = "sfx_shoot_shotgun",
        pelletCount = 6,
        iconRes = R.drawable.item_shotgun
    ),
    ASSAULT_RIFLE(
        displayName = "Kargil AK-Special",
        fireRateMs = 145L,
        baseDamage = 44f,
        magCapacity = 25,
        reloadTimeMs = 1700L,
        range = 820f,
        spreadAngle = 0.04f,
        bulletSpeed = 24f,
        sfxKey = "sfx_shoot_rifle",
        pelletCount = 1,
        iconRes = R.drawable.item_rifle
    )
}

enum class AbilityType(
    val title: String,
    val description: String,
    val cost: Int
) {
    BULLET_POWER("Desi Masala", "+25% Weapon Damage", 200),
    RAPID_TRIGGER("Fast Finger", "+30% Faster Fire Rate", 250),
    QUICK_RELOAD("Quick Hands", "+40% Faster Reload Time", 180),
    CHAI_SPEED("Cutting Chai", "+20% Movement Speed", 160),
    MAX_HEALTH("Pahalwan Body", "+50 Max HP & Instant 50 HP Heal", 220),
    AYURVEDIC_MEDS("Haldi Doodh", "+50% Medkit Healing", 150),
    AMMO_SCAVENGER("Jugaad Pockets", "+50% Ammo from Pickups", 170),
    BULLETPROOF("Kevlar Kurta", "-20% Zombie Damage Taken", 260),
    YOGA_REGEN("Pranayama", "+1.5 HP/sec Passive Health Regen", 240),
    BOUNTY_HUNTER("Laxmi Chappal", "+35% Bonus Score on Kills", 200)
}

enum class PickupType {
    AMMO,
    MEDKIT,
    WEAPON_SMG,
    WEAPON_SHOTGUN,
    WEAPON_RIFLE
}

data class Pickup(
    val id: Long,
    val type: PickupType,
    val x: Float,
    val y: Float,
    val amount: Int = 0,
    val weaponType: WeaponType? = null,
    val promptText: String
)

enum class ZombieState {
    WALKING,
    ATTACKING,
    HURT,
    DYING,
    DEAD
}

data class Zombie(
    val id: Long,
    val type: ZombieType,
    var x: Float,
    var y: Float,
    var hp: Float,
    val maxHp: Float,
    var speed: Float,
    var damage: Float,
    var state: ZombieState = ZombieState.WALKING,
    var deathSoundPlayed: Boolean = false,
    var attackCooldown: Float = 0f,
    var hurtTimer: Float = 0f,
    var animTimer: Float = 0f,
    var animFrame: Int = 0,
    var deathTimer: Float = 0f,
    var facingLeft: Boolean = false
)

data class Bullet(
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    val damage: Float,
    var distanceRemaining: Float,
    val weaponType: WeaponType
)

data class Particle(
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    val color: Long,
    var alpha: Float,
    val size: Float,
    var life: Float,
    val maxLife: Float
)

data class FloatingText(
    val text: String,
    var x: Float,
    var y: Float,
    val color: Long,
    var life: Float,
    val maxLife: Float = 1.0f
)

enum class ObstacleType {
    BUILDING,
    CHAI_TAPRI,
    SCOOTER,
    AUTO_RICKSHAW,
    CRATE,
    BARREL,
    WALL
}

data class Obstacle(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val type: ObstacleType,
    @DrawableRes val drawableRes: Int? = null,
    val label: String = ""
)
