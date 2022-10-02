import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import de.mr_pine.recipes.common.models.Recipe
import de.mr_pine.recipes.common.models.RecipeIngredient
import de.mr_pine.recipes.common.models.instructions.RecipeInstruction
import edits.EditCard
import edits.InstructionList

@Composable
fun MainLayout(mutableRecipe: MutableState<Recipe?>) {
    var recipe by remember(mutableRecipe) { mutableRecipe }
    var editIngredient: RecipeIngredient? by remember(recipe) { mutableStateOf(null) }
    var editEmbed: RecipeInstruction.EmbedData? by remember(recipe) { mutableStateOf(null) }
    var editInstruction: RecipeInstruction? by remember(recipe) { mutableStateOf(null) }
    Row {
        Column(modifier = Modifier.weight(1f)) {
            recipe?.ingredients?.EditCard() {
                editIngredient = it
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            recipe?.instructions?.InstructionList(
                ingredients = recipe!!.ingredients,
                setEditEmbed = {
                    editEmbed = it
                },
                setEditInstruction = {
                    editInstruction = it
                }
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            editIngredient?.EditCard()
            Text(editEmbed?.embed?.content ?: "")
            editInstruction?.EditCard()
        }
    }
}