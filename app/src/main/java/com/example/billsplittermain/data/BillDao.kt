package com.example.billsplittermain.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Bill and Person entities.
 * Provides CRUD operations and queries for all database-backed entities.
 */
@Dao
interface BillDao {

    // ==================== BILL OPERATIONS ====================

    /** Retrieves all bills ordered by date descending. */
    @Query("SELECT * FROM bills ORDER BY date DESC")
    fun getAllBills(): Flow<List<Bill>>

    /** Retrieves a specific bill by its ID. */
    @Query("SELECT * FROM bills WHERE id = :billId")
    suspend fun getBillById(billId: Long): Bill?

    /** Inserts a new bill and returns its row ID. */
    @Insert
    suspend fun insertBill(bill: Bill): Long

    /** Updates an existing bill's details. */
    @Update
    suspend fun updateBill(bill: Bill)

    /** Deletes a bill from the database. */
    @Delete
    suspend fun deleteBill(bill: Bill)

    @Query("DELETE FROM bills") 
    suspend fun deleteAllBills()

    // ==================== ITEM OPERATIONS ====================

    /** Retrieves all items associated with a specific bill. */
    @Query("SELECT * FROM bill_items WHERE billId = :billId")
    suspend fun getItemsForBill(billId: Long): List<BillItem>

    /** Inserts multiple line items. */
    @Insert
    suspend fun insertItems(items: List<BillItem>): List<Long>

    /** Deletes a specific line item. */
    @Delete
    suspend fun deleteItem(item: BillItem)

    @Query("DELETE FROM bill_items") 
    suspend fun deleteAllItems()

    // ==================== PERSON OPERATIONS ====================

    /** Retrieves all participants assigned to a bill. */
    @Query("SELECT * FROM persons WHERE billId = :billId")
    suspend fun getPersonsForBill(billId: Long): List<Person>

    /** Inserts a new person. */
    @Insert
    suspend fun insertPerson(person: Person): Long

    /** Inserts multiple people. */
    @Insert
    suspend fun insertPersons(persons: List<Person>): List<Long>

    /** Updates a person's information. */
    @Update
    suspend fun updatePerson(person: Person)

    /** Deletes a person. */
    @Delete
    suspend fun deletePerson(person: Person)

    /** Updates the settled status of a participant. */
    @Query("UPDATE persons SET isPaid = :paid WHERE id = :personId")
    suspend fun markAsPaid(personId: Long, paid: Boolean)

    @Query("DELETE FROM persons") 
    suspend fun deleteAllPersons()

    // ==================== ASSIGNMENT OPERATIONS ====================

    /** Retrieves all item assignments for a person. */
    @Query("SELECT * FROM item_assignments WHERE personId = :personId")
    suspend fun getAssignmentsForPerson(personId: Long): List<ItemAssignment>

    /** Inserts multiple item assignments. */
    @Insert
    suspend fun insertAssignments(assignments: List<ItemAssignment>)

    /** Removes all assignments for a specific item. */
    @Query("DELETE FROM item_assignments WHERE itemId = :itemId")
    suspend fun deleteAssignmentsForItem(itemId: Long)

    @Query("DELETE FROM item_assignments") 
    suspend fun deleteAllAssignments()

    // ==================== CONTACT OPERATIONS ====================

    /** Retrieves all saved contacts ordered by usage frequency. */
    @Query("SELECT * FROM saved_contacts ORDER BY usageCount DESC")
    fun getAllSavedContacts(): Flow<List<SavedContact>>

    /** Inserts or replaces a saved contact. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedContact(contact: SavedContact): Long

    /** Deletes a saved contact. */
    @Delete
    suspend fun deleteSavedContact(contact: SavedContact)

    /** Increments the usage frequency of a frequent contact. */
    @Query("UPDATE saved_contacts SET usageCount = usageCount + 1 WHERE id = :contactId")
    suspend fun incrementContactUsage(contactId: Long)

    @Query("DELETE FROM saved_contacts") 
    suspend fun deleteAllContacts()

    // ==================== RELATIONSHIP QUERIES ====================

    /** Retrieves all bills combined with their items. */
    @Transaction
    @Query("SELECT * FROM bills")
    fun getAllBillsWithItems(): Flow<List<BillWithItems>>

    /** Retrieves a single bill combined with its items. */
    @Transaction
    @Query("SELECT * FROM bills WHERE id = :billId")
    suspend fun getBillWithItems(billId: Long): BillWithItems?

    /** Retrieves a single bill combined with its participants. */
    @Transaction
    @Query("SELECT * FROM bills WHERE id = :billId")
    suspend fun getBillWithPersons(billId: Long): BillWithPersons?
}
