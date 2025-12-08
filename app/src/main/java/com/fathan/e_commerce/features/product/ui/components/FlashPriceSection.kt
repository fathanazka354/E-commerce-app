package com.fathan.e_commerce.features.product.ui.components
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fathan.e_commerce.features.product.ui.ProductDetailUiState
import java.util.concurrent.TimeUnit

@Composable
fun FlashPriceSection(ui: ProductDetailUiState) {
    val product = ui.product
    val flash = ui.flashSale

    // If there's no flash sale -> show normal price only
    if (flash == null) {
        Text(
            text = formatCurrency(product.price.toLong()),
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 22.sp),
            color = MaterialTheme.colorScheme.primary
        )
        return
    }

    // Safely get prices
    val flashPrice = flash.flashPrice
    val originalPrice = if (flash.originalPrice > 0.0) flash.originalPrice else product.price

    // Discount percent safe
    val discountPercent = if (originalPrice > 0.0) {
        (((originalPrice - flashPrice) / originalPrice) * 100).toInt()
    } else 0

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Flash price
            Text(
                text = formatCurrency(flashPrice.toLong()),
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 22.sp),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Original price (strikethrough)
            Text(
                text = formatCurrency(originalPrice.toLong()),
                style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.LineThrough),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Discount badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                tonalElevation = 2.dp,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(
                    text = "-$discountPercent%",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Stock / sold info row
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            // If stock info available show remaining stock
            // flash.stock is Long? (from your mapping). Show if > 0
            if (flash.stock > 0L) {
                Text(
                    text = "Sisa: ${flash.stock}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            // sold info (if any)
            val soldCount = flash.sold
            if (soldCount > 0) {
                Text(
                    text = "Terjual: $soldCount",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Optional small badge
            Surface(
                shape = RoundedCornerShape(6.dp),
                tonalElevation = 2.dp,
                modifier = Modifier.alpha(0.95f)
            ) {
                Text(
                    text = "FLASH SALE",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // OPTIONAL: countdown (requires end time in model)
        // If you add 'endAtMillis: Long?' to FlashSaleItem, you can enable countdown like below:
        //
        // val endAtMillis = flash.endAtMillis ?: 0L
        // if (endAtMillis > 0L) {
        //     val remainingSeconds by rememberUpdatedState((endAtMillis - System.currentTimeMillis()) / 1000)
        //     CountdownText(endAtMillis)
        // }
    }
}

/** Small countdown util if you later provide end timestamp in milliseconds to FlashSaleItem */
@Composable
fun CountdownText(endAtMillis: Long) {
    var remainingSeconds by remember { mutableStateOf((endAtMillis - System.currentTimeMillis()) / 1000) }

    // Update every second
    LaunchedEffect(endAtMillis) {
        while (remainingSeconds > 0) {
            kotlinx.coroutines.delay(1000)
            remainingSeconds = (endAtMillis - System.currentTimeMillis()) / 1000
        }
    }

    val days = TimeUnit.SECONDS.toDays(remainingSeconds)
    val hours = TimeUnit.SECONDS.toHours(remainingSeconds) % 24
    val minutes = TimeUnit.SECONDS.toMinutes(remainingSeconds) % 60
    val seconds = remainingSeconds % 60

    Text(
        text = String.format("Berakhir dalam %d hari %02d:%02d:%02d", days, hours, minutes, seconds),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error
    )
}
