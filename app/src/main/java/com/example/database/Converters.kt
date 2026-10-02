package com.example.database

import androidx.room.TypeConverter
import com.example.model.FurniturePart
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val listType = Types.newParameterizedType(List::class.java, FurniturePart::class.java)
    private val adapter = moshi.adapter<List<FurniturePart>>(listType)

    @TypeConverter
    fun fromPartsList(parts: List<FurniturePart>?): String {
        if (parts == null) return "[]"
        return try {
            adapter.toJson(parts)
        } catch (e: Exception) {
            "[]"
        }
    }

    @TypeConverter
    fun toPartsList(json: String?): List<FurniturePart> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            adapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
