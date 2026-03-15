package com.example.billsplittermain.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.billsplittermain.Screen
import com.example.billsplittermain.data.Person
import com.example.billsplittermain.data.SavedContact
import com.example.billsplittermain.ui.BillViewModel
import com.example.billsplittermain.ui.theme.PersonColors
import com.example.billsplittermain.utils.formatCurrency

/**
 * Screen for assigning bill items to participants.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitScreen(
    viewModel: BillViewModel,
    navController: NavController
) {
    val persons = viewModel.persons
    val billItems = viewModel.billItems
    val savedContacts by viewModel.savedContacts.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency
    
    var selectedPersonId by remember { mutableStateOf<Long?>(null) }
    var showAddPersonSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(persons.size) {
        if (selectedPersonId == null && persons.isNotEmpty()) {
            selectedPersonId = persons.first().id
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assign Items") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.splitEqually() }) {
                        Text("Split Equally")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddPersonSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Person")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(persons) { person ->
                    PersonChip(
                        person = person,
                        isSelected = selectedPersonId == person.id,
                        onClick = { selectedPersonId = person.id },
                        onRemove = { viewModel.removePerson(person.id) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(billItems) { item ->
                    val isAssigned = selectedPersonId?.let { viewModel.isPersonAssignedToItem(item.id, it) } ?: false
                    val assignedPersons = viewModel.getAssignedPersonsForItem(item.id)
                    
                    ItemAssignmentRow(
                        name = item.name,
                        price = item.totalPrice,
                        currencyCode = selectedCurrency.code,
                        assignedPersons = assignedPersons,
                        isAssignedToSelected = isAssigned,
                        onToggle = {
                            selectedPersonId?.let { viewModel.toggleItemAssignment(item.id, it) }
                        }
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val currentPerson = persons.find { it.id == selectedPersonId }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = currentPerson?.name ?: "No one selected",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Running Total",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text(
                            text = formatCurrency(
                                selectedPersonId?.let { viewModel.getPersonRunningTotal(it) } ?: 0.0,
                                selectedCurrency.code
                            ),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            viewModel.calculateSplit()
                            navController.navigate(Screen.Result.route)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        enabled = persons.isNotEmpty()
                    ) {
                        Text("Calculate Final Split")
                    }
                }
            }
        }
    }

    if (showAddPersonSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddPersonSheet = false },
            sheetState = sheetState
        ) {
            AddPersonContent(
                savedContacts = savedContacts,
                onAddPerson = { name, id ->
                    viewModel.addPerson(name, id)
                    showAddPersonSheet = false
                }
            )
        }
    }
}

/** Chip representing a participant in the split. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonChip(
    person: Person,
    isSelected: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val color = PersonColors.getOrElse(person.colorIndex % PersonColors.size) { Color.Gray }
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(person.name) },
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        },
        trailingIcon = {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(18.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Remove", 
                     modifier = Modifier.size(14.dp))
            }
        }
    )
}

/** Row for item assignment logic. */
@Composable
private fun ItemAssignmentRow(
    name: String,
    price: Double,
    currencyCode: String,
    assignedPersons: List<Person>,
    isAssignedToSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, fontWeight = FontWeight.Bold)
                Text(text = formatCurrency(price, currencyCode), color = MaterialTheme.colorScheme.onSurfaceVariant)
                AvatarStack(persons = assignedPersons)
            }
            Checkbox(checked = isAssignedToSelected, onCheckedChange = { onToggle() })
        }
    }
}

/** Overlapping avatars showing who is assigned to an item. */
@Composable
private fun AvatarStack(persons: List<Person>) {
    Row(horizontalArrangement = Arrangement.spacedBy((-12).dp)) {
        persons.take(5).forEach { person ->
            val color = PersonColors.getOrElse(person.colorIndex % PersonColors.size) { Color.Gray }
            Box(
                modifier = Modifier.size(24.dp).clip(CircleShape).background(color),
                contentAlignment = Alignment.Center
            ) {
                Text(person.name.take(1).uppercase(), color = Color.White, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun AddPersonContent(
    savedContacts: List<SavedContact>,
    onAddPerson: (String, Long?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Frequent Contacts", style = MaterialTheme.typography.titleMedium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(savedContacts) { contact ->
                SavedContactChip(contact.name, contact.usageCount) { onAddPerson(contact.name, contact.id) }
            }
        }
        HorizontalDivider()
        Text("Or enter name", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextField(value = name, onValueChange = { name = it }, modifier = Modifier.weight(1f))
            Button(onClick = { onAddPerson(name, null) }, enabled = name.isNotBlank()) { Text("Add") }
        }
    }
}

/** Chip displaying a saved contact's name and frequency. Used for user profile quick-add feature. */
@Composable
private fun SavedContactChip(name: String, usageCount: Int, onClick: () -> Unit) {
    SuggestionChip(onClick = onClick, label = { Text("$name ($usageCount)") })
}
