package com.example.billsplittermain.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository class that abstracts access to the [BillDatabase] data sources.
 * It provides a clean API for the UI to interact with bill-related data.
 *
 * @property dao The Data Access Object for database operations.
 */
class BillRepository(private val dao: BillDao) {

    /**
     * A [Flow] of all bills in the database, ordered by date descending.
     */
    val allBills: Flow<List<Bill>> = dao.getAllBills()

    /**
     * A [Flow] of all bills combined with their items.
     */
    val allBillsWithItems: Flow<List<BillWithItems>> = dao.getAllBillsWithItems()

    /**
     * A [Flow] of all saved contacts, ordered by usage count descending.
     */
    val allSavedContacts: Flow<List<SavedContact>> = dao.getAllSavedContacts()

    /**
     * Retrieves a specific bill by its ID.
     */
    suspend fun getBillById(billId: Long): Bill? {
        return dao.getBillById(billId)
    }

    /**
     * Inserts a new bill into the database and returns its row ID.
     */
    suspend fun insertBill(bill: Bill): Long {
        return dao.insertBill(bill)
    }

    /**
     * Updates an existing bill's information.
     */
    suspend fun updateBill(bill: Bill) {
        dao.updateBill(bill)
    }

    /**
     * Deletes a specific bill from the database.
     */
    suspend fun deleteBill(bill: Bill) {
        dao.deleteBill(bill)
    }

    /**
     * Retrieves all items associated with a specific bill ID.
     */
    suspend fun getItemsForBill(billId: Long): List<BillItem> {
        return dao.getItemsForBill(billId)
    }

    /**
     * Inserts a list of items for a bill.
     */
    suspend fun insertItems(items: List<BillItem>): List<Long> {
        return dao.insertItems(items)
    }

    /**
     * Deletes a specific item.
     */
    suspend fun deleteItem(item: BillItem) {
        dao.deleteItem(item)
    }

    /**
     * Retrieves all people participating in a specific bill.
     */
    suspend fun getPersonsForBill(billId: Long): List<Person> {
        return dao.getPersonsForBill(billId)
    }

    /**
     * Inserts a new person and returns their row ID.
     */
    suspend fun insertPerson(person: Person): Long {
        return dao.insertPerson(person)
    }

    /**
     * Deletes a person from the database.
     */
    suspend fun deletePerson(person: Person) {
        dao.deletePerson(person)
    }

    /**
     * Updates a person's payment status.
     */
    suspend fun updatePersonPaidStatus(personId: Long, isPaid: Boolean) {
        dao.updatePersonPaidStatus(personId, isPaid)
    }

    /**
     * Retrieves all item assignments for a specific person.
     */
    suspend fun getAssignmentsForPerson(personId: Long): List<ItemAssignment> {
        return dao.getAssignmentsForPerson(personId)
    }

    /**
     * Inserts multiple item assignments at once.
     */
    suspend fun insertAssignments(assignments: List<ItemAssignment>) {
        dao.insertAssignments(assignments)
    }

    /**
     * Creates and saves a new contact to the database.
     */
    suspend fun saveContact(name: String) {
        val contact = SavedContact(name = name)
        dao.insertSavedContact(contact)
    }

    /**
     * Deletes a saved contact.
     */
    suspend fun deleteSavedContact(contact: SavedContact) {
        dao.deleteSavedContact(contact)
    }

    /**
     * Increments the usage count for a specific contact ID.
     */
    suspend fun incrementContactUsage(contactId: Long) {
        dao.incrementContactUsage(contactId)
    }

    /**
     * Retrieves a single bill combined with all its items.
     */
    suspend fun getBillWithItems(billId: Long): BillWithItems? {
        return dao.getBillWithItems(billId)
    }
}
