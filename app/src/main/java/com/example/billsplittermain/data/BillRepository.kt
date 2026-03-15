package com.example.billsplittermain.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing bill data and saved contacts.
 * Acts as the single source of truth for the ViewModel, mediating between Room and potentially network.
 */
class BillRepository(private val dao: BillDao) {

    val allBillsWithItems: Flow<List<BillWithItems>> = dao.getAllBillsWithItems()
    val allSavedContacts: Flow<List<SavedContact>> = dao.getAllSavedContacts()

    suspend fun insertBill(bill: Bill): Long = dao.insertBill(bill)
    
    suspend fun insertItems(items: List<BillItem>): List<Long> = dao.insertItems(items)
    
    suspend fun insertPerson(person: Person): Long = dao.insertPerson(person)
    
    suspend fun insertAssignments(assignments: List<ItemAssignment>) = dao.insertAssignments(assignments)

    suspend fun getBillWithItems(billId: Long): BillWithItems? = dao.getBillWithItems(billId)
    
    suspend fun getPersonsForBill(billId: Long): List<Person> = dao.getPersonsForBill(billId)
    
    suspend fun getAssignmentsForPerson(personId: Long): List<ItemAssignment> = dao.getAssignmentsForPerson(personId)

    suspend fun deleteBill(bill: Bill) = dao.deleteBill(bill)

    suspend fun markPersonAsPaid(personId: Long, isPaid: Boolean) = dao.markAsPaid(personId, isPaid)

    suspend fun saveContact(name: String) {
        dao.insertSavedContact(SavedContact(name = name))
    }

    suspend fun incrementContactUsage(contactId: Long) = dao.incrementContactUsage(contactId)

    suspend fun deleteAllData() {
        dao.deleteAllAssignments()
        dao.deleteAllPersons()
        dao.deleteAllItems()
        dao.deleteAllBills()
        dao.deleteAllContacts()
    }
}
