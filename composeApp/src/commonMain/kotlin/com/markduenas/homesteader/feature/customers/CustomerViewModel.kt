package com.markduenas.homesteader.feature.customers

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.markduenas.homesteader.data.repository.CustomerRepository
import com.markduenas.homesteader.data.repository.EventRepository
import com.markduenas.homesteader.data.repository.AnimalRepository
import com.markduenas.homesteader.domain.model.AnimalEvent
import com.markduenas.homesteader.domain.model.Customer
import com.markduenas.homesteader.domain.model.EventType
import com.markduenas.homesteader.domain.model.HarvestEventData
import com.markduenas.homesteader.domain.model.StatusChangeEventData
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CustomerPurchase(
    val event: AnimalEvent,
    val animalName: String,
    val animalSpecies: String,
    val totalAmount: Double?,
    val weightLbs: Double?
)

data class CustomerListState(
    val customers: List<Customer> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val error: String? = null
)

data class CustomerDetailState(
    val customer: Customer? = null,
    val purchases: List<CustomerPurchase> = emptyList(),
    val totalRevenue: Double = 0.0,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isSaving: Boolean = false
)

sealed interface CustomerListIntent {
    data object LoadCustomers : CustomerListIntent
    data class Search(val query: String) : CustomerListIntent
    data class DeleteCustomer(val id: String) : CustomerListIntent
}

sealed interface CustomerDetailIntent {
    data class LoadCustomer(val id: String?) : CustomerDetailIntent
    data class SaveCustomer(val customer: Customer) : CustomerDetailIntent
    data object DeleteCustomer : CustomerDetailIntent
}

sealed class CustomerEffect {
    data class ShowError(val message: String) : CustomerEffect()
    data object CustomerSaved : CustomerEffect()
    data object CustomerDeleted : CustomerEffect()
}

class CustomerListViewModel(
    private val customerRepository: CustomerRepository
) : ScreenModel {

    private val _state = MutableStateFlow(CustomerListState())
    val state: StateFlow<CustomerListState> = _state.asStateFlow()

    private val _effects = Channel<CustomerEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        loadCustomers()
    }

    fun handleIntent(intent: CustomerListIntent) {
        when (intent) {
            is CustomerListIntent.LoadCustomers -> loadCustomers()
            is CustomerListIntent.Search -> search(intent.query)
            is CustomerListIntent.DeleteCustomer -> deleteCustomer(intent.id)
        }
    }

    private fun loadCustomers() {
        screenModelScope.launch {
            customerRepository.getAllCustomers()
                .collect { customers ->
                    val query = _state.value.searchQuery
                    _state.update {
                        it.copy(
                            customers = if (query.isBlank()) customers
                                        else customers.filter { c -> c.name.contains(query, ignoreCase = true) },
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun search(query: String) {
        _state.update { it.copy(searchQuery = query) }
        screenModelScope.launch {
            val all = _state.value.customers
            // re-apply filter from current cached list
            customerRepository.getAllCustomers()
                .collect { customers ->
                    _state.update { state ->
                        state.copy(
                            customers = if (query.isBlank()) customers
                                        else customers.filter { c -> c.name.contains(query, ignoreCase = true) }
                        )
                    }
                }
        }
    }

    private fun deleteCustomer(id: String) {
        screenModelScope.launch {
            try {
                customerRepository.deleteCustomer(id)
                _effects.send(CustomerEffect.CustomerDeleted)
            } catch (e: Exception) {
                _effects.send(CustomerEffect.ShowError("Failed to delete customer: ${e.message}"))
            }
        }
    }
}

class CustomerDetailViewModel(
    private val customerRepository: CustomerRepository,
    private val eventRepository: EventRepository,
    private val animalRepository: AnimalRepository
) : ScreenModel {

    private val _state = MutableStateFlow(CustomerDetailState())
    val state: StateFlow<CustomerDetailState> = _state.asStateFlow()

    private val _effects = Channel<CustomerEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun handleIntent(intent: CustomerDetailIntent) {
        when (intent) {
            is CustomerDetailIntent.LoadCustomer -> loadCustomer(intent.id)
            is CustomerDetailIntent.SaveCustomer -> saveCustomer(intent.customer)
            is CustomerDetailIntent.DeleteCustomer -> deleteCustomer()
        }
    }

    private fun loadCustomer(id: String?) {
        screenModelScope.launch {
            if (id == null) {
                _state.update { it.copy(isLoading = false) }
                return@launch
            }

            combine(
                customerRepository.getCustomerById(id),
                eventRepository.getEventsByType(EventType.STATUS_CHANGE),
                eventRepository.getEventsByType(EventType.HARVEST),
                animalRepository.getAllAnimals()
            ) { customer, statusEvents, harvestEvents, animals ->
                val animalMap = animals.associateBy { it.id }
                val allSaleEvents = (statusEvents + harvestEvents)
                    .filter { event ->
                        val data = event.eventData
                        when (data) {
                            is StatusChangeEventData -> data.customerId == id
                            is HarvestEventData -> data.customerId == id
                            else -> false
                        }
                    }
                    .sortedByDescending { it.eventDate.toString() }

                val purchases = allSaleEvents.mapNotNull { event ->
                    val animal = animalMap[event.animalId] ?: return@mapNotNull null
                    val (amount, weight) = when (val data = event.eventData) {
                        is StatusChangeEventData -> Pair(data.salePrice, data.saleWeight)
                        is HarvestEventData -> {
                            val processing = (data.killFee ?: 0.0) +
                                ((data.butcherPricePerPound ?: 0.0) * (data.dressedWeight ?: 0.0))
                            val net = (data.revenue ?: 0.0) - processing
                            Pair(if (data.revenue != null) net else null, data.dressedWeight ?: data.liveWeight)
                        }
                        else -> Pair(null, null)
                    }
                    CustomerPurchase(
                        event = event,
                        animalName = animal.name ?: animal.tagId ?: "Unknown",
                        animalSpecies = animal.species.displayName,
                        totalAmount = amount,
                        weightLbs = weight
                    )
                }

                val totalRevenue = purchases.sumOf { it.totalAmount ?: 0.0 }

                Triple(customer, purchases, totalRevenue)
            }.collect { (customer, purchases, totalRevenue) ->
                _state.update {
                    it.copy(
                        customer = customer,
                        purchases = purchases,
                        totalRevenue = totalRevenue,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun saveCustomer(customer: Customer) {
        screenModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            try {
                if (customer.id.isBlank()) {
                    customerRepository.insertCustomer(customer)
                } else {
                    customerRepository.updateCustomer(customer)
                }
                _effects.send(CustomerEffect.CustomerSaved)
            } catch (e: Exception) {
                _effects.send(CustomerEffect.ShowError("Failed to save customer: ${e.message}"))
            } finally {
                _state.update { it.copy(isSaving = false) }
            }
        }
    }

    private fun deleteCustomer() {
        val id = _state.value.customer?.id ?: return
        screenModelScope.launch {
            try {
                customerRepository.deleteCustomer(id)
                _effects.send(CustomerEffect.CustomerDeleted)
            } catch (e: Exception) {
                _effects.send(CustomerEffect.ShowError("Failed to delete customer: ${e.message}"))
            }
        }
    }
}
