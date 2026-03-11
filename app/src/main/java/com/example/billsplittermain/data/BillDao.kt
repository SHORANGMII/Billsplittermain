package com.example.billsplittermain.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** Data Access Object for Bill and Person entities. Provides CRUD operations and queries. */
@Dao
interface BillDao {

    @Query("SELECT * FROM bills ORDER BY date DESC")
    fun getAllBills(): Flow<List<Bill>>

    @Query("SELECT * FROM bills WHERE id = :billId")
    suspend fun getBillById(billId: Long): Bill?

    @Insert
    suspend fun insertBill(bill: Bill): Long

    @Update
    suspend fun updateBill(bill: Bill)

    @Delete
    suspend fun deleteBill(bill: Bill)

    @Query("SELECT * FROM bill_items WHERE billId = :billId")
    suspend fun getItemsForBill(billId: Long): List<BillItem>

    @Insert
    suspend fun insertItems(items: List<BillItem>): List<Long>

    @Delete
    suspend fun deleteItem(item: BillItem)

    @Query("SELECT * FROM persons WHERE billId = :billId")
    suspend fun getPersonsForBill(billId: Long): List<Person>

    @Insert
    suspend fun insertPerson(person: Person): Long

    @Insert
    suspend fun insertPersons(persons: List<Person>): List<Long>

    @Update
    suspend fun updatePerson(person: Person)

    @Delete
    suspend fun deletePerson(person: Person)

    @Query("UPDATE persons SET isPaid = :paid WHERE id = :personId")
    suspend fun markAsPaid(personId: Long, paid: Boolean)

    @Query("SELECT * FROM item_assignments WHERE personId = :personId")
    suspend fun getAssignmentsForPerson(personId: Long): List<ItemAssignment>

    @Insert
    suspend fun insertAssignments(assignments: List<ItemAssignment>)

    @Query("DELETE FROM item_assignments WHERE itemId = :itemId")
    suspend fun deleteAssignmentsForItem(itemId: Long)

    @Query("SELECT * FROM saved_contacts ORDER BY usageCount DESC")
    fun getAllSavedContacts(): Flow<List<SavedContact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedContact(contact: SavedContact): Long

    @Delete
    suspend fun deleteSavedContact(contact: SavedContact)

    @Query("UPDATE saved_contacts SET usageCount = usageCount + 1 WHERE id = :contactId")
    suspend fun incrementContactUsage(contactId: Long)

    @Transaction
    @Query("SELECT * FROM bills")
    fun getAllBillsWithItems(): Flow<List<BillWithItems>>

    @Transaction
    @Query("SELECT * FROM bills WHERE id = :billId")
    suspend fun getBillWithItems(billId: Long): BillWithItems?

    @Transaction
    @Query("SELECT * FROM bills WHERE id = :billId")
    suspend fun getBillWithPersons(billId: Long): BillWithPersons?
}
