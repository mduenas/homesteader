package com.markduenas.homesteader.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.benasher44.uuid.uuid4
import com.markduenas.homesteader.core.util.DateTimeUtil
import com.markduenas.homesteader.data.database.CustomerQueries
import com.markduenas.homesteader.data.database.toDomain
import com.markduenas.homesteader.domain.model.Customer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class CustomerRepository(
    private val queries: CustomerQueries
) {
    private val dispatcher = Dispatchers.IO

    fun getAllCustomers(): Flow<List<Customer>> {
        return queries.selectAll()
            .asFlow()
            .mapToList(dispatcher)
            .map { entities -> entities.map { it.toDomain() } }
    }

    fun getCustomerById(id: String): Flow<Customer?> {
        return queries.selectById(id)
            .asFlow()
            .mapToOneOrNull(dispatcher)
            .map { it?.toDomain() }
    }

    fun searchCustomers(query: String): Flow<List<Customer>> {
        return queries.searchCustomers(query, query, query)
            .asFlow()
            .mapToList(dispatcher)
            .map { entities -> entities.map { it.toDomain() } }
    }

    suspend fun insertCustomer(customer: Customer): String = withContext(dispatcher) {
        val id = customer.id.ifBlank { uuid4().toString() }
        val now = DateTimeUtil.nowIsoString()
        queries.insert(
            id = id,
            name = customer.name,
            phone = customer.phone,
            email = customer.email,
            address = customer.address,
            notes = customer.notes,
            created_at = now,
            updated_at = now
        )
        id
    }

    suspend fun updateCustomer(customer: Customer) = withContext(dispatcher) {
        val now = DateTimeUtil.nowIsoString()
        queries.update(
            name = customer.name,
            phone = customer.phone,
            email = customer.email,
            address = customer.address,
            notes = customer.notes,
            updated_at = now,
            id = customer.id
        )
    }

    suspend fun deleteCustomer(id: String) = withContext(dispatcher) {
        queries.delete(id)
    }
}
