package com.example.billsplittermain.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Represents a complete bill stored in Room database.
 */
@Entity(tableName = "bills")
data class Bill(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    val date: Date = Date(),
    val subtotal: Double = 0.0,
    val taxAmount: Double = 0.0,
    val taxPercentage: Double = 0.0,
    val tipAmount: Double = 0.0,
    val tipPercentage: Double = 0.0,
    val total: Double = 0.0,
    val isSynced: Boolean = false
)

/**
 * A single line item on a bill (e.g. Burger $8.99 x2)
 */
@Entity(
    tableName = "bill_items",
    foreignKeys = [
        ForeignKey(
            entity = Bill::class,
            parentColumns = ["id"],
            childColumns = ["billId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class BillItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val billId: Long = 0,
    val name: String,
    val price: Double,
    val quantity: Int = 1,
    val totalPrice: Double
)

/**
 * A person participating in the bill split. isPaid tracks whether they have settled their share.
 */
@Entity(
    tableName = "persons",
    foreignKeys = [
        ForeignKey(
            entity = Bill::class,
            parentColumns = ["id"],
            childColumns = ["billId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Person(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val billId: Long = 0,
    val name: String,
    val color: Int = 0,
    val isPaid: Boolean = false
)

/**
 * A frequently-used contact saved for quick add in future splits. Ordered by usageCount so most-used appear first.
 */
@Entity(tableName = "saved_contacts")
data class SavedContact(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val usageCount: Int = 1
)
