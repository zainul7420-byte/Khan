package com.example.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [ProjectEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class WoodCraftDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao

    companion object {
        @Volatile
        private var INSTANCE: WoodCraftDatabase? = null

        fun getInstance(context: Context): WoodCraftDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WoodCraftDatabase::class.java,
                    "woodcraft_database.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
