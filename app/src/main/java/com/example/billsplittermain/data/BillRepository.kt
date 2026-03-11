package com.example.billsplittermain.data

import kotlinx.coroutines.flow.Flow

/** Repository layer for Bill Splitter. Delegates all database operations to  DAOs. */
class BillRepository(private val dao: BillDao) {

    val allBills: Flow<List<Bill>> = dao.getAllBills()

    val allBillsWithItems: Flow<List<BillWithItems>> = dao.getAllBillsWithItems()

    val allSavedContacts: Flow<List<SavedContact>> = dao.getAllSavedContacts()

    suspend fun getBillById(billId: Long): Bill? {
        return dao.getBillById(billId)
    }

    suspend fun insertBill(bill: Bill): Long {
        return dao.insertBill(bill)
    }

    suspend fun updateBill(bill: Bill) {
        dao.updateBill(bill)
    }

    suspend fun deleteBill(bill: Bill) {
        dao.deleteBill(bill)
    }

    suspend fun getItemsForBill(billId: Long): List<BillItem> {
        return dao.getItemsForBill(billId)
    }

    suspend fun insertItems(items: List<BillItem>): List<Long> {
        return dao.insertItems(items)
    }

    suspend fun deleteItem(item: BillItem) {
        dao.deleteItem(item)
    }

    suspend fun getPersonsForBill(billId: Long): List<Person> {
        return dao.getPersonsForBill(billId)
    }

    suspend fun insertPerson(person: Person): Long {
        return dao.insertPerson(person)
    }

    suspend fun updatePerson(person: Person) {
        dao.updatePerson(person)
    }

    suspend fun deletePerson(person: Person) {
        dao.deletePerson(person)
    }

    suspend fun markPersonAsPaid(personId: Long, paid: Boolean) {
        dao.markAsPaid(personId, paid)
    }

    suspend fun getAssignmentsForPerson(personId: Long): List<ItemAssignment> {
        return dao.getAssignmentsForPerson(personId)
    }

    suspend fun insertAssignments(assignments: List<ItemAssignment>) {
        dao.insertAssignments(assignments)
    }

    suspend fun saveContact(name: String) {
        val contact = SavedContact(name = name)
        dao.insertSavedContact(contact)
    }

    suspend fun deleteSavedContact(contact: SavedContact) {
        dao.deleteSavedContact(contact)
    }

    suspend fun incrementContactUsage(contactId: Long) {
        dao.incrementContactUsage(contactId)
    }

    suspend fun getBillWithItems(billId: Long): BillWithItems? {
        return dao.getBillWithItems(billId)
    }

    suspend fun getBillWithPersons(billId: Long): BillWithPersons? {
        return dao.getBillWithPersons(billId)
    }
}
