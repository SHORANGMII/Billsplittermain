package com.example.billsplittermain

import android.app.Application
import com.example.billsplittermain.data.BillDatabase
import com.example.billsplittermain.data.BillRepository

/**
 * Application class for the Bill Splitter app.
 * It provides a central place to initialize and store the [BillDatabase] and [BillRepository].
 */
class BillSplitterApplication : Application() {

    /**
     * The application's Room database.
     */
    lateinit var database: BillDatabase
        private set

    /**
     * The repository for database operations.
     */
    lateinit var repository: BillRepository
        private set

    /**
     * Initializes the database and repository on application creation.
     */
    override fun onCreate() {
        super.onCreate()
        database = BillDatabase.getDatabase(this)
        repository = BillRepository(database.billDao())
    }
}
