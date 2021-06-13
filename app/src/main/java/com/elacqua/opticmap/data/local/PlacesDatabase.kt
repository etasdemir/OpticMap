package com.elacqua.opticmap.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Place::class],
    version = 1,
    exportSchema = false
)
abstract class PlacesDatabase: RoomDatabase() {
    abstract fun getPlacesDao(): PlacesDao

    companion object {
        private const val DATABASE_NAME = "PlacesDatabase"
        @Volatile
        private lateinit var instance: PlacesDatabase

        fun getInstance(context: Context): PlacesDatabase {
            synchronized(this) {
                if (!::instance.isInitialized) {
                    instance = Room.databaseBuilder(context, PlacesDatabase::class.java, DATABASE_NAME)
                        .fallbackToDestructiveMigration()
                        .build()
                }
                return instance
            }
        }
    }
}