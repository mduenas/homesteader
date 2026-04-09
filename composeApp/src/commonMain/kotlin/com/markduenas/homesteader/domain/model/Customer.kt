package com.markduenas.homesteader.domain.model

data class Customer(
    val id: String = "",
    val name: String,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val notes: String? = null,
    val createdAt: String = "",
    val updatedAt: String = ""
)
