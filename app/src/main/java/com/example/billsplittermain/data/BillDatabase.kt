package com.example.billsplittermain.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Room database for Bill Splitter. Version 2 adds SavedContact table and isPaid field on Person.
 */
@Database(
    entities = [
        Bill::class,
        BillItem::class,
        Person::class,
        ItemAssignment::class,
        SavedContact::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BillDatabase : RoomDatabase() {

    abstract fun billDao(): BillDao

    companion object {
        @Volatile
        private var INSTANCE: BillDatabase? = null

        /**
         * Returns the singleton instance of [BillDatabase].
         * Uses the singleton pattern with a synchronized block to ensure only one instance is created.
         */
        fun getDatabase(context: Context): BillDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BillDatabase::class.java,
                    "bill_splitter_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
