package de.mr_pine.recipes.models

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import de.mr_pine.recipes.R

class RecipeMetadata(
    override val serialized: String
) : RecipeDeserializable {
    var title: String = ""
    var author: String = ""

    companion object {
        const val DataTag = "Metadata"
    }

    init {
        deserialize()
    }

    override fun deserialize(): RecipeMetadata {
        title = serialized.extractString("Title")
        author = serialized.extractString("Author")
        return this
    }

    @ExperimentalMaterial3Api
    @Composable
    fun MetaInfo() {
        Text(
            text = "${stringResource(R.string.by)} $author",
            style = MaterialTheme.typography.labelMedium.copy(
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.W400
            )
        )
    }
}