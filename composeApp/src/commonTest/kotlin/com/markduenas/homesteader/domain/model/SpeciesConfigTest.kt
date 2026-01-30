package com.markduenas.homesteader.domain.model

import com.markduenas.homesteader.TestData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SpeciesConfigTest {

    @Test
    fun `default config has expected values`() {
        val config = TestData.createSpeciesConfig()

        assertEquals("cattle_beef", config.speciesKey)
        assertEquals("Cattle (Beef)", config.displayName)
        assertTrue(config.isEnabled)
        assertTrue(config.trackBreeding)
        assertTrue(config.trackPregnancy)
        assertTrue(config.trackHealth)
        assertFalse(config.trackMilkProduction)
        assertFalse(config.trackEggProduction)
        assertTrue(config.trackWeight)
        assertFalse(config.trackFeed)
    }

    @Test
    fun `cattle has correct default gestation period`() {
        val config = TestData.createSpeciesConfig(
            speciesKey = "cattle_beef",
            gestationDays = 283
        )

        assertEquals(283, config.gestationDays)
    }

    @Test
    fun `cattle has correct default heat cycle`() {
        val config = TestData.createSpeciesConfig(
            speciesKey = "cattle_beef",
            heatCycleDays = 21
        )

        assertEquals(21, config.heatCycleDays)
    }

    @Test
    fun `goat config can have different values`() {
        val config = TestData.createSpeciesConfig(
            speciesKey = "goats_meat",
            displayName = "Goats (Meat)",
            gestationDays = 150,
            heatCycleDays = 21
        )

        assertEquals("goats_meat", config.speciesKey)
        assertEquals("Goats (Meat)", config.displayName)
        assertEquals(150, config.gestationDays)
        assertEquals(21, config.heatCycleDays)
    }

    @Test
    fun `pig config can have different values`() {
        val config = TestData.createSpeciesConfig(
            speciesKey = "pigs",
            displayName = "Pigs",
            gestationDays = 114,
            heatCycleDays = 21
        )

        assertEquals("pigs", config.speciesKey)
        assertEquals(114, config.gestationDays)
    }

    @Test
    fun `sheep config can have different values`() {
        val config = TestData.createSpeciesConfig(
            speciesKey = "sheep",
            displayName = "Sheep",
            gestationDays = 147,
            heatCycleDays = 17
        )

        assertEquals("sheep", config.speciesKey)
        assertEquals(147, config.gestationDays)
        assertEquals(17, config.heatCycleDays)
    }

    @Test
    fun `chicken config can track egg production`() {
        val config = TestData.createSpeciesConfig(
            speciesKey = "chickens_layers",
            displayName = "Chickens (Layers)",
            trackEggProduction = true,
            trackBreeding = false
        )

        assertTrue(config.trackEggProduction)
        assertFalse(config.trackBreeding)
    }

    @Test
    fun `dairy config can track milk production`() {
        val config = TestData.createSpeciesConfig(
            speciesKey = "cattle_dairy",
            displayName = "Cattle (Dairy)",
            trackMilkProduction = true
        )

        assertTrue(config.trackMilkProduction)
    }

    @Test
    fun `disabled species config`() {
        val config = TestData.createSpeciesConfig(isEnabled = false)

        assertFalse(config.isEnabled)
    }

    @Test
    fun `config with all tracking disabled`() {
        val config = TestData.createSpeciesConfig(
            trackBreeding = false,
            trackPregnancy = false,
            trackHealth = false,
            trackMilkProduction = false,
            trackEggProduction = false,
            trackWeight = false,
            trackFeed = false
        )

        assertFalse(config.trackBreeding)
        assertFalse(config.trackPregnancy)
        assertFalse(config.trackHealth)
        assertFalse(config.trackMilkProduction)
        assertFalse(config.trackEggProduction)
        assertFalse(config.trackWeight)
        assertFalse(config.trackFeed)
    }

    @Test
    fun `config with all tracking enabled`() {
        val config = TestData.createSpeciesConfig(
            trackBreeding = true,
            trackPregnancy = true,
            trackHealth = true,
            trackMilkProduction = true,
            trackEggProduction = true,
            trackWeight = true,
            trackFeed = true
        )

        assertTrue(config.trackBreeding)
        assertTrue(config.trackPregnancy)
        assertTrue(config.trackHealth)
        assertTrue(config.trackMilkProduction)
        assertTrue(config.trackEggProduction)
        assertTrue(config.trackWeight)
        assertTrue(config.trackFeed)
    }

    @Test
    fun `custom fields schema defaults to empty list`() {
        val config = TestData.createSpeciesConfig()
        assertTrue(config.customFieldsSchema.isEmpty())
    }

    @Test
    fun `config can have null gestation days`() {
        val config = TestData.createSpeciesConfig(
            gestationDays = null,
            heatCycleDays = null,
            weaningAgeDays = null
        )

        assertNull(config.gestationDays)
        assertNull(config.heatCycleDays)
        assertNull(config.weaningAgeDays)
    }
}

class DefaultSpeciesConfigsTest {

    @Test
    fun `all default configs exist`() {
        assertTrue(DefaultSpeciesConfigs.ALL_DEFAULTS.isNotEmpty())
    }

    @Test
    fun `cattle beef config has correct values`() {
        val config = DefaultSpeciesConfigs.CATTLE_BEEF

        assertEquals("cattle_beef", config.speciesKey)
        assertEquals("Cattle (Beef)", config.displayName)
        assertTrue(config.trackBreeding)
        assertTrue(config.trackPregnancy)
        assertTrue(config.trackWeight)
        assertTrue(config.trackHealth)
        assertEquals(283, config.gestationDays)
        assertEquals(21, config.heatCycleDays)
        assertEquals(205, config.weaningAgeDays)
    }

    @Test
    fun `cattle dairy config tracks milk production`() {
        val config = DefaultSpeciesConfigs.CATTLE_DAIRY

        assertTrue(config.trackMilkProduction)
        assertTrue(config.trackBreeding)
    }

    @Test
    fun `layer chickens track egg production`() {
        val config = DefaultSpeciesConfigs.CHICKENS_LAYERS

        assertTrue(config.trackEggProduction)
        assertTrue(config.trackHealth)
        assertFalse(config.trackBreeding)
    }

    @Test
    fun `broiler chickens track weight and feed`() {
        val config = DefaultSpeciesConfigs.CHICKENS_BROILERS

        assertTrue(config.trackWeight)
        assertTrue(config.trackFeed)
        assertTrue(config.trackHealth)
    }

    @Test
    fun `pigs have correct gestation period`() {
        val config = DefaultSpeciesConfigs.PIGS

        assertEquals(114, config.gestationDays)
        assertEquals(28, config.weaningAgeDays)
    }

    @Test
    fun `sheep have correct cycle length`() {
        val config = DefaultSpeciesConfigs.SHEEP

        assertEquals(17, config.heatCycleDays)
        assertEquals(147, config.gestationDays)
    }

    @Test
    fun `rabbits have short gestation`() {
        val config = DefaultSpeciesConfigs.RABBITS

        assertEquals(31, config.gestationDays)
    }

    @Test
    fun `horses have long gestation`() {
        val config = DefaultSpeciesConfigs.HORSES

        assertEquals(340, config.gestationDays)
    }
}
