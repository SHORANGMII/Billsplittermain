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
 * Data Access Object for the Bill Splitter application.
 * Handles database operations for all entities and relationships.
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

    // --- Person Operations ---

    /**
     * Retrieves all people associated with a specific bill.
     */
    @Query("SELECT * FROM persons WHERE billId = :billId")
    suspend fun getPersonsForBill(billId: Long): List<Person>

    /**
     * Inserts a new person into the database.
     * @return The row ID of the newly inserted person.
     */
    @Insert
    suspend fun insertPerson(person: Person): Long

    /**
     * Inserts multiple people into the database.
     * @return List of row IDs for the newly inserted people.
     */
    @Insert
    suspend fun insertPersons(persons: List<Person>): List<Long>

    /**
     * Updates a person's information.
     */
    @Update
    suspend fun updatePerson(person: Person)

    /**
     * Deletes a person from the database.
     */
    @Delete
    suspend fun deletePerson(person: Person)

    /**
     * Updates whether a person has paid their share.
     */
    @Query("UPDATE persons SET isPaid = :isPaid WHERE id = :personId")
    suspend fun updatePersonPaidStatus(personId: Long, isPaid: Boolean)

    // --- ItemAssignment Operations ---

    /**
     * Retrieves all item assignments for a specific person.
     */
    @Query("SELECT * FROM item_assignments WHERE personId = :personId")
    suspend fun getAssignmentsForPerson(personId: Long): List<ItemAssignment>

    /**
     * Inserts multiple item assignments.
     */
    @Insert
    suspend fun insertAssignments(assignments: List<ItemAssignment>)

    /**
     * Deletes all assignments associated with a specific item.
     */
    @Query("DELETE FROM item_assignments WHERE itemId = :itemId")
    suspend fun deleteAssignmentsForItem(itemId: Long)

    // --- SavedContact Operations ---

    /**
     * Retrieves all saved contacts, ordered by usage count descending.
     */
    @Query("SELECT * FROM saved_contacts ORDER BY usageCount DESC")
    fun getAllSavedContacts(): Flow<List<SavedContact>>

    /**
     * Inserts or replaces a saved contact.
     * @return The row ID of the inserted contact.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedContact(contact: SavedContact): Long

    /**
     * Deletes a specific saved contact.
     */
    @Delete
    suspend fun deleteSavedContact(contact: SavedContact)

    /**
     * Increments the usage count of a contact.
     */
    @Query("UPDATE saved_contacts SET usageCount = usageCount + 1 WHERE id = :contactId")
    suspend fun incrementContactUsage(contactId: Long)

    // --- Relationship Queries ---

    /**
     * Retrieves all bills combined with their items using a transaction.
     */
    @Transaction
    @Query("SELECT * FROM bills")
    fun getAllBillsWithItems(): Flow<List<BillWithItems>>

    /**
     * Retrieves a single bill combined with its items using a transaction.
     */
    @Transaction
    @Query("SELECT * FROM bills WHERE id = :billId")
    suspend fun getBillWithItems(billId: Long): BillWithItems?
}
