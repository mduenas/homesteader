package com.markduenas.homesteader.feature.customers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.markduenas.homesteader.core.util.ContactImportData
import com.markduenas.homesteader.core.util.IncomingContactStore
import com.markduenas.homesteader.data.repository.CustomerRepository
import com.markduenas.homesteader.domain.model.Customer
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Shown when a contact is shared into the app from the native Contacts app.
 * Pre-fills fields from the parsed vCard and lets the user edit before saving.
 */
@Composable
fun ContactImportDialog(
    contact: ContactImportData,
    customerRepository: CustomerRepository = koinInject()
) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf(contact.name) }
    var phone by remember { mutableStateOf(contact.phone ?: "") }
    var email by remember { mutableStateOf(contact.email ?: "") }
    var address by remember { mutableStateOf(contact.address ?: "") }
    var isSaving by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { IncomingContactStore.clear() },
        title = { Text("Add as Customer?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Review and save this contact as a customer in Steady Hand.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) return@Button
                    isSaving = true
                    scope.launch {
                        customerRepository.insertCustomer(
                            Customer(
                                name = name.trim(),
                                phone = phone.trim().ifBlank { null },
                                email = email.trim().ifBlank { null },
                                address = address.trim().ifBlank { null }
                            )
                        )
                        IncomingContactStore.clear()
                    }
                },
                enabled = name.isNotBlank() && !isSaving
            ) {
                Text(if (isSaving) "Saving..." else "Save Customer")
            }
        },
        dismissButton = {
            TextButton(onClick = { IncomingContactStore.clear() }) {
                Text("Cancel")
            }
        }
    )
}
