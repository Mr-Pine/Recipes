package de.mr_pine.recipes.common.views.instructions

import androidx.compose.runtime.*
import de.mr_pine.recipes.common.models.instructions.InstructionSubmodels
import de.mr_pine.recipes.common.models.instructions.InstructionSubmodels.EmbedTypeModel.Companion.getEnum
import de.mr_pine.recipes.common.models.instructions.RecipeInstruction
import de.mr_pine.recipes.common.translation.Translation
import de.mr_pine.recipes.common.views.DropDown

@Composable
fun RecipeInstruction.EmbedData.TypeDropDown(onSelect: (InstructionSubmodels.EmbedTypeEnum) -> Unit) {
    var modelTypeDropdownExpanded by remember { mutableStateOf(false) }
    var selectedType: InstructionSubmodels.EmbedTypeEnum? by remember {
        mutableStateOf(
            embed.getEnum().takeIf { it.selectable }
        )
    }
    DropDown(
        expanded = modelTypeDropdownExpanded,
        labelString = Translation.type.getString(),
        onDismissRequest = { modelTypeDropdownExpanded = false },
        onExpandedChange = { modelTypeDropdownExpanded = it },
        selectedString = selectedType?.modelName?.getString() ?: "",
        selectedIcon = selectedType?.icon,
        options = InstructionSubmodels.EmbedTypeEnum.values().filter { it.selectable },
        optionText = { it.modelName.getString() },
        optionIcon = { it.icon }
    ) { embedTypeEnum ->
        selectedType = embedTypeEnum
        modelTypeDropdownExpanded = false
        onSelect(embedTypeEnum)
    }
    /*ExposedDropdownMenuBox(
        expanded = modelTypeDropdownExpanded,
        onExpandedChange = {
            modelTypeDropdownExpanded = !modelTypeDropdownExpanded
        }) {
        TextField(
            readOnly = true,
            value = selectedType?.modelName?.getString() ?: "",
            leadingIcon =
            selectedType?.let {
                {
                    Icon(
                        it.icon,
                        contentDescription = null
                    )
                }
            },
            onValueChange = {},
            label = { Text(stringResource(R.string.Type)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelTypeDropdownExpanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = modelTypeDropdownExpanded,
            onDismissRequest = { modelTypeDropdownExpanded = false }) {
            InstructionSubmodels.EmbedTypeEnum.values().filter { it.selectable }
                .forEach { embedType ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = embedType.icon,
                                    contentDescription = ""
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = embedType.modelName.getString())
                            }
                        },
                        onClick = {
                            selectedType = embedType
                            buffer.embed = typeBuffers[embedType]!!
                            modelTypeDropdownExpanded = false
                        })
                }
        }
    }*/
}