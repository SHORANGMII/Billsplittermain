package com.example.billsplittermain.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the Bill Splitter application.
 * Handles database operations for [Bill] and [BillItem] entities.
 */
@Dao
interface BillDao {

    // --- Bill Operations ---

    /**
     * Retrieves all bills from the database, ordered by date in descending order.
     */
    @Query("SELECT * FROM bills ORDER BY date DESC")
    fun getAllBills(): Flow<List<Bill>>

    /**
     * Retrieves a specific bill by its ID.
     */
    @Query("SELECT * FROM bills WHERE id = :billId")
    suspend fun getBillById(billId: Long): Bill?

    /**
     * Inserts a new bill into the database.
     * @return The row ID of the newly inserted bill.
     */
    @Insert
    suspend fun insertBill(bill: Bill): Long

    /**
     * Updates an existing bill in the database.
     */
    @Update
    suspend fun updateBill(bill: Bill)

    /**
     * Deletes a bill from the database.
     */
    @Delete
    suspend fun deleteBill(bill: Bill)

    // --- BillItem Operations ---

    /**
     * Retrieves all items associated with a specific bill.
     */
    @Query("SELECT * FROM bill_items WHERE billId = :billId")
    suspend fun getItemsForBill(billId: Long): List<BillItem>

    /**
     * Inserts multiple bill items into the database.
     * @return List of row IDs for the newly inserted items.
     */
    @Insert
    suspend fun insertItems(items: List<BillItem>): List<Long>

    /**
     * Deletes a specific item from a bill.
     */
    @Delete
    suspend fun deleteItem(item: BillItem)
}
