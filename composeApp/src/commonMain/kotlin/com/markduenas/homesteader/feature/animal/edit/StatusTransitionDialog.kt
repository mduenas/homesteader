package com.markduenas.homesteader.feature.animal.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.markduenas.homesteader.core.designsystem.components.CustomerSelectorField
import com.markduenas.homesteader.core.util.DateTimeUtil
import com.markduenas.homesteader.core.util.formatDecimal
import com.markduenas.homesteader.domain.model.AnimalStatus
import com.markduenas.homesteader.domain.model.Species
import kotlinx.datetime.LocalDate

data class StatusTransitionData(
    val salePrice: Double? = null,
    val buyer: String? = null,
    val buyerContact: String? = null,
    val customerId: String? = null,
    val saleWeight: Double? = null,
    val liveWeight: Double? = null,
    val dressedWeight: Double? = null,
    val killFee: Double? = null,
    val butcherPricePerPound: Double? = null,
    val numberOfAnimals: Int? = null,
    val harvestPurpose: String? = null,
    val harvestRevenue: Double? = null,
    val reason: String? = null
)

private fun Species.isMeatSpecies(): Boolean = this in listOf(
    Species.CATTLE_BEEF, Species.PIG, Species.GOAT_MEAT, Species.SHEEP,
    Species.RABBIT, Species.CHICKEN_BROILER, Species.TURKEY, Species.DUCK, Species.QUAIL
)

private fun Species.isPoultryBatch(): Boolean = this in listOf(
    Species.CHICKEN_BROILER, Species.TURKEY, Species.DUCK, Species.QUAIL
)

private fun Species.hasMeatWeight(): Boolean = isMeatSpecies()

private fun formatAge(birthDate: LocalDate?, eventDate: LocalDate): String {
    if (birthDate == null) return "Unknown age"
    val totalDays = (eventDate.toEpochDays() - birthDate.toEpochDays()).toInt()
    if (totalDays < 0) return "Unknown age"
    return when {
        totalDays < 30 -> "$totalDays days"
        totalDays < 365 -> "${totalDays / 30} months, ${totalDays % 30} days"
        else -> {
            val years = totalDays / 365
            val rem = totalDays % 365
            val months = rem / 30
            if (months > 0) "$years yr $months mo" else "$years years"
        }
    }
}

@Composable
fun StatusTransitionDialog(
    animalName: String,
    newStatus: AnimalStatus,
    species: Species,
    birthDate: LocalDate? = null,
    eventDate: LocalDate = DateTimeUtil.today(),
    onConfirm: (StatusTransitionData) -> Unit,
    onDismiss: () -> Unit
) {
    when (newStatus) {
        AnimalStatus.SOLD -> SoldDialog(animalName, species, birthDate, eventDate, onConfirm, onDismiss)
        AnimalStatus.DECEASED -> DeceasedDialog(animalName, species, birthDate, eventDate, onConfirm, onDismiss)
        AnimalStatus.TRANSFERRED -> TransferredDialog(animalName, onConfirm, onDismiss)
        AnimalStatus.ACTIVE -> ReactivateDialog(animalName, onConfirm, onDismiss)
    }
}

@Composable
private fun SoldDialog(
    animalName: String,
    species: Species,
    birthDate: LocalDate?,
    eventDate: LocalDate,
    onConfirm: (StatusTransitionData) -> Unit,
    onDismiss: () -> Unit
) {
    var salePrice by remember { mutableStateOf("") }
    var buyer by remember { mutableStateOf("") }
    var customerId by remember { mutableStateOf<String?>(null) }
    var buyerContact by remember { mutableStateOf("") }
    var saleWeight by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Sale") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnimalAgeRow(animalName, birthDate, eventDate)
                OutlinedTextField(
                    value = salePrice,
                    onValueChange = { salePrice = it },
                    label = { Text("Sale Price ($)") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                CustomerSelectorField(
                    selectedCustomerId = customerId,
                    buyerNameFallback = buyer,
                    onCustomerSelected = { id, name ->
                        customerId = id.ifBlank { null }
                        buyer = name
                    },
                    onBuyerNameChanged = { buyer = it; customerId = null }
                )
                OutlinedTextField(
                    value = buyerContact,
                    onValueChange = { buyerContact = it },
                    label = { Text("Contact (optional)") },
                    placeholder = { Text("Phone or email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                if (species.hasMeatWeight()) {
                    OutlinedTextField(
                        value = saleWeight,
                        onValueChange = { saleWeight = it },
                        label = { Text("Live Weight at Sale (lbs, optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(StatusTransitionData(
                    salePrice = salePrice.toDoubleOrNull(),
                    buyer = buyer.ifBlank { null },
                    buyerContact = buyerContact.ifBlank { null },
                    customerId = customerId,
                    saleWeight = saleWeight.toDoubleOrNull(),
                    reason = notes.ifBlank { null }
                ))
            }) { Text("Confirm") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Skip") } }
    )
}

@Composable
private fun DeceasedDialog(
    animalName: String,
    species: Species,
    birthDate: LocalDate?,
    eventDate: LocalDate,
    onConfirm: (StatusTransitionData) -> Unit,
    onDismiss: () -> Unit
) {
    var isHarvest by remember { mutableStateOf(species.isMeatSpecies()) }
    var liveWeight by remember { mutableStateOf("") }
    var dressedWeight by remember { mutableStateOf("") }
    var killFee by remember { mutableStateOf("") }
    var butcherPricePerPound by remember { mutableStateOf("") }
    var numberOfAnimals by remember { mutableStateOf("1") }
    var purpose by remember { mutableStateOf("Personal Use") }
    var revenue by remember { mutableStateOf("") }
    var buyer by remember { mutableStateOf("") }
    var customerId by remember { mutableStateOf<String?>(null) }
    var causeOfDeath by remember { mutableStateOf("") }

    val purposes = listOf("Personal Use", "Sale", "Donation")

    val processingCost by remember {
        derivedStateOf {
            val k = killFee.toDoubleOrNull() ?: 0.0
            val d = dressedWeight.toDoubleOrNull() ?: 0.0
            val r = butcherPricePerPound.toDoubleOrNull() ?: 0.0
            k + (d * r)
        }
    }
    val grossRevenue by remember { derivedStateOf { revenue.toDoubleOrNull() ?: 0.0 } }
    val netRevenue by remember { derivedStateOf { grossRevenue - processingCost } }
    val showBreakdown by remember {
        derivedStateOf { processingCost > 0.0 || grossRevenue > 0.0 }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isHarvest) "Record Harvest" else "Record Death") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnimalAgeRow(animalName, birthDate, eventDate)

                if (species.isMeatSpecies()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { isHarvest = true }, modifier = Modifier.weight(1f)) {
                            Text(
                                "Harvest / Slaughter",
                                color = if (isHarvest) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (isHarvest) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                        TextButton(onClick = { isHarvest = false }, modifier = Modifier.weight(1f)) {
                            Text(
                                "Unexpected Death",
                                color = if (!isHarvest) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (!isHarvest) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                    HorizontalDivider()
                }

                if (isHarvest) {
                    if (species.isPoultryBatch()) {
                        OutlinedTextField(
                            value = numberOfAnimals,
                            onValueChange = { numberOfAnimals = it },
                            label = { Text("Number of Birds") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = liveWeight,
                            onValueChange = { liveWeight = it },
                            label = { Text("Live Wt (lbs)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = dressedWeight,
                            onValueChange = { dressedWeight = it },
                            label = { Text("Dressed Wt (lbs)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Text(
                        "Processing Costs",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = killFee,
                            onValueChange = { killFee = it },
                            label = { Text("Kill Fee ($)") },
                            placeholder = { Text("0.00") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = butcherPricePerPound,
                            onValueChange = { butcherPricePerPound = it },
                            label = { Text("Butcher ($/lb)") },
                            placeholder = { Text("0.00") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Purpose", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            purposes.forEach { p ->
                                val selected = purpose == p
                                TextButton(onClick = { purpose = p }) {
                                    Text(
                                        text = p,
                                        color = if (selected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }
                    if (purpose == "Sale") {
                        OutlinedTextField(
                            value = revenue,
                            onValueChange = { revenue = it },
                            label = { Text("Gross Revenue ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        CustomerSelectorField(
                            selectedCustomerId = customerId,
                            buyerNameFallback = buyer,
                            onCustomerSelected = { id, name ->
                                customerId = id.ifBlank { null }
                                buyer = name
                            },
                            onBuyerNameChanged = { buyer = it; customerId = null }
                        )
                    }
                    if (showBreakdown) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    "Financial Summary",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (grossRevenue > 0.0) BreakdownRow("Gross Revenue", grossRevenue)
                                if (processingCost > 0.0) {
                                    BreakdownRow("Processing Costs", -processingCost)
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                                    BreakdownRow("Net Revenue", netRevenue, bold = true)
                                }
                                val dressed = dressedWeight.toDoubleOrNull()
                                if (dressed != null && dressed > 0.0 && grossRevenue > 0.0) {
                                    Text(
                                        text = "Revenue: ${(grossRevenue / dressed).formatDecimal()}/lb dressed",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = causeOfDeath,
                        onValueChange = { causeOfDeath = it },
                        label = { Text("Cause of Death") },
                        placeholder = { Text("e.g. illness, injury, old age") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (isHarvest) {
                    onConfirm(StatusTransitionData(
                        liveWeight = liveWeight.toDoubleOrNull(),
                        dressedWeight = dressedWeight.toDoubleOrNull(),
                        killFee = killFee.toDoubleOrNull(),
                        butcherPricePerPound = butcherPricePerPound.toDoubleOrNull(),
                        numberOfAnimals = numberOfAnimals.toIntOrNull(),
                        harvestPurpose = purpose,
                        harvestRevenue = revenue.toDoubleOrNull(),
                        buyer = buyer.ifBlank { null },
                        customerId = customerId
                    ))
                } else {
                    onConfirm(StatusTransitionData(reason = causeOfDeath.ifBlank { null }))
                }
            }) { Text("Confirm") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Skip") } }
    )
}

@Composable
private fun TransferredDialog(
    animalName: String,
    onConfirm: (StatusTransitionData) -> Unit,
    onDismiss: () -> Unit
) {
    var destination by remember { mutableStateOf("") }
    var recipient by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Transfer") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(animalName.ifBlank { "This animal" }, style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = destination,
                    onValueChange = { destination = it },
                    label = { Text("Transfer Destination") },
                    placeholder = { Text("e.g. neighbor\'s farm, auction house") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = recipient,
                    onValueChange = { recipient = it },
                    label = { Text("Recipient (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(StatusTransitionData(
                    reason = destination.ifBlank { null },
                    buyer = recipient.ifBlank { null }
                ))
            }) { Text("Confirm") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Skip") } }
    )
}

@Composable
private fun ReactivateDialog(
    animalName: String,
    onConfirm: (StatusTransitionData) -> Unit,
    onDismiss: () -> Unit
) {
    var reason by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reactivate Animal") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(animalName.ifBlank { "This animal" }, style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(StatusTransitionData(reason = reason.ifBlank { null })) }) { Text("Confirm") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Skip") } }
    )
}

@Composable
private fun AnimalAgeRow(animalName: String, birthDate: LocalDate?, eventDate: LocalDate) {
    Column {
        Text(
            text = animalName.ifBlank { "This animal" },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "Age: ${formatAge(birthDate, eventDate)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BreakdownRow(label: String, amount: Double, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
        val color = when {
            amount < 0 -> MaterialTheme.colorScheme.error
            bold -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.onSurface
        }
        Text(
            text = if (amount < 0) "-${(-amount).formatDecimal()}" else "$${amount.formatDecimal()}",
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun HarvestDialog(
    animalName: String,
    species: Species = Species.CATTLE_BEEF,
    birthDate: LocalDate? = null,
    eventDate: LocalDate = DateTimeUtil.today(),
    onConfirm: (StatusTransitionData) -> Unit,
    onDismiss: () -> Unit
) {
    DeceasedDialog(animalName, species, birthDate, eventDate, onConfirm, onDismiss)
}
