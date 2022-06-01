package de.mr_pine.recipes.models

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import de.mr_pine.recipes.R

data class RecipeMetadata(
    val text: String,
    val title: String,
    val author: String
) {
    companion object {
        const val DataTag = "Metadata"

        fun deserialize(serialized: String): RecipeMetadata {
            return RecipeMetadata(
                text = serialized,
                title = serialized.extractString("Title"),
                author = serialized.extractString("Author")
            )
        }
    }

    @ExperimentalMaterial3Api
    @Composable
    fun MetaInfo() {
        Text(text = "${stringResource(R.string.by)} $author", style = MaterialTheme.typography.labelMedium.copy(fontStyle = FontStyle.Italic, fontWeight = FontWeight.W400))
    }
}