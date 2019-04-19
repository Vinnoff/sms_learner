package com.balekouy.industries.sms_learner

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import java.nio.file.Files.exists



@Database(entities = [Data::class, DataZero::class, DataUn::class], version = 2, exportSchema = false)

abstract class AppDatabase : RoomDatabase() {
    abstract fun dataDao(): DataDAO

    companion object {
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase? {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java,
                            "data.db"
                        ).fallbackToDestructiveMigration().build()
                    }
                }
            }
            return INSTANCE
        }
        fun databaseExist(context: Context): Boolean {
            val dbFile = context.getDatabasePath("data.db")
            return dbFile.exists()
        }
    }
}