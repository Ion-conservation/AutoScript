package com.yike.jarvis.feature.beverage.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yike.jarvis.feature.beverage.db.entity.BeverageCatalogEntity
import com.yike.jarvis.feature.beverage.db.entity.BeverageEntity
import com.yike.jarvis.ui.theme.DashboardColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeverageTracker(
    viewModel: BeverageViewModel = hiltViewModel()
) {
    val dailySummaries by viewModel.dailySummaries.collectAsState()
    val catalog by viewModel.beverageCatalog.collectAsState()

    var selectedDateForAdd by remember { mutableStateOf<Long?>(null) }
    var showAddBeverageDialog by remember { mutableStateOf(false) }
    var showCatalogDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column() {
                TopAppBar(
                    title = {
                        Text(
                            text = "🥤 饮料追踪",
                            modifier = Modifier.weight(2f),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    },
                    actions = {
                        IconButton(
                            modifier = Modifier.size(24.dp),
                            onClick = { showCatalogDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Manage Catalog",
                                tint = DashboardColors.TextSecondary
                            )
                        }
                        IconButton(
                            modifier = Modifier.size(24.dp),
                            onClick = {
                                selectedDateForAdd = null
                                showAddBeverageDialog = true
                            }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Day Card",
                                tint = DashboardColors.Accent
                            )
                        }
                    }
                )

                HorizontalDivider(
                    thickness = 0.5.dp, // 线条粗细，建议 0.5 到 1 dp
                    color = MaterialTheme.colorScheme.outlineVariant // 使用主题中的虚线条颜色
                )
            }

        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {


            if (dailySummaries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No beverages recorded yet.",
                        color = DashboardColors.TextSecondary
                    )
                }
            } else {
                dailySummaries.forEach { summary ->
                    DailyBeverageCard(
                        summary = summary,
                        onAddBeverage = {
                            selectedDateForAdd = summary.dateLong
                            showAddBeverageDialog = true
                        }
                    )
                }
            }
        }
    }

    if (showAddBeverageDialog) {
        AddBeverageDialog(
            initialDate = selectedDateForAdd,
            catalog = catalog,
            onDismiss = { showAddBeverageDialog = false },
            onConfirm = { name, brand, sugar, caffeine, tags, timestamp ->
                viewModel.addBeverage(name, brand, sugar, caffeine, tags, timestamp)
                showAddBeverageDialog = false
            }
        )
    }

    if (showCatalogDialog) {
        CatalogManagementDialog(
            catalog = catalog,
            onDismiss = { showCatalogDialog = false },
            onAdd = { name, brand, sugar, caffeine, tags ->
                viewModel.addCatalogItem(name, brand, sugar, caffeine, tags)
            }
        )
    }
}

@Composable
fun DailyBeverageCard(
    summary: DailyBeverageSummary,
    onAddBeverage: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, DashboardColors.PrimaryVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = summary.dateDisplay,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onAddBeverage,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Beverage",
                        tint = DashboardColors.Accent
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    "Total Sugar",
                    "${summary.totalSugar.toInt()}g",
                    if (summary.totalSugar > 50) DashboardColors.Primary else DashboardColors.Accent,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    "Total Caffeine",
                    "${summary.totalCaffeine.toInt()}mg",
                    if (summary.totalCaffeine > 200) DashboardColors.SecondaryVariant else DashboardColors.Secondary,
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = "Beverages",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DashboardColors.TextSecondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            summary.beverages.forEach { beverage ->
                BeverageItem(beverage)
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                color = Color(0xFF1A1A1A),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = if (color == DashboardColors.Primary || color == DashboardColors.SecondaryVariant) "⚠" else "✓",
                    fontSize = 16.sp,
                    color = color,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(text = label, fontSize = 12.sp, color = DashboardColors.TextSecondary)
            }
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun BeverageItem(beverage: BeverageEntity) {
    val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val time = timeFormatter.format(Date(beverage.timestamp))
    val tags = if (beverage.tags.isEmpty()) emptyList() else beverage.tags.split(",")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .background(
                color = Color(0xFF0A0A0A),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = beverage.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DashboardColors.TextPrimary
                )
                Text(text = time, fontSize = 12.sp, color = DashboardColors.TextSecondary)
            }

            if (tags.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = DashboardColors.Surface
                    ) {
                        Text(
                            text = beverage.brand,
                            fontSize = 10.sp,
                            color = DashboardColors.Secondary,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = when {
                                tag.contains("High") -> DashboardColors.PrimaryVariant
                                tag.contains("Low") || tag.contains("Zero") -> Color(0xFF1A331A)
                                else -> DashboardColors.Background
                            }
                        ) {
                            Text(
                                text = tag,
                                fontSize = 10.sp,
                                color = when {
                                    tag.contains("High") -> DashboardColors.Primary
                                    tag.contains("Low") || tag.contains("Zero") -> DashboardColors.Accent
                                    else -> DashboardColors.TextSecondary
                                },
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Text(
                text = "Sugar: ${beverage.sugar.toInt()}g  •  Caffeine: ${beverage.caffeine.toInt()}mg",
                fontSize = 11.sp,
                color = DashboardColors.TextSecondary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBeverageDialog(
    initialDate: Long?,
    catalog: List<BeverageCatalogEntity>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, Double, List<String>, Long) -> Unit
) {
    var selectedCatalogItem by remember { mutableStateOf<BeverageCatalogEntity?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var dateMillis by remember { mutableStateOf(initialDate ?: System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialDate == null) "Add Day Card" else "Add Beverage") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (initialDate == null) {
                    OutlinedTextField(
                        value = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                            Date(
                                dateMillis
                            )
                        ),
                        onValueChange = {},
                        label = { Text("Select Date") },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true },
                        enabled = false,
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedCatalogItem?.name ?: "Select Beverage",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Beverage") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        if (catalog.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No catalog items. Please add some first.") },
                                onClick = { expanded = false }
                            )
                        } else {
                            catalog.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text("${item.brand} - ${item.name} (${item.sugar.toInt()}g sugar)") },
                                    onClick = {
                                        selectedCatalogItem = item
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedCatalogItem?.let {
                        onConfirm(
                            it.name,
                            it.brand,
                            it.sugar,
                            it.caffeine,
                            if (it.defaultTags.isEmpty()) emptyList() else it.defaultTags.split(","),
                            dateMillis
                        )
                    }
                },
                enabled = selectedCatalogItem != null
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dateMillis = it }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun CatalogManagementDialog(
    catalog: List<BeverageCatalogEntity>,
    onDismiss: () -> Unit,
    onAdd: (String, String, Double, Double, String) -> Unit
) {
    var showAddForm by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var sugar by remember { mutableStateOf("") }
    var caffeine by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Beverage Catalog") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (showAddForm) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Add New Beverage Type", fontWeight = FontWeight.Bold)
                            TextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Name") })
                            TextField(
                                value = brand,
                                onValueChange = { brand = it },
                                label = { Text("Brand") })
                            TextField(
                                value = sugar,
                                onValueChange = { sugar = it },
                                label = { Text("Sugar (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            TextField(
                                value = caffeine,
                                onValueChange = { caffeine = it },
                                label = { Text("Caffeine (mg)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            TextField(
                                value = tags,
                                onValueChange = { tags = it },
                                label = { Text("Tags (comma separated)") })
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = {
                                    if (name.isNotEmpty()) {
                                        onAdd(
                                            name,
                                            brand,
                                            sugar.toDoubleOrNull() ?: 0.0,
                                            caffeine.toDoubleOrNull() ?: 0.0,
                                            tags
                                        )
                                        showAddForm = false
                                        name = ""; brand = ""; sugar = ""; caffeine = ""; tags = ""
                                    }
                                }) { Text("Save") }
                                TextButton(onClick = { showAddForm = false }) { Text("Cancel") }
                            }
                        }
                    }
                } else {
                    Button(onClick = { showAddForm = true }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Add, null)
                        Text("Add New Type")
                    }
                }

                catalog.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(item.name, fontWeight = FontWeight.Bold)
                            Text(
                                "${item.brand}  •  S: ${item.sugar}g  C: ${item.caffeine}mg",
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
