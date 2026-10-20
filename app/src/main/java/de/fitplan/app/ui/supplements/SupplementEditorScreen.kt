package de.fitplan.app.ui.supplements

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import de.fitplan.app.R
import de.fitplan.app.core.data.newId
import de.fitplan.app.core.library.SupplementTemplates
import de.fitplan.app.core.library.toSupplement
import de.fitplan.app.core.logic.supplement
import de.fitplan.app.core.model.AppData
import de.fitplan.app.core.model.IntakeHint
import de.fitplan.app.core.model.Supplement
import de.fitplan.app.core.model.SupplementForm
import de.fitplan.app.core.model.TimeSlot
import de.fitplan.app.ui.LocalActions
import de.fitplan.app.ui.common.ConfirmDialog
import de.fitplan.app.ui.common.DayChips
import de.fitplan.app.ui.common.InfoCard
import de.fitplan.app.ui.common.SHORT_DATE
import de.fitplan.app.ui.common.SectionHeader
import de.fitplan.app.ui.common.SelectableCircle
import de.fitplan.app.ui.common.Stepper
import de.fitplan.app.ui.common.emoji
import de.fitplan.app.ui.common.formatMinute
import de.fitplan.app.ui.common.label
import de.fitplan.app.ui.common.rememberToday
import de.fitplan.app.ui.common.units
import de.fitplan.app.ui.theme.ACCENT_COUNT
import de.fitplan.app.ui.theme.FitTheme
import java.time.LocalDate

private val SUPPLEMENT_EMOJIS = listOf(
    "💊", "☀️", "🌞", "🌙", "🐟", "🛡️", "🩸", "⚡", "🍊", "🦴", "🏋️", "🥤", "✨", "🦠", "🌿", "🟡", "💧", "🍬", "🫐", "🥄",
)

private const val DAY_MILLIS = 86_400_000L

class SupplementEditorViewModel(val original: Supplement, val isNew: Boolean, val tip: String?) : ViewModel() {
    var supplement by mutableStateOf(original)
        private set

    val isDirty: Boolean get() = isNew || supplement != original

    val canSave: Boolean get() = supplement.name.isNotBlank() && supplement.slots.isNotEmpty() && supplement.days.isNotEmpty()

    fun update(transform: (Supplement) -> Supplement) {
        supplement = transform(supplement)
    }

    companion object {
        fun create(data: AppData, supplementId: String?, templateKey: String?, today: LocalDate): SupplementEditorViewModel {
            data.supplement(supplementId ?: "")?.let {
                return SupplementEditorViewModel(it, isNew = false, tip = SupplementTemplates.find(it.templateKey)?.tip)
            }
            val template = SupplementTemplates.find(templateKey)
            val supplement = template?.toSupplement(newId(), today.toEpochDay()) ?: Supplement(
                id = newId(),
                name = "",
                color = data.supplements.size % ACCENT_COUNT,
                createdEpochDay = today.toEpochDay(),
            )
            return SupplementEditorViewModel(supplement, isNew = true, tip = template?.tip)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SupplementEditorScreen(data: AppData, supplementId: String?, templateKey: String?, onClose: () -> Unit) {
    val actions = LocalActions.current
    val today = rememberToday()
    val vm = viewModel { SupplementEditorViewModel.create(data, supplementId, templateKey, today) }
    val s = vm.supplement
    var confirmDiscard by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var pickDate by rememberSaveable { mutableStateOf(false) }

    val close: () -> Unit = {
        if (vm.isDirty) {
            confirmDiscard = true
        } else {
            onClose()
        }
    }
    BackHandler(onBack = close)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (vm.isNew) R.string.supplement_new_title else R.string.supplement_edit_title)) },
                navigationIcon = {
                    IconButton(onClick = close) { Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.close)) }
                },
                actions = {
                    TextButton(
                        enabled = vm.canSave,
                        onClick = {
                            val toSave = s.copy(name = s.name.trim(), dose = s.dose.trim(), notes = s.notes.trim())
                            actions.launch { upsertSupplement(toSave) }
                            onClose()
                        },
                    ) { Text(stringResource(R.string.save)) }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            vm.tip?.let { tip ->
                item {
                    InfoCard(icon = Icons.Rounded.Lightbulb, title = stringResource(R.string.supplement_tip_title), text = tip)
                }
            }
            item {
                OutlinedTextField(
                    value = s.name,
                    onValueChange = { name -> vm.update { it.copy(name = name) } },
                    label = { Text(stringResource(R.string.field_name)) },
                    placeholder = { Text(stringResource(R.string.field_supplement_name_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                OutlinedTextField(
                    value = s.dose,
                    onValueChange = { dose -> vm.update { it.copy(dose = dose) } },
                    label = { Text(stringResource(R.string.field_dose)) },
                    placeholder = { Text(stringResource(R.string.field_dose_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                SectionHeader(stringResource(R.string.field_icon))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(SUPPLEMENT_EMOJIS) { emoji ->
                        SelectableCircle(
                            selected = s.emoji == emoji,
                            onClick = { vm.update { it.copy(emoji = emoji) } },
                            background = FitTheme.accent(s.color).soft,
                        ) { Text(emoji, fontSize = 20.sp) }
                    }
                }
                Spacer(Modifier.width(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
                    (0 until ACCENT_COUNT).forEach { index ->
                        SelectableCircle(
                            selected = s.color == index,
                            onClick = { vm.update { it.copy(color = index) } },
                            background = FitTheme.accent(index).strong,
                            size = 32.dp,
                        ) {
                            if (s.color == index) Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            }
            item {
                SectionHeader(stringResource(R.string.field_form))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SupplementForm.entries.forEach { form ->
                        FilterChip(
                            selected = s.form == form,
                            onClick = { vm.update { it.copy(form = form) } },
                            label = { Text("${form.emoji} ${stringResource(form.label)}") },
                        )
                    }
                }
            }
            item {
                SectionHeader(stringResource(R.string.field_slots))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TimeSlot.entries.forEach { slot ->
                        val selected = slot in s.slots
                        FilterChip(
                            selected = selected,
                            onClick = { vm.update { it.copy(slots = if (selected) it.slots - slot else it.slots + slot) } },
                            label = { Text("${stringResource(slot.label)} · ${formatMinute(data.settings.slotMinute(slot))}") },
                        )
                    }
                }
                if (s.slots.isEmpty()) {
                    Text(stringResource(R.string.field_slots_required), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
            item {
                SectionHeader(stringResource(R.string.field_days))
                DayChips(selected = s.days, onChange = { days -> vm.update { it.copy(days = days) } })
            }
            item {
                SectionHeader(stringResource(R.string.field_hint))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IntakeHint.entries.forEach { hint ->
                        FilterChip(
                            selected = s.hint == hint,
                            onClick = { vm.update { it.copy(hint = hint) } },
                            label = { Text("${hint.emoji} ${stringResource(hint.label)}") },
                        )
                    }
                }
            }
            item { StockCard(s, onChange = vm::update) }
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(stringResource(R.string.course_title), style = MaterialTheme.typography.titleMedium)
                                Text(
                                    stringResource(R.string.course_text),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Switch(
                                checked = s.endEpochDay != null,
                                onCheckedChange = { on ->
                                    vm.update { it.copy(endEpochDay = if (on) today.plusWeeks(8).toEpochDay() else null) }
                                },
                            )
                        }
                        s.endEpochDay?.let { end ->
                            OutlinedButton(onClick = { pickDate = true }) {
                                Text(stringResource(R.string.course_until, LocalDate.ofEpochDay(end).format(SHORT_DATE)))
                            }
                        }
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = s.notes,
                    onValueChange = { notes -> vm.update { it.copy(notes = notes) } },
                    label = { Text(stringResource(R.string.field_notes)) },
                    minLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                )
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.supplement_active), style = MaterialTheme.typography.bodyLarge)
                        Text(
                            stringResource(R.string.supplement_active_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(checked = s.active, onCheckedChange = { active -> vm.update { it.copy(active = active) } })
                }
            }
            if (!vm.isNew) {
                item {
                    TextButton(
                        onClick = { confirmDelete = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    ) {
                        Icon(Icons.Rounded.Delete, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.supplement_delete))
                    }
                }
            }
        }
    }

    if (pickDate) {
        val state = rememberDatePickerState(initialSelectedDateMillis = (s.endEpochDay ?: today.toEpochDay()) * DAY_MILLIS)
        DatePickerDialog(
            onDismissRequest = { pickDate = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis -> vm.update { it.copy(endEpochDay = millis / DAY_MILLIS) } }
                    pickDate = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = { TextButton(onClick = { pickDate = false }) { Text(stringResource(R.string.cancel)) } },
        ) { DatePicker(state = state) }
    }
    if (confirmDiscard) {
        ConfirmDialog(
            title = stringResource(R.string.discard_title),
            text = stringResource(R.string.discard_text),
            confirmLabel = stringResource(R.string.discard),
            destructive = true,
            onConfirm = {
                confirmDiscard = false
                onClose()
            },
            onDismiss = { confirmDiscard = false },
        )
    }
    if (confirmDelete) {
        ConfirmDialog(
            title = stringResource(R.string.supplement_delete),
            text = stringResource(R.string.supplement_delete_text, s.name),
            confirmLabel = stringResource(R.string.delete),
            destructive = true,
            onConfirm = {
                actions.launch { deleteSupplement(s.id) }
                confirmDelete = false
                onClose()
            },
            onDismiss = { confirmDelete = false },
        )
    }
}

@Composable
private fun StockCard(s: Supplement, onChange: ((Supplement) -> Supplement) -> Unit) {
    val resources = LocalContext.current.resources
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.padding(top = 12.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Inventory2, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.stock_title), style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(R.string.stock_text),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = s.trackStock, onCheckedChange = { on -> onChange { it.copy(trackStock = on) } })
            }
            if (s.trackStock) {
                HorizontalDivider()
                Stepper(
                    label = stringResource(R.string.stock_per_intake),
                    value = s.unitsPerIntake,
                    onChange = { value -> onChange { it.copy(unitsPerIntake = value) } },
                    range = 1..500,
                    format = { resources.getQuantityString(s.form.units, it, it) },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NumberField(
                        label = stringResource(R.string.stock_current),
                        value = s.stock,
                        onChange = { value -> onChange { it.copy(stock = value) } },
                        modifier = Modifier.weight(1f),
                    )
                    NumberField(
                        label = stringResource(R.string.stock_pack_size),
                        value = s.packSize,
                        onChange = { value -> onChange { it.copy(packSize = value.coerceAtLeast(1)) } },
                        modifier = Modifier.weight(1f),
                    )
                }
                Stepper(
                    label = stringResource(R.string.stock_warn_days),
                    value = s.refillWarningDays,
                    onChange = { value -> onChange { it.copy(refillWarningDays = value) } },
                    range = 1..60,
                )
            }
        }
    }
}

@Composable
private fun NumberField(label: String, value: Int, onChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    var text by remember(value) { mutableStateOf(value.toString()) }
    OutlinedTextField(
        value = text,
        onValueChange = { input ->
            val digits = input.filter(Char::isDigit).take(6)
            text = digits
            digits.toIntOrNull()?.let(onChange)
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
    )
}
