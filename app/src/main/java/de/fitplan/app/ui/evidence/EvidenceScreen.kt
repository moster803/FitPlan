package de.fitplan.app.ui.evidence

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.fitplan.app.R
import de.fitplan.app.core.library.ApproachInfo
import de.fitplan.app.core.library.Evidence
import de.fitplan.app.core.library.ExerciseLibrary
import de.fitplan.app.core.model.Approach
import de.fitplan.app.ui.common.SectionHeader

/** Übersicht über die Studienlage, auf der die Übungsbibliothek beruht. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvidenceScreen(onBack: () -> Unit, onShowExercises: (Approach) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.evidence_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text(
                    stringResource(R.string.evidence_intro),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item { SectionHeader(stringResource(R.string.evidence_key_findings)) }
            items(Evidence.keyFindings) { finding ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(finding.text, style = MaterialTheme.typography.bodyLarge)
                        SourceLinks(finding.sourceIds)
                    }
                }
            }
            item { SectionHeader(stringResource(R.string.evidence_approaches)) }
            items(Evidence.approaches, key = { it.approach.name }) { info ->
                ApproachCard(info, onShowExercises = { onShowExercises(info.approach) })
            }
            item { SectionHeader(stringResource(R.string.evidence_red_flags)) }
            item { RedFlagsCard() }
            item { SectionHeader(stringResource(R.string.evidence_sources)) }
            items(Evidence.sources, key = { it.id }) { source ->
                SourceCard(source.citation, source.finding, source.url)
            }
            item {
                Text(
                    stringResource(R.string.about_disclaimer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun ApproachCard(info: ApproachInfo, onShowExercises: () -> Unit) {
    val count = ExerciseLibrary.all.count { it.approach == info.approach }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(info.emoji, fontSize = 22.sp)
                Spacer(Modifier.width(10.dp))
                Text(info.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(
                    stringResource(R.string.evidence_exercise_count, count),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(info.summary, style = MaterialTheme.typography.bodyMedium)
            SourceLinks(info.sourceIds)
            TextButton(onClick = onShowExercises, contentPadding = PaddingValues(0.dp)) {
                Text(stringResource(R.string.evidence_show_exercises))
            }
        }
    }
}

@Composable
private fun RedFlagsCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Warning, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.evidence_red_flags_text), style = MaterialTheme.typography.bodyMedium)
            }
            Evidence.redFlags.forEach { flag ->
                Row {
                    Text("•", modifier = Modifier.width(16.dp), fontWeight = FontWeight.Bold)
                    Text(flag, style = MaterialTheme.typography.bodyMedium)
                }
            }
            SourceLinks(listOf("nvl2017"))
        }
    }
}

@Composable
private fun SourceCard(citation: String, finding: String, url: String) {
    val uriHandler = LocalUriHandler.current
    Card(
        onClick = { runCatching { uriHandler.openUri(url) } },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(Modifier.padding(16.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(citation, style = MaterialTheme.typography.titleSmall)
                Text(finding, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

/** Kurze, antippbare Quellenangaben, die den Artikel bzw. die Leitlinie im Browser öffnen. */
@Composable
fun SourceLinks(sourceIds: List<String>, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    Column(modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        sourceIds.mapNotNull(Evidence::source).forEach { source ->
            Text(
                text = "↗ " + source.citation,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .clickable { runCatching { uriHandler.openUri(source.url) } },
            )
        }
    }
}
