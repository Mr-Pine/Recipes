package de.mr_pine.recipes.model_views.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.mr_pine.recipes.R
import de.mr_pine.recipes.models.RecipeIngredient
import de.mr_pine.recipes.models.RecipeIngredients

@Composable
fun RecipeIngredients.IngredientsCard() {
    Card(
        modifier = Modifier
            .padding(2.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(
                text = stringResource(R.string.Ingredients),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium)
            )
            for (ingredient in ingredients) {
                ingredient.IngredientRow()
            }
        }
    }
}

@Composable
fun RecipeIngredient.IngredientRow() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .alpha(if (isChecked) 0.5f else 1f)
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable { isChecked = !isChecked }
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = null,
            modifier = Modifier.size(LocalViewConfiguration.current.minimumTouchTargetSize)
        )
        Text(
            text = "$name: $amount ${unit.displayValue()}",
            fontSize = 20.sp
        )
    }
}