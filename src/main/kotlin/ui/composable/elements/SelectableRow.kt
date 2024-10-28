package ui.composable.elements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun <T : Enum<T>> SelectableRow(
    enumValues: Array<T>,
    selectedValue: T,
    onSelect: (T) -> Unit,
    getTitle: (T) -> String
) {
    Row(
        modifier = Modifier.wrapContentWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        enumValues.forEach { value ->
            Text(
                text = getTitle(value),
                textAlign = TextAlign.Start,
                fontWeight = if (value == selectedValue) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier
                    .padding(16.dp)
                    .clickable { onSelect(value) }
            )
        }
    }
}