import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import de.mr_pine.recipes.common.models.Recipe
import de.mr_pine.recipes.common.models.RecipeIngredient
import edits.EditCard

@Composable
fun MainLayout(mutableRecipe: MutableState<Recipe?>) {
    var recipe by remember { mutableRecipe }
    var editIngredient: RecipeIngredient? by remember { mutableStateOf(null) }
    Row {
        Column(modifier = Modifier.weight(1f)) {
            recipe?.ingredients?.EditCard() {
                editIngredient = it
            }
        }
        Column(modifier = Modifier.weight(1f)) { }
        Column(modifier = Modifier.weight(1f)) {
            editIngredient?.EditCard()
        }
    }
}