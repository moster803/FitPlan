package de.fitplan.app.ui.player

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import de.fitplan.app.R
import de.fitplan.app.core.model.ExerciseMode
import de.fitplan.app.core.model.Settings
import de.fitplan.app.core.workout.Phase
import de.fitplan.app.core.workout.PhaseType
import de.fitplan.app.core.workout.Side
import java.util.Locale

/** Töne, Vibration und Sprachansagen während des Trainings – damit man nicht aufs Handy schauen muss. */
class WorkoutFeedback(private val context: Context) : TextToSpeech.OnInitListener {

    private var sound = true
    private var vibration = true
    private var voice = true
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var pendingSpeech: String? = null
    private val tone: ToneGenerator? = runCatching { ToneGenerator(AudioManager.STREAM_MUSIC, 80) }.getOrNull()
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Vibrator::class.java)
    }

    fun configure(settings: Settings) {
        sound = settings.sound
        vibration = settings.vibration
        voice = settings.voice
        if (voice && tts == null) tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        val engine = tts ?: return
        if (status != TextToSpeech.SUCCESS) return
        val result = engine.setLanguage(Locale.GERMAN)
        ttsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
        pendingSpeech?.let(::speak)
        pendingSpeech = null
    }

    fun phaseStarted(phases: List<Phase>, index: Int) {
        val phase = phases.getOrNull(index) ?: return
        beep(ToneGenerator.TONE_PROP_BEEP2, 250)
        vibrate(longArrayOf(0, 180))
        speak(announcement(phase, phases.getOrNull(index + 1)))
    }

    fun countdown(secondsLeft: Int) {
        beep(ToneGenerator.TONE_PROP_BEEP, 120)
        if (secondsLeft == 1) vibrate(longArrayOf(0, 60))
    }

    fun finished() {
        beep(ToneGenerator.TONE_PROP_ACK, 400)
        vibrate(longArrayOf(0, 120, 100, 120, 100, 250))
        speak(context.getString(R.string.tts_finished))
    }

    fun announce(phase: Phase, next: Phase?) = speak(announcement(phase, next))

    private fun announcement(phase: Phase, next: Phase?): String {
        val name = phase.exercise.name
        return when (phase.type) {
            PhaseType.GET_READY -> context.getString(R.string.tts_get_ready, name)
            PhaseType.SWITCH_SIDE -> context.getString(R.string.tts_switch_side)
            PhaseType.REST -> if (next != null && next.set > 1 && next.exercise.id == phase.exercise.id) {
                context.getString(R.string.tts_rest_next_set, next.set)
            } else {
                context.getString(R.string.tts_rest_next, name)
            }
            PhaseType.WORK -> {
                val amount = if (phase.mode == ExerciseMode.TIMED) {
                    context.resources.getQuantityString(R.plurals.tts_seconds, phase.seconds, phase.seconds)
                } else {
                    context.resources.getQuantityString(R.plurals.tts_reps, phase.reps, phase.reps)
                }
                when (phase.side) {
                    Side.LEFT -> context.getString(R.string.tts_work_left, name, amount)
                    Side.RIGHT -> context.getString(R.string.tts_work_right, amount)
                    null -> context.getString(R.string.tts_work, name, amount)
                }
            }
        }
    }

    private fun speak(text: String) {
        if (!voice) return
        val engine = tts ?: return
        if (!ttsReady) {
            pendingSpeech = text
            return
        }
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "fitplan")
    }

    private fun beep(type: Int, durationMs: Int) {
        if (sound) tone?.startTone(type, durationMs)
    }

    private fun vibrate(pattern: LongArray) {
        if (!vibration) return
        val device = vibrator ?: return
        if (!device.hasVibrator()) return
        device.vibrate(VibrationEffect.createWaveform(pattern, -1))
    }

    fun release() {
        tone?.release()
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
