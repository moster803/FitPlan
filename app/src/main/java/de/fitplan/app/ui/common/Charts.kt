package de.fitplan.app.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/** Ein Wert pro Kategorie; [value] null bedeutet „keine Daten“. */
data class ColumnDatum(val label: String, val value: Float?, val detail: String)

/**
 * Schlichtes Säulendiagramm für eine Datenreihe (0…1). Dünne Säulen mit abgerundetem Ende,
 * Grundlinie als Haarlinie, Antippen zeigt den genauen Wert.
 */
@Composable
fun ColumnChart(
    data: List<ColumnDatum>,
    description: String,
    modifier: Modifier = Modifier,
    plotHeight: androidx.compose.ui.unit.Dp = 96.dp,
) {
    val scheme = MaterialTheme.colorScheme
    val barColor = scheme.primary
    val emptyColor = scheme.outlineVariant
    val baseline = scheme.outlineVariant
    var selected by remember(data) { mutableStateOf<Int?>(null) }
    Column(modifier.semantics { contentDescription = description }) {
        Box(Modifier.height(28.dp).fillMaxWidth()) {
            selected?.let { index ->
                Tooltip(data[index].detail, Modifier.align(Alignment.Center))
            }
        }
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(plotHeight)
                .pointerInput(data) {
                    detectTapGestures { offset ->
                        val slot = size.width / data.size.toFloat()
                        val index = (offset.x / slot).toInt().coerceIn(0, data.lastIndex)
                        selected = if (selected == index) null else index
                    }
                },
        ) {
            val slot = size.width / data.size
            val barWidth = minOf(24.dp.toPx(), slot * 0.6f)
            val radius = 4.dp.toPx()
            data.forEachIndexed { index, datum ->
                val centerX = slot * index + slot / 2
                val left = centerX - barWidth / 2
                val value = datum.value
                if (value == null) {
                    // Keine fälligen Einnahmen: kleiner neutraler Strich statt Säule.
                    drawLine(emptyColor, Offset(centerX - barWidth / 4, size.height - 1.dp.toPx()), Offset(centerX + barWidth / 4, size.height - 1.dp.toPx()), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                } else {
                    val height = (size.height * value.coerceIn(0f, 1f)).coerceAtLeast(if (value > 0f) radius * 2 else 0f)
                    if (height > 0f) {
                        val alpha = if (selected == null || selected == index) 1f else 0.45f
                        val path = Path().apply {
                            addRoundRect(
                                RoundRect(
                                    rect = Rect(Offset(left, size.height - height), Size(barWidth, height)),
                                    topLeft = CornerRadius(radius),
                                    topRight = CornerRadius(radius),
                                    bottomLeft = CornerRadius.Zero,
                                    bottomRight = CornerRadius.Zero,
                                ),
                            )
                        }
                        drawPath(path, barColor.copy(alpha = alpha))
                    }
                }
            }
            drawLine(baseline, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
        }
        Row(Modifier.fillMaxWidth().padding(top = 6.dp)) {
            data.forEach { datum ->
                Text(
                    datum.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

data class LinePoint(val x: Float, val y: Float, val detail: String)

/**
 * Liniendiagramm für eine Reihe mit fester Skala [yMin]…[yMax], z. B. Rückengefühl 1–5.
 * [yLabels] werden links als Achsenbeschriftung gezeigt.
 */
@Composable
fun LineChart(
    points: List<LinePoint>,
    xRange: ClosedFloatingPointRange<Float>,
    yMin: Float,
    yMax: Float,
    yLabels: List<Pair<Float, String>>,
    xLabels: List<Pair<Float, String>>,
    description: String,
    modifier: Modifier = Modifier,
    plotHeight: androidx.compose.ui.unit.Dp = 160.dp,
) {
    val scheme = MaterialTheme.colorScheme
    val lineColor = scheme.primary
    val grid = scheme.outlineVariant.copy(alpha = 0.6f)
    val surface = scheme.surfaceContainerLow
    var selected by remember(points) { mutableStateOf<Int?>(null) }
    val axisWidth = 32.dp

    Column(modifier.semantics { contentDescription = description }) {
        Box(Modifier.height(28.dp).fillMaxWidth()) {
            selected?.let { Tooltip(points[it].detail, Modifier.align(Alignment.Center)) }
        }
        Row {
            Box(Modifier.width(axisWidth).height(plotHeight)) {
                yLabels.forEach { (value, label) ->
                    val fraction = 1f - (value - yMin) / (yMax - yMin)
                    Text(
                        label,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = (plotHeight - 16.dp) * fraction),
                    )
                }
            }
            Canvas(
                Modifier
                    .weight(1f)
                    .height(plotHeight)
                    .pointerInput(points) {
                        detectTapGestures { offset ->
                            if (points.isEmpty()) return@detectTapGestures
                            val span = (xRange.endInclusive - xRange.start).takeIf { it > 0f } ?: 1f
                            val nearest = points.indices.minBy { abs(((points[it].x - xRange.start) / span) * size.width - offset.x) }
                            selected = if (selected == nearest) null else nearest
                        }
                    },
            ) {
                val inset = 8.dp.toPx()
                val plotTop = inset
                val plotBottom = size.height - inset
                fun y(value: Float) = plotBottom - (value - yMin) / (yMax - yMin) * (plotBottom - plotTop)
                val span = (xRange.endInclusive - xRange.start).takeIf { it > 0f } ?: 1f
                fun x(value: Float) = (value - xRange.start) / span * size.width

                yLabels.forEach { (value, _) ->
                    drawLine(grid, Offset(0f, y(value)), Offset(size.width, y(value)), strokeWidth = 1.dp.toPx())
                }
                if (points.isEmpty()) return@Canvas
                val offsets = points.map { Offset(x(it.x), y(it.y)) }
                if (offsets.size > 1) {
                    val line = Path().apply {
                        moveTo(offsets.first().x, offsets.first().y)
                        offsets.drop(1).forEach { lineTo(it.x, it.y) }
                    }
                    val area = Path().apply {
                        addPath(line)
                        lineTo(offsets.last().x, plotBottom)
                        lineTo(offsets.first().x, plotBottom)
                        close()
                    }
                    drawPath(area, lineColor.copy(alpha = 0.10f))
                    drawPath(line, lineColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
                offsets.forEachIndexed { index, point ->
                    val isSelected = index == selected
                    val radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx()
                    drawCircle(surface, radius + 2.dp.toPx(), point)
                    drawCircle(lineColor, radius, point)
                }
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .padding(start = axisWidth, top = 4.dp)
                .height(16.dp),
        ) {
            xLabels.forEach { (value, label) ->
                val span = (xRange.endInclusive - xRange.start).takeIf { it > 0f } ?: 1f
                val fraction = ((value - xRange.start) / span).coerceIn(0f, 1f)
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.align(
                        when {
                            fraction < 0.2f -> Alignment.CenterStart
                            fraction > 0.8f -> Alignment.CenterEnd
                            else -> Alignment.Center
                        },
                    ),
                )
            }
        }
    }
}

@Composable
private fun Tooltip(text: String, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
        shape = MaterialTheme.shapes.small,
        modifier = modifier,
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
    }
}
