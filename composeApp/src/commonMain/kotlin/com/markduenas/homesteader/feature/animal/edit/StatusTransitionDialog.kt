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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.markduenas.homesteader.domain.model.AnimalStatus

/**
 * Data collected from the user when an animal's status changes.
 * Passed back to the ViewModel to create the appropriate event.
 */
data class StatusTransitionData(
    val salePrice: Double? = null,
    val buyer: String? = null,
    val buyerContact: String? = null,
    val liveWeight: Double? = null,
    val dressedWeight: Double? = null,
    val harvestPurpose: String? = null,
    val harvestRevenue: Double? = null,
    val reason: String? = null        // cause of death, transfer destination, etc.
)

/**
 * Dialog shown when the user changes an animal's status to a non-ACTIVE value.
 * Collects relevant details (price, buyer, weight, cause, etc.) based on the new status.
 */
@Composable
fun StatusTransitionDialog(
    animalName: String,
    newStatus: AnimalStatus,
    onConfirm: (StatusTransitionData) -> Unit,
    onDismiss: () -> Unit
) {
    var salePrice by remember { mutableStateOf("") }
    var buyer by remember { mutableStateOf("") }
    var buyerContact by remember { mutableStateOf("") }
    var liveWeight by remember { mutableStateOf("") }
    var dressedWeight by remember { mutableStateOf("") }
    var harvestPurpose by remember { mutableStateOf("Personal Use") }
    var harvestRevenue by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    val title = when (newStatus) {
        AnimalStatus.SOLD -> "Record Sale"
        AnimalStatus.DECEASED -> "Record Death"
        AnimalStatus.TRANSFERRED -> "Record Transfer"
        AnimalStatus.ACTIVE -> "Reactivate Animal"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = animalName.ifBlank { "This animal" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                when (newStatus) {
                    AnimalStatus.SOLD -> {
                        OutlinedTextField(
                            value = salePrice,
                            onValueChange = { salePrice = it },
                            label = { Text("Sale Price ($)") },
                            placeholder = { Text("0.00") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = buyer,
                            onValueChange = { buyer = it },
                            label = { Text("Buyer Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = buyerContact,
                            onValueChange = { buyerContact = it },
                            label = { Text("Buyer Contact (optional)") },
                            placeholder = { Text("Phone or email") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = reason,
                            onValueChange = { reason = it },
                            label = { Text("Notes (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }

                    AnimalStatus.DECEASED -> {
                        // Deceased could be natural death OR harvest — show a note
                        Text(
                            text = "To record a planned harvest (slaughter for meat), use the Harvest option instead.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = reason,
                            onValueChange = { reason = it },
                            label = { Text("Cause of Death") },
                            placeholder = { Text("e.g. illness, injury, old age") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }

                    AnimalStatus.TRANSFERRED -> {
                        OutlinedTextField(
                            value = reason,
                            onValueChange = { reason = it },
                            label = { Text("Transfer Destination") },
                            placeholder = { Text("e.g. neighbor's farm, auction house") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = buyer,
                            onValueChange = { buyer = it },
                            label = { Text("Recipient (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    AnimalStatus.ACTIVE -> {
                        OutlinedTextField(
                            value = reason,
                            onValueChange = { reason = it },
                            label = { Text("Reason for Reactivation (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(
                    StatusTransitionData(
                        salePrice = salePrice.toDoubleOrNull(),
                        buyer = buyer.ifBlank { null },
                        buyerContact = buyerContact.ifBlank { null },
                        liveWeight = liveWeight.toDoubleOrNull(),
                        dressedWeight = dressedWeight.toDoubleOrNull(),
                        harvestPurpose = harvestPurpose.ifBlank { null },
                        harvestRevenue = harvestRevenue.toDoubleOrNull(),
                        reason = reason.ifBlank { null }
                    )
                )
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Skip") }
        }
    )
}

/**
 * Dialog for recording a planned harvest (slaughter for meat/fiber/etc).
 * Separate from StatusTransitionDialog so it can be triggered independently
 * or when the user explicitly chooses "Harvest" from the status dropdown.
 */
@Composable
fun HarvestDialog(
    animalName: String,
    onConfirm: (StatusTransitionData) -> Unit,
    onDismiss: () -> Unit
) {
    var liveWeight by remember { mutableStateOf("") }
    var dressedWeight by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("Personal Use") }
    var revenue by remember { mutableStateOf("") }
    var buyer by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val purposes = listOf("Personal Use", "Sale", "Donation")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Harvest") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = animalName.ifBlank { "This animal" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

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

                // Purpose selector — simple text buttons
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Purpose", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        purposes.forEach { p ->
                            val selected = purpose == p
                            TextButton(
                                onClick = { purpose = p },
                                modifier = Modifier
                            ) {
                                Text(
                                    text = p,
                                    color = if (selected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = if (selected) MaterialTheme.typography.labelLarge
                                            else MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }

                if (purpose == "Sale") {
                    OutlinedTextField(
                        value = revenue,
                        onValueChange = { revenue = it },
                        label = { Text("Revenue ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = buyer,
                        onValueChange = { buyer = it },
                        label = { Text("Buyer (optional)") },
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
                onConfirm(
                    StatusTransitionData(
                        liveWeight = liveWeight.toDoubleOrNull(),
                        dressedWeight = dressedWeight.toDoubleOrNull(),
                        harvestPurpose = purpose,
                        harvestRevenue = revenue.toDoubleOrNull(),
                        buyer = buyer.ifBlank { null },
                        reason = notes.ifBlank { null }
                    )
                )
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Skip") }
        }
    )
}
