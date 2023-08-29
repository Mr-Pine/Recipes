package de.mr_pine.recipes.android.model_views.view

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import de.mr_pine.recipes.android.R
import de.mr_pine.recipes.common.model.RecipeMetadata
import de.mr_pine.recipes.common.model.amount

@Composable
fun RecipeMetadata.MetaInfo() {
    Column{
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