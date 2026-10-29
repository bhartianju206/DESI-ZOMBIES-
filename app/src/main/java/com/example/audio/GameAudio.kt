package com.example.audio

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.SystemClock
import android.util.Log
import com.example.model.ZombieType

class GameAudio(private val context: Context) {
    private val TAG = "GameAudio"

    // SoundPool for fast low-latency gameplay SFX (gunshots, reload, impacts, pickups)
    private val sfxSoundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(12)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    // Dedicated MediaPlayer for high-fidelity dialogue speech playback (320kbps MP3s)
    private var dialoguePlayer: MediaPlayer? = null
    private val dialogueLock = Any()

    // Dedicated MediaPlayer for Survivor (Player) death sound
    private var playerDeathPlayer: MediaPlayer? = null
    private val playerDeathLock = Any()

    // SFX Sound IDs
    private val sfxMap = mutableMapOf<String, Int>()

    var isSoundEnabled: Boolean = true

    // Cooldown timestamp to prevent microsecond duplicate trigger race conditions
    private var lastDeathSoundTime: Long = 0L
    private val minDialogueIntervalMs = 200L

    init {
        loadSfxSounds()
        verifyMemeAudioFiles()
        verifyPlayerDeathAudio()
    }

    /**
     * Resolves an AssetFileDescriptor checking all possible casing, folders, and hyphen/underscore variants.
     */
    fun findAssetFd(filename: String): Pair<AssetFileDescriptor?, String> {
        val assetManager = context.assets
        val variants = listOf(
            "meme/$filename",
            "meme/${filename.lowercase()}",
            "meme/${filename.uppercase()}",
            filename,
            filename.lowercase(),
            filename.uppercase(),
            "meme/${filename.replace('-', '_')}",
            "meme/${filename.replace('_', '-')}",
            filename.replace('-', '_'),
            filename.replace('_', '-')
        ).distinct()

        for (candidate in variants) {
            try {
                val afd = assetManager.openFd(candidate)
                return Pair(afd, candidate)
            } catch (ignored: Exception) {
                // Try next
            }
        }
        return Pair(null, "")
    }

    /**
     * Verifies at startup that all 6 character meme audio recordings can be found and read.
     */
    private fun verifyMemeAudioFiles() {
        val mapping = listOf(
            ZombieType.MODI to "modi.mp3",
            ZombieType.AMITABH to "Amitabh.mp3",
            ZombieType.ACP to "ACP.mp3",
            ZombieType.SALMAN to "Salman.mp3",
            ZombieType.SRK to "shah-rukh-khan.mp3",
            ZombieType.RAVI to "RAVI.mp3"
        )

        for ((zombieType, filename) in mapping) {
            val (afd, resolvedPath) = findAssetFd(filename)
            if (afd != null) {
                Log.i(TAG, "DIAGNOSTIC: Verified audio asset for ${zombieType.characterName} -> '$resolvedPath' (size: ${afd.length} bytes)")
                try {
                    afd.close()
                } catch (ignored: Exception) {}
            } else {
                Log.e(TAG, "DIAGNOSTIC ERROR: MISSING EXACT MEME AUDIO FILE for ${zombieType.characterName}: '$filename'")
            }
        }
    }

    /**
     * Resolves the survivor death sound file from assets.
     */
    fun findPlayerDeathFd(): Pair<AssetFileDescriptor?, String> {
        val candidates = listOf(
            "sfx/player_death.mp3",
            "player_death.mp3",
            "whoooooo-yeleeee-laudeeee_Nk6JpCP_56605a44.mp3"
        )
        for (candidate in candidates) {
            try {
                val afd = context.assets.openFd(candidate)
                return Pair(afd, candidate)
            } catch (ignored: Exception) {}
        }
        return Pair(null, "")
    }

    private fun verifyPlayerDeathAudio() {
        val (afd, path) = findPlayerDeathFd()
        if (afd != null) {
            Log.i(TAG, "DIAGNOSTIC: Verified survivor death audio asset -> '$path' (${afd.length} bytes)")
            try {
                afd.close()
            } catch (ignored: Exception) {}
        } else {
            Log.e(TAG, "DIAGNOSTIC ERROR: Survivor death audio asset NOT found!")
        }
    }

    private fun loadSfxSounds() {
        val sfxNames = listOf(
            "sfx_shoot_pistol",
            "sfx_shoot_smg",
            "sfx_shoot_shotgun",
            "sfx_shoot_rifle",
            "sfx_reload",
            "sfx_hit",
            "sfx_medkit",
            "sfx_pickup",
            "sfx_wave_complete",
            "sfx_player_hurt"
        )

        val assetManager = context.assets
        for (sfx in sfxNames) {
            val candidates = listOf("sfx/$sfx.wav", "$sfx.wav")
            for (c in candidates) {
                try {
                    val afd = assetManager.openFd(c)
                    val soundId = sfxSoundPool.load(afd, 1)
                    sfxMap[sfx] = soundId
                    break
                } catch (ignored: Exception) {}
            }
        }
    }

    /**
     * Plays the character-specific meme death dialogue using Android MediaPlayer for 100% reliable
     * MP3 voice reproduction without cutting off when the zombie entity is removed.
     */
    fun playZombieDeathSound(type: ZombieType) {
        if (!isSoundEnabled) {
            Log.d(TAG, "DIAGNOSTIC: Sound is disabled, skipping death dialogue for ${type.characterName}")
            return
        }

        val now = SystemClock.uptimeMillis()
        if (now - lastDeathSoundTime < minDialogueIntervalMs) {
            // Prevent microsecond multiple-hit race conditions
            Log.d(TAG, "DIAGNOSTIC: Rapid duplicate trigger prevented for ${type.characterName}")
            return
        }
        lastDeathSoundTime = now

        val filename = type.memeFilename
        Log.i(TAG, "DIAGNOSTIC: Playing death dialogue for ${type.characterName} ('$filename')")

        synchronized(dialogueLock) {
            try {
                // Safely release previous dialogue player if running
                dialoguePlayer?.let { player ->
                    try {
                        if (player.isPlaying) {
                            player.stop()
                        }
                        player.reset()
                        player.release()
                    } catch (e: Exception) {
                        Log.w(TAG, "Error cleaning up previous dialogue player", e)
                    }
                }
                dialoguePlayer = null

                val (afd, resolvedPath) = findAssetFd(filename)
                if (afd == null) {
                    Log.e(TAG, "DIAGNOSTIC ERROR: Asset not found for ${type.characterName}: '$filename'")
                    return
                }

                val mp = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    setVolume(1.0f, 1.0f)
                    setOnPreparedListener { player ->
                        player.start()
                        Log.i(TAG, "DIAGNOSTIC SUCCESS: Started dialogue playback for ${type.characterName} from '$resolvedPath'")
                    }
                    setOnCompletionListener { player ->
                        Log.d(TAG, "DIAGNOSTIC: Finished dialogue playback for ${type.characterName}")
                        try {
                            player.reset()
                            player.release()
                        } catch (e: Exception) {}
                        synchronized(dialogueLock) {
                            if (dialoguePlayer == player) {
                                dialoguePlayer = null
                            }
                        }
                    }
                    setOnErrorListener { player, what, extra ->
                        Log.e(TAG, "DIAGNOSTIC ERROR: MediaPlayer error for ${type.characterName}: what=$what, extra=$extra")
                        try {
                            player.reset()
                            player.release()
                        } catch (e: Exception) {}
                        synchronized(dialogueLock) {
                            if (dialoguePlayer == player) {
                                dialoguePlayer = null
                            }
                        }
                        true
                    }
                    prepareAsync()
                }

                dialoguePlayer = mp
                afd.close()
            } catch (e: Exception) {
                Log.e(TAG, "DIAGNOSTIC EXCEPTION: Failed to initiate dialogue for ${type.characterName}", e)
            }
        }
    }

    /**
     * Plays the custom meme audio when the survivor (player) dies.
     * Uses Android MediaPlayer for full high-fidelity playback.
     */
    fun playPlayerDeath() {
        if (!isSoundEnabled) {
            Log.d(TAG, "DIAGNOSTIC: Sound disabled, skipping player death sound")
            return
        }

        Log.i(TAG, "DIAGNOSTIC: Playing survivor death sound")
        synchronized(playerDeathLock) {
            try {
                playerDeathPlayer?.let { player ->
                    try {
                        if (player.isPlaying) player.stop()
                        player.reset()
                        player.release()
                    } catch (e: Exception) {
                        Log.w(TAG, "Error cleaning up previous player death player", e)
                    }
                }
                playerDeathPlayer = null

                val (afd, path) = findPlayerDeathFd()
                if (afd == null) {
                    Log.e(TAG, "DIAGNOSTIC ERROR: Cannot find survivor death audio asset!")
                    return
                }

                val mp = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    setVolume(1.0f, 1.0f)
                    setOnPreparedListener { player ->
                        player.start()
                        Log.i(TAG, "DIAGNOSTIC SUCCESS: Started survivor death audio from '$path'")
                    }
                    setOnCompletionListener { player ->
                        Log.d(TAG, "DIAGNOSTIC: Finished survivor death playback")
                        try {
                            player.reset()
                            player.release()
                        } catch (e: Exception) {}
                        synchronized(playerDeathLock) {
                            if (playerDeathPlayer == player) playerDeathPlayer = null
                        }
                    }
                    setOnErrorListener { player, what, extra ->
                        Log.e(TAG, "DIAGNOSTIC ERROR: MediaPlayer error on player death: what=$what, extra=$extra")
                        try {
                            player.reset()
                            player.release()
                        } catch (e: Exception) {}
                        synchronized(playerDeathLock) {
                            if (playerDeathPlayer == player) playerDeathPlayer = null
                        }
                        true
                    }
                    prepareAsync()
                }

                playerDeathPlayer = mp
                afd.close()
            } catch (e: Exception) {
                Log.e(TAG, "DIAGNOSTIC EXCEPTION: Failed to initiate player death sound", e)
            }
        }
    }

    /**
     * Diagnostic testing helper to test-play any of the 6 character dialogues independently.
     */
    fun testPlayDialogue(type: ZombieType) {
        lastDeathSoundTime = 0L // Bypass cooldown for test
        playZombieDeathSound(type)
    }

    fun playSfx(sfxKey: String, volume: Float = 0.7f) {
        if (!isSoundEnabled) return
        val soundId = sfxMap[sfxKey] ?: return
        try {
            sfxSoundPool.play(soundId, volume, volume, 1, 0, 1.0f)
        } catch (e: Exception) {
            Log.e(TAG, "Error playing SFX $sfxKey", e)
        }
    }

    fun stopAll() {
        sfxSoundPool.autoPause()
        synchronized(dialogueLock) {
            try {
                dialoguePlayer?.let { player ->
                    if (player.isPlaying) {
                        player.pause()
                    }
                }
            } catch (ignored: Exception) {}
        }
        synchronized(playerDeathLock) {
            try {
                playerDeathPlayer?.let { player ->
                    if (player.isPlaying) {
                        player.pause()
                    }
                }
            } catch (ignored: Exception) {}
        }
    }

    fun resumeAll() {
        if (isSoundEnabled) {
            sfxSoundPool.autoResume()
            synchronized(dialogueLock) {
                try {
                    dialoguePlayer?.start()
                } catch (ignored: Exception) {}
            }
            synchronized(playerDeathLock) {
                try {
                    playerDeathPlayer?.start()
                } catch (ignored: Exception) {}
            }
        }
    }

    fun release() {
        sfxSoundPool.release()
        synchronized(dialogueLock) {
            try {
                dialoguePlayer?.release()
                dialoguePlayer = null
            } catch (ignored: Exception) {}
        }
        synchronized(playerDeathLock) {
            try {
                playerDeathPlayer?.release()
                playerDeathPlayer = null
            } catch (ignored: Exception) {}
        }
    }
}
