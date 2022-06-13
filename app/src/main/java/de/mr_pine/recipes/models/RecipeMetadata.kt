package de.mr_pine.recipes.models

import android.util.Log
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import de.mr_pine.recipes.R

private const val TAG = "RecipeMetadata"

class RecipeMetadata(
    override val serialized: String
) : RecipeDeserializable {
    var title: String = ""
    var author: String? = null
    var portionSize: Float? = null

    companion object {
        const val DataTag = "Metadata"
    }

    init {
        deserialize()
    }

    override fun deserialize(forceDeserialization: Boolean): RecipeMetadata {
        title = serialized.extractString("Title")
        try {
            author = serialized.extractString("Author")
        } catch (e: Exception) {
            Log.i(TAG, "deserialize: No author found")
        }
        try {
            portionSize = serialized.extractString("Portions").toFloat()
        } catch (e: Exception) {
            Log.i(TAG, "deserialize: No portionsize found or not a number")
        }
        return this
    }

    @ExperimentalMaterial3Api
    @Composable
    fun MetaInfo() {
        Row{
            if (author != null) Text(
                text = "${stringResource(R.string.by)} $author",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.W400
                )
            )
            if (portionSize != null) Text(
                text = "${stringResource(R.string.portions)}: ${portionSize!!.amount}",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.W400
                )
            )
        }
    }
}