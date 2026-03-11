package com.example.billsplittermain.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.util.Date

/** Represents a complete bill stored in Room database. */
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

@Entity(
    tableName = "bill_items",
    foreignKeys = [
        ForeignKey(
            entity = Bill::class,
            parentColumns = ["id"],
            childColumns = ["billId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["billId"])]
)
data class BillItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val billId: Long = 0,
    val name: String,
    val price: Double,
    val quantity: Int = 1,
    val totalPrice: Double
)

/** Represents one person in a bill split. */
@Entity(
    tableName = "persons",
    foreignKeys = [
        ForeignKey(
            entity = Bill::class,
            parentColumns = ["id"],
            childColumns = ["billId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["billId"])]
)
data class Person(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val billId: Long = 0,
    val name: String,
    val amountOwed: Double = 0.0,
    val isPaid: Boolean = false,
    val colorIndex: Int = 0
)

@Entity(tableName = "saved_contacts")
data class SavedContact(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val usageCount: Int = 1
)

@Entity(
    tableName = "item_assignments",
    foreignKeys = [
        ForeignKey(
            entity = BillItem::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Person::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["itemId"]),
        Index(value = ["personId"])
    ]
)
data class ItemAssignment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemId: Long = 0,
    val personId: Long = 0,
    val splitPercentage: Double = 100.0
)

/** Joins a bill with all its associated persons using Room @Relation. */
data class BillWithPersons(
    @Embedded val bill: Bill,
    @Relation(
        parentColumn = "id",
        entityColumn = "billId"
    )
    val persons: List<Person>
)

data class BillWithItems(
    @Embedded val bill: Bill,
    @Relation(
        parentColumn = "id",
        entityColumn = "billId"
    )
    val items: List<BillItem>
)

data class SplitResult(
    val person: Person,
    val items: List<BillItem>,
    val itemsSubtotal: Double,
    val taxShare: Double,
    val tipShare: Double,
    val total: Double
)

data class ReceiptItem(
    val name: String,
    val price: Double,
    val quantity: Double = 1.0
)

data class OcrResult(
    val items: List<ReceiptItem>,
    val subtotal: Double?,
    val tax: Double?,
    val tip: Double?,
    val total: Double?
)
