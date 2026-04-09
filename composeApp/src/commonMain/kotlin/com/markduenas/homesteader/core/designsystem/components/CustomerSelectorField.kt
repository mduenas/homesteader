package com.markduenas.homesteader.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.markduenas.homesteader.data.repository.CustomerRepository
import com.markduenas.homesteader.domain.model.Customer
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * A field that lets users select an existing customer or add a new one inline.
 *
 * [selectedCustomerId] is the ID of the currently selected customer (or null).
 * [buyerNameFallback] is the free-text buyer name to show if no customer is selected.
 * [onCustomerSelected] called with (customerId, displayName) when a customer is picked.
 * [onBuyerNameChanged] called when user edits the free-text name (clears customer link).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerSelectorField(
    selectedCustomerId: String?,
    buyerNameFallback: String,
    onCustomerSelected: (customerId: String, name: String) -> Unit,
    onBuyerNameChanged: (String) -> Unit,
    label: String = "Buyer / Customer",
    modifier: Modifier = Modifier,
    customerRepository: CustomerRepository = koinInject()
) {
    val customers by customerRepository.getAllCustomers().collectAsState(initial = emptyList())
    var showPicker by remember { mutableStateOf(false) }
    var showAddNew by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val selectedCustomer = remember(selectedCustomerId, customers) {
        customers.find { it.id == selectedCustomerId }
    }

    val displayText = selectedCustomer?.name ?: buyerNameFallback

    OutlinedTextField(
        value = displayText,
        onValueChange = { newText ->
            // User typing clears the customer link
            onCustomerSelected("", newText)
            onBuyerNameChanged(newText)
        },
        label = { Text(label) },
        placeholder = { Text("Name or select from list") },
        trailingIcon = {
            Text(
                text = "👤",
                modifier = Modifier.clickable { showPicker = true }
            )
        },
        modifier = modifier.fillMaxWidth()
    )

    if (showPicker) {
        CustomerPickerDialog(
            customers = customers,
            onSelect = { customer ->
                onCustomerSelected(customer.id, customer.name)
                showPicker = false
            },
            onAddNew = {
                showPicker = false
                showAddNew = true
            },
            onDismiss = { showPicker = false }
        )
    }

    if (showAddNew) {
        AddNewCustomerDialog(
            onSave = { newCustomer ->
                scope.launch {
                    val id = customerRepository.insertCustomer(newCustomer)
                    onCustomerSelected(id, newCustomer.name)
                }
                showAddNew = false
            },
            onDismiss = { showAddNew = false }
        )
    }
}

@Composable
private fun CustomerPickerDialog(
    customers: List<Customer>,
    onSelect: (Customer) -> Unit,
    onAddNew: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Customer") },
        text = {
            Column {
                if (customers.isEmpty()) {
                    Text(
                        text = "No customers saved yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(customers) { customer ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(customer) }
                                    .padding(vertical = 10.dp, horizontal = 4.dp)
                            ) {
                                Text(
                                    text = customer.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                val sub = listOfNotNull(customer.phone, customer.email)
                                    .joinToString(" · ")
                                if (sub.isNotBlank()) {
                                    Text(
                                        text = sub,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAddNew() }
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "+ Add New Customer",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AddNewCustomerDialog(
    onSave: (Customer) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Customer") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            Customer(
                                name = name.trim(),
                                phone = phone.trim().ifBlank { null },
                                email = email.trim().ifBlank { null }
                            )
                        )
                    }
                },
                enabled = name.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
