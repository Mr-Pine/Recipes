package de.mr_pine.recipes.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.mr_pine.recipes.R
import de.mr_pine.recipes.models.Recipe
import de.mr_pine.recipes.models.RecipeIngredients
import de.mr_pine.recipes.models.RecipeMetadata
import de.mr_pine.recipes.models.instructions.RecipeInstructions
import de.mr_pine.recipes.viewModels.RecipeViewModel


@ExperimentalMaterial3Api
@Composable
fun Home(viewModel: RecipeViewModel) {
    Home(
        { viewModel.showNavDrawer() },
        viewModel.recipes,
        viewModel.recipes::add,
        importRecipe = { viewModel.importRecipe() },
        navigateToRecipe = viewModel::navigateToRecipe
    )
}

@ExperimentalMaterial3Api
@Composable
fun Home(
    showNavDrawer: () -> Unit,
    recipeList: List<Recipe>,
    addRecipe: (Recipe) -> Unit,
    importRecipe: () -> Unit,
    navigateToRecipe: (Recipe) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .clip(RoundedCornerShape(100))
                            .clickable { /*TODO: Search*/ },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProvideTextStyle(
                            MaterialTheme.typography.displaySmall.copy(
                                fontSize = 20.sp
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                modifier = Modifier.padding(8.dp)
                            )
                            Text(text = stringResource(id = R.string.search))
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = showNavDrawer) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "menu"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = importRecipe) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            )
        },
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .statusBarsPadding()
    ) { paddingValues ->
        Box(
            Modifier
                .padding(paddingValues)
                .padding(horizontal = 12.dp)
        ) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { Box(modifier = Modifier.height(2.dp)) }
                items(recipeList) { recipe ->
                    ElevatedCard(
                        onClick = { navigateToRecipe(recipe) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = recipe.metadata.title,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }
                item {
                    FilledTonalButton(
                        onClick = {
                            val newRecipe = Recipe(
                                RecipeInstructions(listOf()),
                                RecipeMetadata(""),
                                RecipeIngredients(mutableStateListOf())
                            )

                            addRecipe(newRecipe)

                            navigateToRecipe(newRecipe)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                            Text(text = stringResource(R.string.Add_recipe))
                        }
                    }
                }
                item { Box(modifier = Modifier.height(2.dp)) }
            }
        }
    }
}