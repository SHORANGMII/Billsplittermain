package com.example.billsplittermain

import android.app.Application
import com.example.billsplittermain.data.BillDatabase
import com.example.billsplittermain.data.BillRepository

/** Application class for Bill Splitter OCR. Initializes Room database for dependency injection. */
class BillSplitterApplication : Application() {

    lateinit var database: BillDatabase
        private set

    lateinit var repository: BillRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = BillDatabase.getDatabase(this)
        repository = BillRepository(database.billDao())
    }
}
