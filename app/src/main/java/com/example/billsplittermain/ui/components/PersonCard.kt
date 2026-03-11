package com.example.billsplittermain.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.billsplittermain.data.Person
import com.example.billsplittermain.ui.theme.PersonColors
import com.example.billsplittermain.ui.theme.Success
import com.example.billsplittermain.utils.toCurrency

/** Displays a person card with colored avatar, name, amount owed, and paid toggle. */
@Composable
fun PersonCard(
    person: Person,
    onTogglePaid: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val avatarColor = PersonColors.getOrElse(person.colorIndex % PersonColors.size) { PersonColors[0] }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (person.isPaid) Success.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = person.name.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = person.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = person.amountOwed.toCurrency(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Checkbox(
                checked = person.isPaid,
                onCheckedChange = { onTogglePaid(person.id, it) }
            )
        }
    }
}
