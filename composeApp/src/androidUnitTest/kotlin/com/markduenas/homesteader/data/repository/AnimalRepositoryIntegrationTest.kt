package com.markduenas.homesteader.data.repository

import com.markduenas.homesteader.domain.model.Animal
import com.markduenas.homesteader.domain.model.AnimalStatus
import com.markduenas.homesteader.domain.model.Sex
import com.markduenas.homesteader.domain.model.Species
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Integration tests for AnimalRepository using a real in-memory SQLite database.
 */
class AnimalRepositoryIntegrationTest {

    private lateinit var repository: AnimalRepository

    @Before
    fun setup() {
        val database = TestDatabaseHelper.createInMemoryDatabase()
        repository = AnimalRepository(database.animalQueries)
    }

    private fun createTestAnimal(
        id: String = "test-id",
        name: String? = "Test Cow",
        tagId: String? = "TAG001",
        species: Species = Species.CATTLE_BEEF,
        breed: String? = "Angus",
        sex: Sex = Sex.FEMALE,
        status: AnimalStatus = AnimalStatus.ACTIVE,
        motherId: String? = null,
        fatherId: String? = null
    ): Animal = Animal(
        id = id,
        tagId = tagId,
        name = name,
        species = species,
        breed = breed,
        sex = sex,
        birthDate = LocalDate(2022, 1, 15),
        acquisitionDate = LocalDate(2022, 3, 1),
        status = status,
        motherId = motherId,
        fatherId = fatherId,
        notes = "Test notes",
        photoUri = null,
        customFields = emptyMap()
    )

    @Test
    fun `insert and retrieve animal by id`() = runTest {
        val animal = createTestAnimal(id = "animal-1")

        val insertedId = repository.insertAnimal(animal)
        assertEquals("animal-1", insertedId)

        val retrieved = repository.getAnimalById("animal-1").first()
        assertNotNull(retrieved)
        assertEquals("animal-1", retrieved.id)
        assertEquals("Test Cow", retrieved.name)
        assertEquals("TAG001", retrieved.tagId)
        assertEquals(Species.CATTLE_BEEF, retrieved.species)
        assertEquals("Angus", retrieved.breed)
        assertEquals(Sex.FEMALE, retrieved.sex)
        assertEquals(AnimalStatus.ACTIVE, retrieved.status)
    }

    @Test
    fun `get all animals returns inserted animals`() = runTest {
        repository.insertAnimal(createTestAnimal(id = "animal-1", name = "Bessie"))
        repository.insertAnimal(createTestAnimal(id = "animal-2", name = "Daisy"))
        repository.insertAnimal(createTestAnimal(id = "animal-3", name = "Clover"))

        val animals = repository.getAllAnimals().first()
        assertEquals(3, animals.size)
        assertTrue(animals.any { it.name == "Bessie" })
        assertTrue(animals.any { it.name == "Daisy" })
        assertTrue(animals.any { it.name == "Clover" })
    }

    @Test
    fun `get animals by species filters correctly`() = runTest {
        repository.insertAnimal(createTestAnimal(id = "cow-1", species = Species.CATTLE_BEEF))
        repository.insertAnimal(createTestAnimal(id = "cow-2", species = Species.CATTLE_DAIRY))
        repository.insertAnimal(createTestAnimal(id = "goat-1", species = Species.GOAT_MEAT))

        val beefCattle = repository.getAnimalsBySpecies(Species.CATTLE_BEEF).first()
        assertEquals(1, beefCattle.size)
        assertEquals("cow-1", beefCattle.first().id)
    }

    @Test
    fun `get animals by status filters correctly`() = runTest {
        repository.insertAnimal(createTestAnimal(id = "active-1", status = AnimalStatus.ACTIVE))
        repository.insertAnimal(createTestAnimal(id = "active-2", status = AnimalStatus.ACTIVE))
        repository.insertAnimal(createTestAnimal(id = "sold-1", status = AnimalStatus.SOLD))
        repository.insertAnimal(createTestAnimal(id = "deceased-1", status = AnimalStatus.DECEASED))

        val activeAnimals = repository.getAnimalsByStatus(AnimalStatus.ACTIVE).first()
        assertEquals(2, activeAnimals.size)

        val soldAnimals = repository.getAnimalsByStatus(AnimalStatus.SOLD).first()
        assertEquals(1, soldAnimals.size)
        assertEquals("sold-1", soldAnimals.first().id)
    }

    @Test
    fun `update animal persists changes`() = runTest {
        val animal = createTestAnimal(id = "animal-1", name = "Original Name")
        repository.insertAnimal(animal)

        val updated = animal.copy(name = "Updated Name", breed = "Hereford")
        repository.updateAnimal(updated)

        val retrieved = repository.getAnimalById("animal-1").first()
        assertNotNull(retrieved)
        assertEquals("Updated Name", retrieved.name)
        assertEquals("Hereford", retrieved.breed)
    }

    @Test
    fun `update animal status changes status only`() = runTest {
        repository.insertAnimal(createTestAnimal(id = "animal-1", status = AnimalStatus.ACTIVE))

        repository.updateAnimalStatus("animal-1", AnimalStatus.SOLD)

        val retrieved = repository.getAnimalById("animal-1").first()
        assertNotNull(retrieved)
        assertEquals(AnimalStatus.SOLD, retrieved.status)
    }

    @Test
    fun `delete animal removes from database`() = runTest {
        repository.insertAnimal(createTestAnimal(id = "animal-1"))

        val beforeDelete = repository.getAnimalById("animal-1").first()
        assertNotNull(beforeDelete)

        repository.deleteAnimal("animal-1")

        val afterDelete = repository.getAnimalById("animal-1").first()
        assertNull(afterDelete)
    }

    @Test
    fun `search animals finds by name`() = runTest {
        repository.insertAnimal(createTestAnimal(id = "1", name = "Bessie"))
        repository.insertAnimal(createTestAnimal(id = "2", name = "Daisy"))
        repository.insertAnimal(createTestAnimal(id = "3", name = "Betty"))

        val results = repository.searchAnimals("ess").first()
        assertEquals(1, results.size)
        assertEquals("Bessie", results.first().name)
    }

    @Test
    fun `search animals finds by tag id`() = runTest {
        repository.insertAnimal(createTestAnimal(id = "1", name = "Bessie", tagId = "COW001"))
        repository.insertAnimal(createTestAnimal(id = "2", name = "Daisy", tagId = "COW002"))
        repository.insertAnimal(createTestAnimal(id = "3", name = "Billy", tagId = "GOAT001"))

        val results = repository.searchAnimals("COW").first()
        assertEquals(2, results.size)
        assertTrue(results.all { it.tagId?.contains("COW") == true })
    }

    @Test
    fun `get offspring by mother returns children`() = runTest {
        repository.insertAnimal(createTestAnimal(id = "mother-1", name = "Mother Cow"))
        repository.insertAnimal(createTestAnimal(id = "calf-1", name = "Calf 1", motherId = "mother-1"))
        repository.insertAnimal(createTestAnimal(id = "calf-2", name = "Calf 2", motherId = "mother-1"))
        repository.insertAnimal(createTestAnimal(id = "other", name = "Other"))

        val offspring = repository.getOffspringByMother("mother-1").first()
        assertEquals(2, offspring.size)
        assertTrue(offspring.all { it.motherId == "mother-1" })
    }

    @Test
    fun `get offspring by father returns children`() = runTest {
        repository.insertAnimal(createTestAnimal(id = "father-1", name = "Bull", sex = Sex.MALE))
        repository.insertAnimal(createTestAnimal(id = "calf-1", name = "Calf 1", fatherId = "father-1"))
        repository.insertAnimal(createTestAnimal(id = "calf-2", name = "Calf 2", fatherId = "father-1"))

        val offspring = repository.getOffspringByFather("father-1").first()
        assertEquals(2, offspring.size)
        assertTrue(offspring.all { it.fatherId == "father-1" })
    }

    @Test
    fun `get active animal count returns correct count`() = runTest {
        repository.insertAnimal(createTestAnimal(id = "active-1", status = AnimalStatus.ACTIVE))
        repository.insertAnimal(createTestAnimal(id = "active-2", status = AnimalStatus.ACTIVE))
        repository.insertAnimal(createTestAnimal(id = "sold-1", status = AnimalStatus.SOLD))

        val count = repository.getActiveAnimalCount()
        assertEquals(2L, count)
    }

    @Test
    fun `get animal count by species returns correct counts`() = runTest {
        repository.insertAnimal(createTestAnimal(id = "cow-1", species = Species.CATTLE_BEEF, status = AnimalStatus.ACTIVE))
        repository.insertAnimal(createTestAnimal(id = "cow-2", species = Species.CATTLE_BEEF, status = AnimalStatus.ACTIVE))
        repository.insertAnimal(createTestAnimal(id = "goat-1", species = Species.GOAT_MEAT, status = AnimalStatus.ACTIVE))
        repository.insertAnimal(createTestAnimal(id = "sold-cow", species = Species.CATTLE_BEEF, status = AnimalStatus.SOLD))

        val counts = repository.getAnimalCountBySpecies()
        assertEquals(2L, counts["cattle_beef"])
        assertEquals(1L, counts["goat_meat"])
    }

    @Test
    fun `insert animal with custom fields preserves them`() = runTest {
        val animal = createTestAnimal(id = "animal-1").copy(
            customFields = mapOf("ear_tag_color" to "yellow", "pen_number" to "3")
        )
        repository.insertAnimal(animal)

        val retrieved = repository.getAnimalById("animal-1").first()
        assertNotNull(retrieved)
        assertEquals("yellow", retrieved.customFields["ear_tag_color"])
        assertEquals("3", retrieved.customFields["pen_number"])
    }

    @Test
    fun `insert generates id when blank`() = runTest {
        val animal = createTestAnimal(id = "")
        val generatedId = repository.insertAnimal(animal)

        assertTrue(generatedId.isNotBlank())

        val retrieved = repository.getAnimalById(generatedId).first()
        assertNotNull(retrieved)
    }
}
