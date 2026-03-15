package com.example.billsplittermain.ui.screens

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.billsplittermain.Screen
import com.example.billsplittermain.data.SplitResult
import com.example.billsplittermain.ui.BillViewModel
import com.example.billsplittermain.ui.theme.PersonColors
import com.example.billsplittermain.ui.theme.Success
import com.example.billsplittermain.utils.formatCurrency
import kotlinx.coroutines.launch

/**
 * Screen that displays the final split results for each person.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    viewModel: BillViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val splitResults by viewModel.splitResults
    val selectedCurrency by viewModel.selectedCurrency
    val grandTotal = viewModel.getConvertedGrandTotal()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Split Results") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ==================== SUMMARY CARD ====================
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("GRAND TOTAL", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = formatCurrency(grandTotal, selectedCurrency.code),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==================== RESULTS LIST ====================
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(splitResults) { result ->
                    SplitResultCard(
                        result = result,
                        currencyCode = selectedCurrency.code,
                        onTogglePaid = { personId, isPaid ->
                            viewModel.togglePersonPaid(personId, isPaid)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==================== ACTION BUTTONS ====================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                /** Share summary as plain text. */
                Button(
                    onClick = {
                        val shareText = buildString {
                            appendLine("Receipt Split Summary:")
                            splitResults.forEach {
                                appendLine("${it.person.name}: ${formatCurrency(it.total, selectedCurrency.code)}")
                            }
                            appendLine("\nTotal: ${formatCurrency(grandTotal, selectedCurrency.code)}")
                        }
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Split"))
                    },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Share")
                }

                /** Export summary to PDF via system print manager. */
                Button(
                    onClick = { exportToPdf(context, splitResults, grandTotal, selectedCurrency.code) },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("PDF")
                }

                /** Save bill to Room database and exit. */
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.saveBill()
                            Toast.makeText(context, "Bill saved!", Toast.LENGTH_SHORT).show()
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Save")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ==================== NAVIGATION ====================
            TextButton(
                onClick = {
                    viewModel.createNewBill()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            ) {
                Text("Start New Split")
            }
        }
    }
}

/**
 * Strategy: Builds an HTML string from results, loads it into an off-screen WebView,
 * and then passes the document to PrintManager to generate a PDF.
 */
private fun exportToPdf(context: Context, results: List<SplitResult>, total: Double, currency: String) {
    val webView = WebView(context)
    val html = buildString {
        append("<html><body>")
        append("<h1>Receipt Split</h1>")
        append("<table border='1' style='width:100%; border-collapse: collapse;'>")
        append("<tr><th>Person</th><th>Breakdown</th><th>Total</th></tr>")
        results.forEach {
            append("<tr>")
            append("<td>${it.person.name}</td>")
            append("<td>Items: ${it.itemsSubtotal}, Tax: ${it.taxShare}, Tip: ${it.tipShare}</td>")
            append("<td>${formatCurrency(it.total, currency)}</td>")
            append("</tr>")
        }
        append("</table>")
        append("<h3>Grand Total: ${formatCurrency(total, currency)}</h3>")
        append("</body></html>")
    }

    webView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
            val jobName = "BillSplit_${System.currentTimeMillis()}"
            val printAdapter = webView.createPrintDocumentAdapter(jobName)
            printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
        }
    }
    webView.loadDataWithBaseURL(null, html, "text/HTML", "UTF-8", null)
}

/** Card for a single person's split result. */
@Composable
private fun SplitResultCard(
    result: SplitResult,
    currencyCode: String,
    onTogglePaid: (Long, Boolean) -> Unit
) {
    val person = result.person
    val avatarColor = PersonColors.getOrElse(person.colorIndex % PersonColors.size) { Color.Gray }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (person.isPaid) Success.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(avatarColor), contentAlignment = Alignment.Center) {
                    Text(person.name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(person.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                Text(formatCurrency(result.total, currencyCode), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Items: ${formatCurrency(result.itemsSubtotal, currencyCode)}   Tax: ${formatCurrency(result.taxShare, currencyCode)}   Tip: ${formatCurrency(result.tipShare, currencyCode)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (person.isPaid) {
                Button(
                    onClick = { onTogglePaid(person.id, false) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Success)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Paid")
                }
            } else {
                OutlinedButton(
                    onClick = { onTogglePaid(person.id, true) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Mark as Paid")
                }
            }
        }
    }
}
