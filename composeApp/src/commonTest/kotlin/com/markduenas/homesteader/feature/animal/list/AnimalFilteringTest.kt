package com.markduenas.homesteader.feature.animal.list

import com.markduenas.homesteader.TestData
import com.markduenas.homesteader.domain.model.Animal
import com.markduenas.homesteader.domain.model.AnimalStatus
import com.markduenas.homesteader.domain.model.Species
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for animal filtering logic.
 * These tests verify the filtering behavior used in AnimalListViewModel.
 */
class AnimalFilteringTest {

    private val testAnimals = listOf(
        TestData.createAnimal(
            id = "1",
            name = "Bessie",
            tagId = "COW001",
            species = Species.CATTLE_BEEF,
            breed = "Holstein",
            status = AnimalStatus.ACTIVE
        ),
        TestData.createAnimal(
            id = "2",
            name = "Daisy",
            tagId = "COW002",
            species = Species.CATTLE_DAIRY,
            breed = "Angus",
            status = AnimalStatus.ACTIVE
        ),
        TestData.createAnimal(
            id = "3",
            name = "Billy",
            tagId = "GOAT001",
            species = Species.GOAT_MEAT,
            breed = "Boer",
            status = AnimalStatus.ACTIVE
        ),
        TestData.createAnimal(
            id = "4",
            name = "Clucky",
            tagId = "CHICK001",
            species = Species.CHICKEN_LAYER,
            breed = "Rhode Island Red",
            status = AnimalStatus.SOLD
        ),
        TestData.createAnimal(
            id = "5",
            name = null,
            tagId = "PIG001",
            species = Species.PIG,
            breed = "Yorkshire",
            status = AnimalStatus.DECEASED
        )
    )

    // Mimics the filter logic from AnimalListViewModel
    private fun applyFilters(
        animals: List<Animal>,
        searchQuery: String = "",
        selectedSpecies: Species? = null,
        selectedStatus: AnimalStatus? = null
    ): List<Animal> {
        var filtered = animals

        if (searchQuery.isNotBlank()) {
            val query = searchQuery.lowercase()
            filtered = filtered.filter { animal ->
                animal.displayName.lowercase().contains(query) ||
                        animal.tagId?.lowercase()?.contains(query) == true ||
                        animal.breed?.lowercase()?.contains(query) == true
            }
        }

        selectedSpecies?.let { species ->
            filtered = filtered.filter { it.species == species }
        }

        selectedStatus?.let { status ->
            filtered = filtered.filter { it.status == status }
        }

        return filtered
    }

    @Test
    fun `no filters returns all animals`() {
        val result = applyFilters(testAnimals)
        assertEquals(5, result.size)
    }

    @Test
    fun `search by name finds matching animals`() {
        val result = applyFilters(testAnimals, searchQuery = "Bessie")
        assertEquals(1, result.size)
        assertEquals("Bessie", result.first().name)
    }

    @Test
    fun `search by name is case insensitive`() {
        val result = applyFilters(testAnimals, searchQuery = "bessie")
        assertEquals(1, result.size)
        assertEquals("Bessie", result.first().name)
    }

    @Test
    fun `search by partial name finds matching animals`() {
        val result = applyFilters(testAnimals, searchQuery = "ess")
        assertEquals(1, result.size)
        assertEquals("Bessie", result.first().name)
    }

    @Test
    fun `search by tag ID finds matching animals`() {
        val result = applyFilters(testAnimals, searchQuery = "COW001")
        assertEquals(1, result.size)
        assertEquals("Bessie", result.first().name)
    }

    @Test
    fun `search by partial tag ID finds matching animals`() {
        val result = applyFilters(testAnimals, searchQuery = "COW")
        assertEquals(2, result.size)
        assertTrue(result.all { it.tagId?.contains("COW") == true })
    }

    @Test
    fun `search by breed finds matching animals`() {
        val result = applyFilters(testAnimals, searchQuery = "Holstein")
        assertEquals(1, result.size)
        assertEquals("Bessie", result.first().name)
    }

    @Test
    fun `search by breed is case insensitive`() {
        val result = applyFilters(testAnimals, searchQuery = "holstein")
        assertEquals(1, result.size)
    }

    @Test
    fun `search with no matches returns empty list`() {
        val result = applyFilters(testAnimals, searchQuery = "NonExistentAnimal")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `filter by species returns only matching species`() {
        val result = applyFilters(testAnimals, selectedSpecies = Species.CATTLE_BEEF)
        assertEquals(1, result.size)
        assertTrue(result.all { it.species == Species.CATTLE_BEEF })
    }

    @Test
    fun `filter by status returns only matching status`() {
        val result = applyFilters(testAnimals, selectedStatus = AnimalStatus.ACTIVE)
        assertEquals(3, result.size)
        assertTrue(result.all { it.status == AnimalStatus.ACTIVE })
    }

    @Test
    fun `filter by sold status finds sold animals`() {
        val result = applyFilters(testAnimals, selectedStatus = AnimalStatus.SOLD)
        assertEquals(1, result.size)
        assertEquals("Clucky", result.first().name)
    }

    @Test
    fun `filter by deceased status finds deceased animals`() {
        val result = applyFilters(testAnimals, selectedStatus = AnimalStatus.DECEASED)
        assertEquals(1, result.size)
        assertEquals("PIG001", result.first().displayName) // Uses tag since name is null
    }

    @Test
    fun `combined search and species filter works`() {
        val result = applyFilters(
            testAnimals,
            searchQuery = "COW",
            selectedSpecies = Species.CATTLE_BEEF
        )
        assertEquals(1, result.size)
        assertEquals(Species.CATTLE_BEEF, result.first().species)
    }

    @Test
    fun `combined search and status filter works`() {
        val result = applyFilters(
            testAnimals,
            searchQuery = "Bessie",
            selectedStatus = AnimalStatus.ACTIVE
        )
        assertEquals(1, result.size)
        assertEquals("Bessie", result.first().name)
    }

    @Test
    fun `combined species and status filter works`() {
        val result = applyFilters(
            testAnimals,
            selectedSpecies = Species.CATTLE_BEEF,
            selectedStatus = AnimalStatus.ACTIVE
        )
        assertEquals(1, result.size)
        assertTrue(result.all { it.species == Species.CATTLE_BEEF && it.status == AnimalStatus.ACTIVE })
    }

    @Test
    fun `all three filters combined works`() {
        val result = applyFilters(
            testAnimals,
            searchQuery = "Holstein",
            selectedSpecies = Species.CATTLE_BEEF,
            selectedStatus = AnimalStatus.ACTIVE
        )
        assertEquals(1, result.size)
        assertEquals("Bessie", result.first().name)
    }

    @Test
    fun `conflicting filters return empty list`() {
        // Search for Bessie but filter by SOLD status
        val result = applyFilters(
            testAnimals,
            searchQuery = "Bessie",
            selectedStatus = AnimalStatus.SOLD
        )
        assertTrue(result.isEmpty())
    }

    @Test
    fun `empty search query does not filter`() {
        val result = applyFilters(testAnimals, searchQuery = "")
        assertEquals(5, result.size)
    }

    @Test
    fun `whitespace only search query does not filter`() {
        val result = applyFilters(testAnimals, searchQuery = "   ")
        assertEquals(5, result.size)
    }

    @Test
    fun `available species are extracted correctly`() {
        val availableSpecies = testAnimals.map { it.species }.distinct().sortedBy { it.displayName }
        assertEquals(5, availableSpecies.size)
        assertTrue(availableSpecies.contains(Species.CATTLE_BEEF))
        assertTrue(availableSpecies.contains(Species.CATTLE_DAIRY))
        assertTrue(availableSpecies.contains(Species.GOAT_MEAT))
        assertTrue(availableSpecies.contains(Species.CHICKEN_LAYER))
        assertTrue(availableSpecies.contains(Species.PIG))
    }
}
