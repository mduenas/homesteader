package com.markduenas.homesteader.data.database

import com.markduenas.homesteader.core.util.DateTimeUtil
import com.markduenas.homesteader.domain.model.Animal
import com.markduenas.homesteader.domain.model.AnimalStatus
import com.markduenas.homesteader.domain.model.Sex
import com.markduenas.homesteader.domain.model.Species
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

fun AnimalEntity.toDomain(): Animal {
    return Animal(
        id = id,
        tagId = tag_id,
        name = name,
        species = Species.fromKey(species),
        breed = breed,
        sex = parseSex(sex),
        birthDate = DateTimeUtil.parseIsoDate(birth_date),
        acquisitionDate = DateTimeUtil.parseIsoDate(acquisition_date),
        status = parseStatus(status),
        motherId = mother_id,
        fatherId = father_id,
        notes = notes,
        photoUri = photo_uri,
        customFields = parseCustomFields(custom_fields)
    )
}

fun Animal.toEntity(
    createdAt: String,
    updatedAt: String
): AnimalEntity {
    return AnimalEntity(
        id = id,
        tag_id = tagId,
        name = name,
        species = species.key,
        breed = breed,
        sex = sex.name,
        birth_date = birthDate?.toString(),
        acquisition_date = acquisitionDate?.toString(),
        status = status.name.lowercase(),
        mother_id = motherId,
        father_id = fatherId,
        notes = notes,
        photo_uri = photoUri,
        custom_fields = serializeCustomFields(customFields),
        created_at = createdAt,
        updated_at = updatedAt
    )
}

private fun parseSex(value: String): Sex {
    return try {
        Sex.valueOf(value.uppercase())
    } catch (e: Exception) {
        Sex.UNKNOWN
    }
}

private fun parseStatus(value: String): AnimalStatus {
    return try {
        AnimalStatus.valueOf(value.uppercase())
    } catch (e: Exception) {
        AnimalStatus.ACTIVE
    }
}

private fun parseCustomFields(jsonString: String?): Map<String, String> {
    if (jsonString.isNullOrBlank()) return emptyMap()
    return try {
        json.decodeFromString<Map<String, String>>(jsonString)
    } catch (e: Exception) {
        emptyMap()
    }
}

private fun serializeCustomFields(fields: Map<String, String>): String? {
    if (fields.isEmpty()) return null
    return try {
        json.encodeToString(MapSerializer(String.serializer(), String.serializer()), fields)
    } catch (e: Exception) {
        null
    }
}
