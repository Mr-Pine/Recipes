package de.mr_pine.recipes.screens

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.mr_pine.recipes.R
import de.mr_pine.recipes.models.Recipe
import de.mr_pine.recipes.viewModels.RecipeViewModel

private const val TAG = "Home"

@ExperimentalMaterial3Api
@Composable
fun Home(viewModel: RecipeViewModel) {
    Home(
        { viewModel.showNavDrawer() },
        viewModel.recipes.values.toList(),
        importRecipe = { viewModel.importRecipe() },
        navigateToRecipe = viewModel::navigateToRecipe
    )
}

@ExperimentalMaterial3Api
@Composable
fun Home(
    showNavDrawer: () -> Unit,
    recipeList: List<Recipe>,
    importRecipe: () -> Unit,
    navigateToRecipe: (Recipe) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarScrollState())

    Log.d(TAG, "Home: ${MaterialTheme.colorScheme}")

    val containerColor by TopAppBarDefaults.smallTopAppBarColors()
        .containerColor(scrollBehavior.scrollFraction)
    Box(modifier = Modifier.background(containerColor)) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .clip(RoundedCornerShape(100))
                                .background(
                                    animateColorAsState(
                                        targetValue = if (scrollBehavior.scrollFraction > 0.01f) {
                                            TopAppBarDefaults
                                                .centerAlignedTopAppBarColors()
                                                .containerColor(0f).value
                                        } else {
                                            TopAppBarDefaults
                                                .centerAlignedTopAppBarColors()
                                                .containerColor(1f).value
                                        },
                                        animationSpec = tween(
                                            durationMillis = 500,
                                            easing = LinearOutSlowInEasing
                                        )
                                    ).value
                                )
                                .clickable { /*TODO: Search*/ },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProvideTextStyle(MaterialTheme.typography.displaySmall.let {
                                it.copy(
                                    color = it.color.copy(0.7f),
                                    fontSize = 20.sp
                                )
                            }) {
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
                    items(recipeList.filter { it.metadata != null }) { recipe ->
                        ElevatedCard(
                            onClick = { navigateToRecipe(recipe) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = recipe.metadata!!.title,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        }
                    }
                    item { Box(modifier = Modifier.height(2.dp)) }
                }
            }
        }
    }
}