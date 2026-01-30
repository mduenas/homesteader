package com.markduenas.homesteader.fakes

import com.markduenas.homesteader.domain.model.Animal
import com.markduenas.homesteader.domain.model.AnimalStatus
import com.markduenas.homesteader.domain.model.Species
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Fake implementation of AnimalRepository for testing.
 */
class FakeAnimalRepository {

    private val _animals = MutableStateFlow<List<Animal>>(emptyList())

    val animals: Flow<List<Animal>> = _animals

    fun getAllAnimals(): Flow<List<Animal>> = _animals

    fun getAnimalsByStatus(status: AnimalStatus): Flow<List<Animal>> =
        _animals.map { animals -> animals.filter { it.status == status } }

    fun getAnimalsBySpecies(species: Species): Flow<List<Animal>> =
        _animals.map { animals -> animals.filter { it.species == species } }

    fun getAnimalById(id: String): Flow<Animal?> =
        _animals.map { animals -> animals.find { it.id == id } }

    fun searchAnimals(query: String): Flow<List<Animal>> =
        _animals.map { animals ->
            animals.filter { animal ->
                animal.displayName.contains(query, ignoreCase = true) ||
                        animal.tagId?.contains(query, ignoreCase = true) == true ||
                        animal.breed?.contains(query, ignoreCase = true) == true
            }
        }

    suspend fun insertAnimal(animal: Animal) {
        _animals.update { currentList ->
            currentList + animal
        }
    }

    suspend fun updateAnimal(animal: Animal) {
        _animals.update { currentList ->
            currentList.map { if (it.id == animal.id) animal else it }
        }
    }

    suspend fun deleteAnimal(id: String) {
        _animals.update { currentList ->
            currentList.filter { it.id != id }
        }
    }

    fun getAnimalCount(): Flow<Long> =
        _animals.map { it.size.toLong() }

    fun getAnimalCountByStatus(status: AnimalStatus): Flow<Long> =
        _animals.map { animals -> animals.count { it.status == status }.toLong() }

    fun getAnimalCountBySpecies(species: Species): Flow<Long> =
        _animals.map { animals -> animals.count { it.species == species }.toLong() }

    // Test helpers
    fun setAnimals(animals: List<Animal>) {
        _animals.value = animals
    }

    fun clear() {
        _animals.value = emptyList()
    }
}
