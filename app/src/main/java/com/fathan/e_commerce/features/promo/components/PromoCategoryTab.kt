package com.fathan.e_commerce.features.promo.components

import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fathan.e_commerce.features.promo.TokoGreen

@Composable
fun PromoCategoryTabs(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    ScrollableTabRow(
        selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
        edgePadding = 0.dp,
        containerColor = Color.White,
        contentColor = TokoGreen,
        indicator = { tabPositions ->
            val index = categories.indexOf(selectedCategory).coerceAtLeast(0)
            if (tabPositions.isNotEmpty() && index < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[index]),
                    color = TokoGreen
                )
            }
        },
        modifier = modifier
    ) {
        categories.forEach { category ->
            Tab(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                text = {
                    Text(
                        category,
                        fontWeight = if (selectedCategory == category)
                            FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }
    }
}
