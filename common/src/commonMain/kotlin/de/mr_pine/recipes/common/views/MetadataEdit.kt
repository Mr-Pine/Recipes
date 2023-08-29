package de.mr_pine.recipes.common.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.mr_pine.recipes.common.model.RecipeMetadata
import de.mr_pine.recipes.common.translation.Translation

@ExperimentalMaterial3Api
@Composable
fun RecipeMetadata.EditColumn(focusRequester: FocusRequester = remember { FocusRequester() }) {
    Column {
        TextField(
            value = title,
            onValueChange = { title = it },
            label = {
                Text(text = Translation.title.getString())
            },
            isError = title.isEmpty(),
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(10.dp))
        var portionsText by remember {
            mutableStateOf(
                portionSize?.toString() ?: ""
            )
        }
        TextField(
            value = portionsText,
            //modifier = Modifier.width(90.dp),
            onValueChange = { newValue ->
                if (newValue == "") {
                    portionSize = null
                    portionsText = newValue
                }
                try {
                    portionSize = newValue.toFloat()
                    portionsText = newValue
                } catch (e: NumberFormatException) {
                    portionSize = Float.NaN
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            label = { Text(text = Translation.portions.getString()) },
            isError = portionSize?.isNaN() ?: false,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(10.dp))
        TextField(
            value = author ?: "",
            onValueChange = {
                author = it.takeIf { it.isNotBlank() }
            },
            label = {
                Text(text = Translation.author.getString())
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}