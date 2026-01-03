package auto.script.feature.beverage

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import auto.script.ui.theme.DashboardColors

@Composable
fun BeverageTracker() {
    Box(
        modifier = Modifier.Companion
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.Companion
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = BorderStroke(1.dp, DashboardColors.BorderColor)
            ) {
                Column(
                    modifier = Modifier.Companion.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.Companion
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Companion.CenterVertically
                    ) {
                        Text(
                            text = "🥤 Beverage Tracker",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Settings",
                                tint = DashboardColors.InfoBlue,
                                modifier = Modifier.Companion.size(20.dp)
                            )
                            Text(
                                text = "+",
                                fontSize = 20.sp,
                                color = DashboardColors.InfoBlue
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.Companion
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard("Total Sugar", "81g", DashboardColors.WarningOrange)
                        StatCard("Total Caffeine", "283mg", Color.Companion.Yellow)
                    }

                    Text(
                        text = "Today's Beverages",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Companion.Bold,
                        color = DashboardColors.TextSecondary,
                        modifier = Modifier.Companion.padding(bottom = 12.dp)
                    )

                    BeverageItem(
                        "Caramel Frappuccino",
                        "09:30 AM",
                        listOf("High Sugar", "High Caffeine"),
                        "Sugar: 54g  •  Caffeine: 95mg"
                    )
                    BeverageItem(
                        "Green Tea",
                        "02:15 PM",
                        listOf("Zero Calories", "Low Caffeine"),
                        "Sugar: 0g  •  Caffeine: 28mg"
                    )
                    BeverageItem(
                        "Energy Drink",
                        "04:45 PM",
                        listOf("High Sugar", "Very High Caffeine"),
                        "Sugar: 27g  •  Caffeine: 160mg"
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, color: Color) {
    Box(
        modifier = Modifier.Companion
            .background(
                color = Color(0xFF1A1A1A),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.Companion.CenterVertically,
                modifier = Modifier.Companion.padding(bottom = 8.dp)
            ) {
                Text(
                    text = "⚠",
                    fontSize = 16.sp,
                    color = color,
                    modifier = Modifier.Companion.padding(end = 4.dp)
                )
                Text(text = label, fontSize = 12.sp, color = DashboardColors.TextSecondary)
            }
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Companion.Bold,
                color = color
            )
        }
    }
}

@Composable
fun BeverageItem(name: String, time: String, tags: List<String>, details: String) {
    Box(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .background(
                color = Color(0xFF0A0A0A),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
            .padding(bottom = 8.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Companion.CenterVertically
            ) {
                Text(
                    text = name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Companion.Bold,
                    color = DashboardColors.TextPrimary
                )
                Text(text = time, fontSize = 12.sp, color = DashboardColors.TextSecondary)
            }
            Row(
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                tags.forEach { tag ->
                    Surface(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                        color = when {
                            tag.contains("High") -> Color(0xFF6B3E00)
                            tag.contains("Low") -> Color(0xFF1A5C1A)
                            else -> Color(0xFF2A2A2A)
                        }
                    ) {
                        Text(
                            text = tag,
                            fontSize = 10.sp,
                            color = when {
                                tag.contains("High") -> DashboardColors.WarningOrange
                                tag.contains("Low") -> DashboardColors.AccentGreen
                                else -> DashboardColors.TextSecondary
                            },
                            modifier = Modifier.Companion.padding(4.dp, 2.dp)
                        )
                    }
                }
            }
            Text(text = details, fontSize = 11.sp, color = DashboardColors.TextSecondary)
        }
    }
}