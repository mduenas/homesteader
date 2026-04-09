package com.markduenas.homesteader.data.database

import com.markduenas.homesteader.domain.model.Customer

fun CustomerEntity.toDomain(): Customer = Customer(
    id = id,
    name = name,
    phone = phone,
    email = email,
    address = address,
    notes = notes,
    createdAt = created_at,
    updatedAt = updated_at
)
