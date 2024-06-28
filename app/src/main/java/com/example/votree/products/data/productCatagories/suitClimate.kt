package com.example.votree.products.data.productCatagories

import androidx.room.TypeConverter

enum class SuitClimate {
    TROPICAL,
    TEMPERATE,
    ARID,
    SEMI_ARID,
    MEDITERRANEAN,
    SUBTROPICAL,
    POLAR,
    ALPINE,
    DESERT,
    MARITIME,
    CONTINENTAL,
    HUMID_SUBTROPICAL,
    HUMID_CONTINENTAL,
    MONSOONAL,
    OCEANIC,
    STEPPE,
    UNKNOWN
}

class SuitClimateConverter {
    @TypeConverter
    fun fromSuitClimate(value: SuitClimate): String {
        return value.name
    }

    @TypeConverter
    fun toSuitClimate(value: String): SuitClimate {
        return enumValueOf(value)
    }
}
