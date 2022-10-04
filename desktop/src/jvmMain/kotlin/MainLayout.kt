import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
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
    val ingredientFocusRequester = remember { FocusRequester() }

    var editEmbed: RecipeInstruction.EmbedData? by remember(recipe) { mutableStateOf(null) }
    val embedFocusRequester = remember { FocusRequester() }


    var editInstruction: RecipeInstruction? by remember(recipe) { mutableStateOf(null) }
    val instructionFocusRequester = remember { FocusRequester() }

    Row {
        Column(modifier = Modifier.weight(1f)) {
            recipe?.ingredients?.EditCard(editIngredient) {
                editIngredient = it
                try {
                    ingredientFocusRequester.requestFocus()
                } catch (_: IllegalStateException) {
                }
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            recipe?.instructions?.InstructionList(
                ingredients = recipe!!.ingredients,
                editEmbed = editEmbed,
                setEditEmbed = {
                    editEmbed = it
                    try {
                        embedFocusRequester.requestFocus()
                    } catch (_: IllegalStateException) {
                    }
                },
                editInstruction = editInstruction,
                setEditInstruction = {
                    editInstruction = it
                    try {
                        instructionFocusRequester.requestFocus()
                    } catch (_: IllegalStateException) {
                    }
                }
            )
        }
        LazyColumn(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp).weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            editIngredient?.let {
                item {
                    it.EditCard(focusRequester = ingredientFocusRequester) {
                        recipe?.ingredients?.ingredients?.remove(it)
                        if (editIngredient == it) editIngredient = null
                    }
                    LaunchedEffect(it) {
                        ingredientFocusRequester.requestFocus()
                    }
                }
            }
            editEmbed?.let {
                item(editEmbed) {
                    it.EditCard(
                        recipe?.ingredients?.ingredients ?: listOf(),
                        focusRequester = embedFocusRequester
                    ) { embed ->
                        recipe?.instructions?.instructions?.first { it.inlineEmbeds.contains(embed) }?.inlineEmbeds?.remove(
                            embed
                        )
                        if (editEmbed == embed) editEmbed = null
                    }
                }
            }
            editInstruction?.let {
                item(editInstruction) {
                    it.EditCard(
                        editEmbed = editEmbed,
                        setEditEmbed = {
                            editEmbed = it
                            try {
                                embedFocusRequester.requestFocus()
                            } catch (_: IllegalStateException) {}
                        },
                        focusRequester = instructionFocusRequester
                    ) {
                        recipe?.instructions?.instructions?.remove(it)
                        if (editInstruction == it) editInstruction = null
                    }
                    LaunchedEffect(it) {
                        instructionFocusRequester.requestFocus()
                    }
                }
            }
            item(recipe) {
                recipe?.metadata?.EditCard()
            }
        }
    }
}