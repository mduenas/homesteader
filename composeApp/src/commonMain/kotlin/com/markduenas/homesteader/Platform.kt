package com.markduenas.homesteader

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform