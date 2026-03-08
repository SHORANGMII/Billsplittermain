package com.example.billsplittermain.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
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

/**
 * Links a BillItem to a Person. splitPercentage allows partial assignments.
 */
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
    ]
)
data class ItemAssignment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemId: Long = 0,
    val personId: Long = 0,
    val splitPercentage: Double = 100.0
)

/**
 * Room relationship class combining a Bill with all its BillItems.
 */
data class BillWithItems(
    @Embedded val bill: Bill,
    @Relation(
        parentColumn = "id",
        entityColumn = "billId"
    )
    val items: List<BillItem>
)

/**
 * The calculated split result for one person — what they owe.
 */
data class SplitResult(
    val person: Person,
    val items: List<BillItem>,
    val itemsSubtotal: Double,
    val taxShare: Double,
    val tipShare: Double,
    val total: Double
)

/**
 * An item detected from OCR scan before it is saved to Room.
 */
data class ReceiptItem(
    val name: String,
    val price: Double,
    val quantity: Double = 1.0
)

/**
 * Full result returned by the OCR processor after scanning a receipt image.
 */
data class OcrResult(
    val items: List<ReceiptItem>,
    val subtotal: Double?,
    val tax: Double?,
    val tip: Double?,
    val total: Double?
)
