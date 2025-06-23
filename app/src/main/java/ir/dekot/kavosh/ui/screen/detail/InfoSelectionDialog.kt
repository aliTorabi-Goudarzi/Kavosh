package ir.dekot.kavosh.ui.screen.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun InfoSelectionDialog(
    onDismissRequest: () -> Unit,
    itemsToSelect: List<Pair<String, String>>,
    title: String,
    confirmButtonText: String,
    onConfirm: (Map<Pair<String, String>, Boolean>) -> Unit
) {
    var selections by remember {
        mutableStateOf(itemsToSelect.associateWith { true })
    }

    Dialog(onDismissRequest = onDismissRequest) {
        // Provider داخلی حذف شد، چون چیدمان را از ریشه برنامه به ارث می‌برد
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                    items(itemsToSelect) { item ->
                        if (item.second.isEmpty()) {
                            Text(
                                text = item.first,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                            )
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = item.first,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Switch(
                                    checked = selections[item] ?: true,
                                    onCheckedChange = { isChecked ->
                                        selections = selections.toMutableMap().apply {
                                            this[item] = isChecked
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismissRequest,
                    ) {
                        Text("لغو")
                    }
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    TextButton(
                        onClick = {
                            onConfirm(selections)
                            onDismissRequest()
                        }
                    ) {
                        Text(confirmButtonText)
                    }
                }
            }
        }
    }
}