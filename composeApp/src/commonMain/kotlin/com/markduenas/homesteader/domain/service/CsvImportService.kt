package com.markduenas.homesteader.domain.service

import com.markduenas.homesteader.data.repository.AnimalRepository
import com.markduenas.homesteader.domain.model.Animal
import com.markduenas.homesteader.domain.model.AnimalStatus
import com.markduenas.homesteader.domain.model.Sex
import com.markduenas.homesteader.domain.model.Species
import kotlinx.datetime.LocalDate

class CsvImportService(
    private val animalRepository: AnimalRepository
) {
    /**
     * Parse CSV content and return list of animals.
     * Expected columns (order flexible, headers required):
     * - name (optional)
     * - tag_id or tagId (optional)
     * - species (required) - species key like "cattle_beef" or display name
     * - breed (optional)
     * - sex (required) - male, female, or unknown
     * - birth_date or birthDate (optional) - YYYY-MM-DD format
     * - status (optional) - active, sold, deceased, transferred
     * - notes (optional)
     */
    fun parseCsv(csvContent: String): CsvParseResult {
        val lines = csvContent.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) {
            return CsvParseResult.Error("CSV is empty")
        }

        // Parse header row
        val headers = parseRow(lines[0]).map { it.lowercase().trim().replace(" ", "_") }

        // Find column indices
        val nameIndex = headers.indexOfFirst { it == "name" }
        val tagIdIndex = headers.indexOfFirst { it == "tag_id" || it == "tagid" }
        val speciesIndex = headers.indexOfFirst { it == "species" }
        val breedIndex = headers.indexOfFirst { it == "breed" }
        val sexIndex = headers.indexOfFirst { it == "sex" }
        val birthDateIndex = headers.indexOfFirst { it == "birth_date" || it == "birthdate" }
        val statusIndex = headers.indexOfFirst { it == "status" }
        val notesIndex = headers.indexOfFirst { it == "notes" }

        if (speciesIndex == -1) {
            return CsvParseResult.Error("Missing required 'species' column")
        }
        if (sexIndex == -1) {
            return CsvParseResult.Error("Missing required 'sex' column")
        }

        val animals = mutableListOf<Animal>()
        val errors = mutableListOf<String>()

        // Parse data rows
        for (i in 1 until lines.size) {
            val row = parseRow(lines[i])
            if (row.isEmpty()) continue

            try {
                val name = if (nameIndex >= 0 && nameIndex < row.size) row[nameIndex].takeIf { it.isNotBlank() } else null
                val tagId = if (tagIdIndex >= 0 && tagIdIndex < row.size) row[tagIdIndex].takeIf { it.isNotBlank() } else null
                val speciesStr = if (speciesIndex < row.size) row[speciesIndex].trim() else ""
                val breed = if (breedIndex >= 0 && breedIndex < row.size) row[breedIndex].takeIf { it.isNotBlank() } else null
                val sexStr = if (sexIndex < row.size) row[sexIndex].trim() else ""
                val birthDateStr = if (birthDateIndex >= 0 && birthDateIndex < row.size) row[birthDateIndex].takeIf { it.isNotBlank() } else null
                val statusStr = if (statusIndex >= 0 && statusIndex < row.size) row[statusIndex].takeIf { it.isNotBlank() } else null
                val notes = if (notesIndex >= 0 && notesIndex < row.size) row[notesIndex].takeIf { it.isNotBlank() } else null

                // Parse species
                val species = parseSpecies(speciesStr)
                if (species == null) {
                    errors.add("Row ${i + 1}: Invalid species '$speciesStr'")
                    continue
                }

                // Parse sex
                val sex = parseSex(sexStr)
                if (sex == null) {
                    errors.add("Row ${i + 1}: Invalid sex '$sexStr'")
                    continue
                }

                // Parse birth date
                val birthDate = birthDateStr?.let {
                    try {
                        LocalDate.parse(it)
                    } catch (e: Exception) {
                        errors.add("Row ${i + 1}: Invalid date format '$it' (expected YYYY-MM-DD)")
                        null
                    }
                }

                // Parse status
                val status = statusStr?.let { parseStatus(it) } ?: AnimalStatus.ACTIVE

                val animal = Animal(
                    id = "", // Will be generated on insert
                    name = name,
                    tagId = tagId,
                    species = species,
                    breed = breed,
                    sex = sex,
                    birthDate = birthDate,
                    status = status,
                    notes = notes
                )

                animals.add(animal)
            } catch (e: Exception) {
                errors.add("Row ${i + 1}: ${e.message}")
            }
        }

        return if (animals.isEmpty() && errors.isNotEmpty()) {
            CsvParseResult.Error("No valid animals found. Errors:\n${errors.joinToString("\n")}")
        } else {
            CsvParseResult.Success(
                animals = animals,
                errors = errors
            )
        }
    }

    suspend fun importAnimals(animals: List<Animal>): ImportResult {
        var imported = 0
        val errors = mutableListOf<String>()

        animals.forEachIndexed { index, animal ->
            try {
                animalRepository.insertAnimal(animal)
                imported++
            } catch (e: Exception) {
                errors.add("Animal ${index + 1} (${animal.displayName}): ${e.message}")
            }
        }

        return ImportResult(
            importedCount = imported,
            errors = errors
        )
    }

    private fun parseRow(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false

        for (char in line) {
            when {
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    result.add(current.toString().trim())
                    current = StringBuilder()
                }
                else -> current.append(char)
            }
        }
        result.add(current.toString().trim())

        return result
    }

    private fun parseSpecies(value: String): Species? {
        // Try by key first
        val byKey = Species.entries.find { it.key.equals(value, ignoreCase = true) }
        if (byKey != null) return byKey

        // Try by display name
        val byName = Species.entries.find { it.displayName.equals(value, ignoreCase = true) }
        if (byName != null) return byName

        // Try partial match
        val partial = Species.entries.find {
            it.key.contains(value, ignoreCase = true) ||
            it.displayName.contains(value, ignoreCase = true)
        }
        return partial
    }

    private fun parseSex(value: String): Sex? {
        return when (value.lowercase()) {
            "male", "m", "bull", "buck", "ram", "boar", "tom", "rooster", "drake", "jack" -> Sex.MALE
            "female", "f", "cow", "doe", "ewe", "sow", "hen", "duck" -> Sex.FEMALE
            "unknown", "u", "" -> Sex.UNKNOWN
            else -> null
        }
    }

    private fun parseStatus(value: String): AnimalStatus {
        return when (value.lowercase()) {
            "active", "alive", "a" -> AnimalStatus.ACTIVE
            "sold", "s" -> AnimalStatus.SOLD
            "deceased", "dead", "d" -> AnimalStatus.DECEASED
            "transferred", "t" -> AnimalStatus.TRANSFERRED
            else -> AnimalStatus.ACTIVE
        }
    }

    fun generateCsvTemplate(): String {
        return """name,tag_id,species,breed,sex,birth_date,status,notes
"Bessie","001","cattle_beef","Angus","female","2022-03-15","active","Example cow"
"Max","002","goat_dairy","Nubian","male","2023-06-20","active",""
"""
    }
}

sealed class CsvParseResult {
    data class Success(
        val animals: List<Animal>,
        val errors: List<String> = emptyList()
    ) : CsvParseResult()

    data class Error(val message: String) : CsvParseResult()
}

data class ImportResult(
    val importedCount: Int,
    val errors: List<String>
)
