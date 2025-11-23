package com.fathan.e_commerce.models

data class Product(
    val id: Int,
    val name: String,
    val brand: String,
    val price: Double,
    val oldPrice: Double? = null,
    val rating: Double,
    val ratingCount: Int,
    val thumbnail: String, // for now just keep it as text
    val colors: List<String>,
    val storages: List<String> = emptyList(),
    val description: String,
    val category: String
)

data class CartItem(
    val product: Product,
    var quantity: Int = 1,
    val selectedColor: String? = null,
    val selectedStorage: String? = null
)

data class Category(
    val name: String,
    val iconEmoji: String // simple icon using emoji to keep UI easy
)

object DummyData {
    val categories = listOf(
        Category("Mobile", "📱"),
        Category("Headphone", "🎧"),
        Category("Tablets", "📲"),
        Category("Laptop", "💻"),
        Category("Speakers", "🔊"),
        Category("More", "⋯")
    )

    val products = listOf(
        Product(
            id = 1,
            name = "iPhone 16 Pro Max",
            brand = "Apple",
            price = 1399.99,
            oldPrice = 1499.99,
            rating = 4.9,
            ratingCount = 2200,
            thumbnail = "iphone_16_pro_max",
            colors = listOf("Desert Titanium", "Natural Titanium", "White Titanium", "Black Titanium"),
            storages = listOf("256 GB", "512 GB", "1 TB"),
            description = "6.9\" OLED, A20 Pro chip, 48MP triple camera, 5G, Dynamic Island, USB-C.",
            category = "Mobile"
        ),
        Product(
            id = 2,
            name = "Smartwatch Ultra",
            brand = "Apple",
            price = 99.99,
            rating = 4.7,
            ratingCount = 1250,
            thumbnail = "smartwatch_ultra",
            colors = listOf("Black", "Starlight"),
            description = "Rugged design, heart rate, GPS, 100m water resistant, 72-hour battery life.",
            category = "Wearable"
        ),
        Product(
            id = 3,
            name = "Noise Cancelling Headphones X",
            brand = "SoundMax",
            price = 249.00,
            oldPrice = 299.00,
            rating = 4.8,
            ratingCount = 930,
            thumbnail = "headphone_x",
            colors = listOf("Matte Black", "Silver"),
            description = "Adaptive noise cancelling, 40h battery, spatial audio, USB-C fast charge.",
            category = "Headphone"
        )
    )
}