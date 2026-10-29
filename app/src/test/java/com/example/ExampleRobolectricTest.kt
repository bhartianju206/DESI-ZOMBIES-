package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.audio.GameAudio
import com.example.game.GameEngine
import com.example.game.GameMap
import com.example.model.AbilityType
import com.example.model.WeaponType
import com.example.model.Zombie
import com.example.model.ZombieType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
    }

    @Test
    fun testAppAndCreatorBranding() {
        val appName = context.getString(R.string.app_name)
        val creatorName = context.getString(R.string.creator_name)
        assertEquals("DESI ZOMBIES", appName)
        assertEquals("CREATER : Raunak_.gpt_", creatorName)
    }

    @Test
    fun testAllSixZombieTypesAndExactMemeAudioFilenames() {
        val types = ZombieType.values()
        assertEquals(6, types.size)

        val mapping = mapOf(
            ZombieType.MODI to "modi.mp3",
            ZombieType.AMITABH to "Amitabh.mp3",
            ZombieType.ACP to "ACP.mp3",
            ZombieType.SRK to "shah-rukh-khan.mp3",
            ZombieType.SALMAN to "Salman.mp3",
            ZombieType.RAVI to "RAVI.mp3"
        )

        for ((type, expectedSound) in mapping) {
            assertEquals("Zombie ${type.characterName} should map to $expectedSound", expectedSound, type.memeFilename)
        }
    }

    @Test
    fun testGameEngineInitialState() {
        val audio = GameAudio(context)
        val engine = GameEngine(context, audio)

        assertEquals(100f, engine.playerHp, 0.01f)
        assertEquals(100f, engine.playerMaxHp, 0.01f)
        assertEquals(1, engine.currentWave)
        assertEquals(0, engine.currentScore)
        assertEquals(WeaponType.PISTOL, engine.currentWeapon)
        assertFalse(engine.playerIsDead)
    }

    @Test
    fun testMapCollisionDetection() {
        // Central avenue crossroads is open
        assertFalse(GameMap.isPointBlocked(1700f, 650f))

        // Boundary walls are blocked
        assertTrue(GameMap.isPointBlocked(10f, 10f))
        assertTrue(GameMap.isPointBlocked(3330f, 1870f))
    }

    @Test
    fun testAbilitySystemApplication() {
        val audio = GameAudio(context)
        val engine = GameEngine(context, audio)

        // Initial damage multiplier
        val baseMultiplier = engine.damageMultiplier
        assertEquals(1.0f, baseMultiplier, 0.001f)

        // Select BULLET_POWER
        engine.selectFreeAbility(AbilityType.BULLET_POWER)
        assertEquals(1.25f, engine.damageMultiplier, 0.001f)

        // Select CHAI_SPEED
        val baseSpeed = engine.speedMultiplier
        engine.selectFreeAbility(AbilityType.CHAI_SPEED)
        assertTrue(engine.speedMultiplier > baseSpeed)
    }

    @Test
    fun testAllSixMemeAudioAssetsCanBeOpened() {
        val audio = GameAudio(context)
        for (type in ZombieType.values()) {
            val (afd, path) = audio.findAssetFd(type.memeFilename)
            assertNotNull("Audio file for ${type.characterName} ('${type.memeFilename}') must exist and open", afd)
            assertTrue("Path must not be empty", path.isNotEmpty())
            assertTrue("Audio file must have valid length", afd!!.length > 0)
            afd.close()
        }
    }

    @Test
    fun testZombieDeathTriggersExactDialogue() {
        val audio = GameAudio(context)
        val engine = GameEngine(context, audio)

        // Spawn ACP zombie
        val zombie = Zombie(
            id = 999L,
            type = ZombieType.ACP,
            x = 1400f,
            y = 1050f,
            hp = 10f,
            maxHp = 140f,
            speed = 1.0f,
            damage = 10f
        )
        engine.zombies.add(zombie)
        assertFalse(zombie.deathSoundPlayed)

        // Direct bullet collision
        engine.bullets.add(
            com.example.model.Bullet(
                x = 1400f,
                y = 1050f,
                vx = 0f,
                vy = 0f,
                damage = 35f,
                distanceRemaining = 100f,
                weaponType = WeaponType.PISTOL
            )
        )

        engine.update(0.016f)

        // Verify zombie is marked dying and death sound flag set
        assertTrue("Death sound must trigger once on death", zombie.deathSoundPlayed)
        assertEquals(com.example.model.ZombieState.DYING, zombie.state)
    }

    @Test
    fun testSurvivorDeathAudioAssetOpens() {
        val audio = GameAudio(context)
        val (afd, path) = audio.findPlayerDeathFd()
        assertNotNull("Survivor death audio file must be accessible in assets", afd)
        assertTrue("Resolved path must not be empty", path.isNotEmpty())
        assertTrue("Survivor death audio must have content", afd!!.length > 0)
        afd.close()
    }

    @Test
    fun testSurvivorDeathTriggersGameOverState() {
        val audio = GameAudio(context)
        val engine = GameEngine(context, audio)

        assertFalse(engine.playerIsDead)
        // Inflict lethal damage
        val zombie = Zombie(
            id = 1001L,
            type = ZombieType.SALMAN,
            x = engine.playerX,
            y = engine.playerY,
            hp = 100f,
            maxHp = 100f,
            speed = 1f,
            damage = 150f
        )
        engine.zombies.add(zombie)
        engine.update(0.016f)

        assertTrue("Player must be dead after lethal damage", engine.playerIsDead)
        assertTrue("UI state must reflect game over", engine.uiState.value.isGameOver)
    }
}
