package com.markduenas.homesteader.domain.model

import com.markduenas.homesteader.TestData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AnimalTest {

    @Test
    fun `displayName returns name when present`() {
        val animal = TestData.createAnimal(name = "Bessie", tagId = "TAG001")
        assertEquals("Bessie", animal.displayName)
    }

    @Test
    fun `displayName returns tagId when name is null`() {
        val animal = TestData.createAnimal(name = null, tagId = "TAG001")
        assertEquals("TAG001", animal.displayName)
    }

    @Test
    fun `displayName returns Unnamed when both name and tagId are null`() {
        val animal = TestData.createAnimal(name = null, tagId = null)
        assertEquals("Unnamed", animal.displayName)
    }

    @Test
    fun `animal with ACTIVE status is active`() {
        val animal = TestData.createAnimal(status = AnimalStatus.ACTIVE)
        assertEquals(AnimalStatus.ACTIVE, animal.status)
    }

    @Test
    fun `animal species is correctly assigned`() {
        val cattleAnimal = TestData.createAnimal(species = Species.CATTLE_BEEF)
        val chickenAnimal = TestData.createAnimal(species = Species.CHICKEN_LAYER)

        assertEquals(Species.CATTLE_BEEF, cattleAnimal.species)
        assertEquals(Species.CHICKEN_LAYER, chickenAnimal.species)
    }

    @Test
    fun `animal sex values are correct`() {
        val female = TestData.createAnimal(sex = Sex.FEMALE)
        val male = TestData.createAnimal(sex = Sex.MALE)
        val unknown = TestData.createAnimal(sex = Sex.UNKNOWN)

        assertEquals(Sex.FEMALE, female.sex)
        assertEquals(Sex.MALE, male.sex)
        assertEquals(Sex.UNKNOWN, unknown.sex)
    }

    @Test
    fun `animal with parents has parent references`() {
        val animal = TestData.createAnimal(
            motherId = "mother-id",
            fatherId = "father-id"
        )

        assertEquals("mother-id", animal.motherId)
        assertEquals("father-id", animal.fatherId)
    }

    @Test
    fun `animal without parents has null references`() {
        val animal = TestData.createAnimal(motherId = null, fatherId = null)

        assertNull(animal.motherId)
        assertNull(animal.fatherId)
    }

    @Test
    fun `animal birth date is correctly stored`() {
        val animal = TestData.createAnimal()

        assertNotNull(animal.birthDate)
        assertEquals(2022, animal.birthDate?.year)
        assertEquals(1, animal.birthDate?.monthNumber)
        assertEquals(15, animal.birthDate?.dayOfMonth)
    }
}

class AnimalStatusTest {

    @Test
    fun `all status values have display names`() {
        AnimalStatus.entries.forEach { status ->
            assertTrue(status.displayName.isNotBlank(), "Status $status should have a display name")
        }
    }

    @Test
    fun `status display names are human readable`() {
        assertEquals("Active", AnimalStatus.ACTIVE.displayName)
        assertEquals("Sold", AnimalStatus.SOLD.displayName)
        assertEquals("Deceased", AnimalStatus.DECEASED.displayName)
        assertEquals("Transferred", AnimalStatus.TRANSFERRED.displayName)
    }
}

class SpeciesTest {

    @Test
    fun `all species have display names`() {
        Species.entries.forEach { species ->
            assertTrue(species.displayName.isNotBlank(), "Species $species should have a display name")
        }
    }

    @Test
    fun `all species have unique keys`() {
        val keys = Species.entries.map { it.key }
        assertEquals(keys.size, keys.distinct().size, "All species keys should be unique")
    }

    @Test
    fun `fromKey returns correct species`() {
        assertEquals(Species.CATTLE_BEEF, Species.fromKey("cattle_beef"))
        assertEquals(Species.GOAT_MEAT, Species.fromKey("goat_meat"))
        assertEquals(Species.CHICKEN_LAYER, Species.fromKey("chicken_layer"))
    }

    @Test
    fun `fromKey returns CUSTOM for unknown key`() {
        assertEquals(Species.CUSTOM, Species.fromKey("unknown_species"))
        assertEquals(Species.CUSTOM, Species.fromKey(""))
    }

    @Test
    fun `common livestock species exist`() {
        assertNotNull(Species.entries.find { it == Species.CATTLE_BEEF })
        assertNotNull(Species.entries.find { it == Species.CATTLE_DAIRY })
        assertNotNull(Species.entries.find { it == Species.GOAT_MEAT })
        assertNotNull(Species.entries.find { it == Species.GOAT_DAIRY })
        assertNotNull(Species.entries.find { it == Species.SHEEP })
        assertNotNull(Species.entries.find { it == Species.PIG })
        assertNotNull(Species.entries.find { it == Species.CHICKEN_LAYER })
        assertNotNull(Species.entries.find { it == Species.CHICKEN_BROILER })
    }

    @Test
    fun `cattle species have gestation and heat cycle data`() {
        assertNotNull(Species.CATTLE_BEEF.defaultGestationDays)
        assertNotNull(Species.CATTLE_BEEF.defaultHeatCycleDays)
        assertEquals(283, Species.CATTLE_BEEF.defaultGestationDays)
        assertEquals(21, Species.CATTLE_BEEF.defaultHeatCycleDays)
    }
}

class SexTest {

    @Test
    fun `all sex values exist`() {
        assertEquals(3, Sex.entries.size)
        assertTrue(Sex.entries.contains(Sex.MALE))
        assertTrue(Sex.entries.contains(Sex.FEMALE))
        assertTrue(Sex.entries.contains(Sex.UNKNOWN))
    }

    @Test
    fun `sex display names are correct`() {
        assertEquals("Male", Sex.MALE.displayName)
        assertEquals("Female", Sex.FEMALE.displayName)
        assertEquals("Unknown", Sex.UNKNOWN.displayName)
    }
}
