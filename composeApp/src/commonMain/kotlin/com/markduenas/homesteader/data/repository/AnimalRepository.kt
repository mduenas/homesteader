package com.markduenas.homesteader.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.benasher44.uuid.uuid4
import com.markduenas.homesteader.core.util.DateTimeUtil
import com.markduenas.homesteader.data.database.AnimalQueries
import com.markduenas.homesteader.data.database.toDomain
import com.markduenas.homesteader.domain.model.Animal
import com.markduenas.homesteader.domain.model.AnimalStatus
import com.markduenas.homesteader.domain.model.Species
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

class AnimalRepository(
    private val queries: AnimalQueries
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val dispatcher = Dispatchers.IO

    fun getAllAnimals(): Flow<List<Animal>> {
        return queries.selectAll()
            .asFlow()
            .mapToList(dispatcher)
            .map { entities -> entities.map { it.toDomain() } }
    }

    fun getAnimalsBySpecies(species: Species): Flow<List<Animal>> {
        return queries.selectBySpecies(species.key)
            .asFlow()
            .mapToList(dispatcher)
            .map { entities -> entities.map { it.toDomain() } }
    }

    fun getAnimalsByStatus(status: AnimalStatus): Flow<List<Animal>> {
        return queries.selectByStatus(status.name.lowercase())
            .asFlow()
            .mapToList(dispatcher)
            .map { entities -> entities.map { it.toDomain() } }
    }

    fun getAnimalById(id: String): Flow<Animal?> {
        return queries.selectById(id)
            .asFlow()
            .mapToOneOrNull(dispatcher)
            .map { it?.toDomain() }
    }

    fun getOffspringByMother(motherId: String): Flow<List<Animal>> {
        return queries.selectByMother(motherId)
            .asFlow()
            .mapToList(dispatcher)
            .map { entities -> entities.map { it.toDomain() } }
    }

    fun getOffspringByFather(fatherId: String): Flow<List<Animal>> {
        return queries.selectByFather(fatherId)
            .asFlow()
            .mapToList(dispatcher)
            .map { entities -> entities.map { it.toDomain() } }
    }

    fun searchAnimals(query: String): Flow<List<Animal>> {
        return queries.searchAnimals(query, query)
            .asFlow()
            .mapToList(dispatcher)
            .map { entities -> entities.map { it.toDomain() } }
    }

    suspend fun insertAnimal(animal: Animal): String = withContext(dispatcher) {
        val id = animal.id.ifBlank { uuid4().toString() }
        val now = DateTimeUtil.nowIsoString()
        val customFieldsJson = if (animal.customFields.isEmpty()) null else {
            json.encodeToString(MapSerializer(String.serializer(), String.serializer()), animal.customFields)
        }

        queries.insert(
            id = id,
            tag_id = animal.tagId,
            name = animal.name,
            species = animal.species.key,
            breed = animal.breed,
            sex = animal.sex.name,
            birth_date = animal.birthDate?.toString(),
            acquisition_date = animal.acquisitionDate?.toString(),
            status = animal.status.name.lowercase(),
            mother_id = animal.motherId,
            father_id = animal.fatherId,
            notes = animal.notes,
            photo_uri = animal.photoUri,
            custom_fields = customFieldsJson,
            created_at = now,
            updated_at = now
        )

        id
    }

    suspend fun updateAnimal(animal: Animal) = withContext(dispatcher) {
        val now = DateTimeUtil.nowIsoString()
        val customFieldsJson = if (animal.customFields.isEmpty()) null else {
            json.encodeToString(MapSerializer(String.serializer(), String.serializer()), animal.customFields)
        }

        queries.update(
            tag_id = animal.tagId,
            name = animal.name,
            species = animal.species.key,
            breed = animal.breed,
            sex = animal.sex.name,
            birth_date = animal.birthDate?.toString(),
            acquisition_date = animal.acquisitionDate?.toString(),
            status = animal.status.name.lowercase(),
            mother_id = animal.motherId,
            father_id = animal.fatherId,
            notes = animal.notes,
            photo_uri = animal.photoUri,
            custom_fields = customFieldsJson,
            updated_at = now,
            id = animal.id
        )
    }

    suspend fun updateAnimalStatus(id: String, status: AnimalStatus) = withContext(dispatcher) {
        val now = DateTimeUtil.nowIsoString()
        queries.updateStatus(
            status = status.name.lowercase(),
            updated_at = now,
            id = id
        )
    }

    suspend fun deleteAnimal(id: String) = withContext(dispatcher) {
        queries.delete(id)
    }

    suspend fun getActiveAnimalCount(): Long = withContext(dispatcher) {
        queries.countActive().executeAsOne()
    }

    suspend fun getAnimalCountBySpecies(): Map<String, Long> = withContext(dispatcher) {
        queries.countBySpecies().executeAsList().associate { it.species to it.count }
    }
}
