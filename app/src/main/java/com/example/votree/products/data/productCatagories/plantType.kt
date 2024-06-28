package com.example.votree.products.data.productCatagories

import androidx.room.TypeConverter

enum class PlantType {
    FLOWERING_PLANTS,
    TREES,
    SHRUBS,
    FERNS,
    MOSSES,
    GRASSES,
    SUCCULENTS,
    CACTI,
    VINES,
    AQUATIC_PLANTS,
    BULBS,
    PALMS,
    CONIFERS,
    HERBS,
    PERENNIALS,
    UNKNOWN
}

// Define a TypeConverter to convert PlantType enum to String and vice versa
class PlantTypeConverter {
    @TypeConverter
    fun fromPlantType(value: PlantType): String {
        return value.name
    }

    @TypeConverter
    fun toPlantType(value: String): PlantType {
        return enumValueOf(value)
    }
}
