package de.mr_pine.recipes.model_views.view

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import de.mr_pine.recipes.R
import de.mr_pine.recipes.models.RecipeMetadata
import de.mr_pine.recipes.models.amount

@Composable
fun RecipeMetadata.MetaInfo() {
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