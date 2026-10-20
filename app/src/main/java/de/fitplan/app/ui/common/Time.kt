package de.fitplan.app.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/** Das aktuelle Datum, das sich um Mitternacht und beim Zurückkehren in die App aktualisiert. */
@Composable
fun rememberToday(): LocalDate {
    var today by remember { mutableStateOf(LocalDate.now()) }
    LifecycleResumeEffect(Unit) {
        today = LocalDate.now()
        onPauseOrDispose { }
    }
    LaunchedEffect(today) {
        val untilMidnight = Duration.between(LocalDateTime.now(), today.plusDays(1).atStartOfDay()).toMillis()
        delay(untilMidnight.coerceAtLeast(0) + 1_000)
        today = LocalDate.now()
    }
    return today
}

fun timeOf(epochMillis: Long): LocalTime = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalTime()

fun formatClock(epochMillis: Long): String = timeOf(epochMillis).let { formatMinute(it.hour * 60 + it.minute) }

val FEELING_EMOJIS = listOf("😣", "😕", "😐", "🙂", "😄")

fun feelingEmoji(feeling: Int?): String? = feeling?.let { FEELING_EMOJIS.getOrNull(it - 1) }
