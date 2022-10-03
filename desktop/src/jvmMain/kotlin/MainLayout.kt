import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.mr_pine.recipes.common.models.Recipe
import de.mr_pine.recipes.common.models.RecipeIngredient
import de.mr_pine.recipes.common.models.instructions.RecipeInstruction
import edits.EditCard
import edits.InstructionList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(mutableRecipe: MutableState<Recipe?>) {
    val recipe by remember(mutableRecipe) { mutableRecipe }
    var editIngredient: RecipeIngredient? by remember(recipe) { mutableStateOf(null) }
    var editEmbed: RecipeInstruction.EmbedData? by remember(recipe) { mutableStateOf(null) }
    var editInstruction: RecipeInstruction? by remember(recipe) { mutableStateOf(null) }
    Row {
        Column(modifier = Modifier.weight(1f)) {
            recipe?.ingredients?.EditCard(editIngredient) {
                editIngredient = it
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            recipe?.instructions?.InstructionList(
                ingredients = recipe!!.ingredients,
                editEmbed = editEmbed,
                setEditEmbed = {
                    editEmbed = it
                },
                editInstruction = editInstruction,
                setEditInstruction = {
                    editInstruction = it
                }
            )
        }
        LazyColumn(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp).weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            editIngredient?.let{
                item {
                    it.EditCard()
                }
            }
            editEmbed?.let{
                item {
                    it.EditCard(recipe?.ingredients?.ingredients ?: listOf())
                }
            }
            editInstruction?.let{
                item {
                    it.EditCard(editEmbed = editEmbed, setEditEmbed = { editEmbed = it })
                }
            }
            item {
                recipe?.metadata?.EditCard()
            }
        }
    }
}