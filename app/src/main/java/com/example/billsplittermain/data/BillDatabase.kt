package com.example.billsplittermain.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/** Room database for Bill Splitter OCR. Manages bills and persons tables. */
@Database(
    entities = [
        Bill::class,
        BillItem::class,
        Person::class,
        ItemAssignment::class,
        SavedContact::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BillDatabase : RoomDatabase() {

    abstract fun billDao(): BillDao

    companion object {
        @Volatile
        private var INSTANCE: BillDatabase? = null

        fun getDatabase(context: Context): BillDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): BillDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                BillDatabase::class.java,
                "bill_splitter_database"
            )
                .fallbackToDestructiveMigration()
                .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
                .build()
        }
    }
}
