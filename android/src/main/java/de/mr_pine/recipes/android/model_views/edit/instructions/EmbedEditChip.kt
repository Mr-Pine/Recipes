package de.mr_pine.recipes.android.model_views.edit.instructions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import de.mr_pine.recipes.android.R
import de.mr_pine.recipes.common.models.RecipeIngredient
import de.mr_pine.recipes.common.models.instructions.InstructionSubmodels
import de.mr_pine.recipes.common.models.instructions.InstructionSubmodels.EmbedTypeModel.Companion.getEnum
import de.mr_pine.recipes.common.models.instructions.RecipeInstruction
import de.mr_pine.recipes.common.views.instructions.IngredientEditColumn
import de.mr_pine.recipes.common.views.instructions.RecipeEmbedChip
import de.mr_pine.recipes.common.views.instructions.TimerEditColumn
import de.mr_pine.recipes.common.views.instructions.TypeDropDown
import kotlin.time.Duration.Companion.seconds

@ExperimentalMaterial3Api
@Composable
fun RecipeInstruction.EmbedData.RecipeEditChipStateful(
    getIngredientFraction: ((String, Float) -> RecipeIngredient)?,
    done: Boolean,
    editIndex: Int? = null,
    removeEmbed: (RecipeInstruction.EmbedData) -> Unit,
    ingredients: List<RecipeIngredient>
) {

    if (embed is InstructionSubmodels.IngredientModel && (embed as InstructionSubmodels.IngredientModel).ingredient == null) {
        (embed as InstructionSubmodels.IngredientModel).receiveIngredient(getIngredientFraction)
    }

    var hideNew by remember { mutableStateOf(embed is InstructionSubmodels.UndefinedEmbedTypeModel) }
    var isEditing by remember { mutableStateOf(hideNew) }

    if (!hideNew) {
        RecipeEmbedChip(
            onClick = { isEditing = true },
            selected = true,
            enabled = !done,
            icon = embed.getEnum().icon,
            labelText = embed.content,
            editIndex = editIndex
        )
    }

    if (isEditing) {
        val buffer by remember { mutableStateOf(this.copy()) }
        val typeBuffers = remember {
            InstructionSubmodels.EmbedTypeEnum.values().map {
                it to when (it) {
                    InstructionSubmodels.EmbedTypeEnum.TIMER -> InstructionSubmodels.TimerModel(
                        mutableStateOf(0.seconds)
                    )

                    InstructionSubmodels.EmbedTypeEnum.UNDEFINED -> InstructionSubmodels.UndefinedEmbedTypeModel()
                    InstructionSubmodels.EmbedTypeEnum.INGREDIENT -> InstructionSubmodels.IngredientModel.NO_INGREDIENT
                }
            }.toMutableStateMap()
        }

        typeBuffers[buffer.embed.getEnum()] = buffer.embed
        @Composable
        fun EditEmbedDialog(
            applyBufferConfirm: InstructionSubmodels.EmbedTypeModel.() -> Unit = {},
            content: @Composable ColumnScope.() -> Unit
        ) {
            val dismiss = { isEditing = false; if (hideNew) removeEmbed(this) }
            AlertDialog(
                onDismissRequest = dismiss,
                confirmButton = {
                    TextButton(onClick = {
                        if (
                            !(buffer.embed is InstructionSubmodels.TimerModel && (buffer.embed as InstructionSubmodels.TimerModel).duration == 0.seconds) &&
                            buffer.embed !is InstructionSubmodels.UndefinedEmbedTypeModel
                        ) {
                            embed = buffer.embed.copy().apply(applyBufferConfirm)
                            enabled = buffer.enabled
                            isEditing = false
                            hideNew = false
                        }
                    }) {
                        Text(text = stringResource(id = if (hideNew) R.string.Add else R.string.Apply))
                    }
                },
                dismissButton = {
                    TextButton(onClick = dismiss) {
                        Text(text = stringResource(id = R.string.Cancel))
                    }
                },
                title = {
                    Text(text = stringResource(id = if (hideNew) R.string.Add_Embed else R.string.Edit_Embed))
                },
                text = { Column(content = content) }
            )
        }

        //Different Dialogs necessary because of https://issuetracker.google.com/issues/221643630
        when (remember(buffer.embed) { buffer.embed.getEnum() }) {
            InstructionSubmodels.EmbedTypeEnum.TIMER -> {
                val timerEmbed = buffer.embed as InstructionSubmodels.TimerModel
                EditEmbedDialog {
                    buffer.TimerEditColumn(timerEmbed) {
                        buffer.embed = typeBuffers[it]!!
                    }
                }
            }

            InstructionSubmodels.EmbedTypeEnum.INGREDIENT -> {
                val ingredientEmbed = buffer.embed as InstructionSubmodels.IngredientModel

                EditEmbedDialog {
                    buffer.IngredientEditColumn(ingredientEmbed, ingredients) {
                        buffer.embed = typeBuffers[it]!!
                    }
                }
            }

            InstructionSubmodels.EmbedTypeEnum.UNDEFINED -> {
                EditEmbedDialog {
                    buffer.TypeDropDown {}
                }
            }
        }
    }
}