package edits

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.mr_pine.recipes.common.translation.Translation

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ColumnScope.DeleteButton(text: String, onRemove: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    Button(modifier = Modifier.padding(top = 8.dp).align(Alignment.CenterHorizontally), onClick = {
        showDialog = true
    }) {
        Icon(Icons.Default.Delete, "Delete")
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
    }
    if (showDialog) AlertDialog(
        onDismissRequest = { showDialog = false },
        title = {
            Text(text = text)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onRemove()
                    showDialog = false
                }
            ) {
                Text(Translation.delete.getString())
            }
        },
        dismissButton = {
            TextButton(
                onClick = { showDialog = false }
            ) {
                Text(Translation.cancel.getString())
            }
        },
        shape = MaterialTheme.shapes.medium
    )
}