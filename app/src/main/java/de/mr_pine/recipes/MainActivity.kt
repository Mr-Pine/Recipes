package de.mr_pine.recipes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import de.mr_pine.recipes.models.Recipe
import de.mr_pine.recipes.screens.RecipeView
import de.mr_pine.recipes.screens.ShowError
import de.mr_pine.recipes.ui.theme.HarmonizedTheme
import de.mr_pine.recipes.ui.theme.RecipesTheme
import de.mr_pine.recipes.viewModels.RecipeViewModel

@ExperimentalFoundationApi
@ExperimentalMaterial3Api
@ExperimentalAnimationApi
@ExperimentalMaterialApi
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            val systemUiController = rememberSystemUiController()

            val recipeViewModel: RecipeViewModel = viewModel()

            try {
            recipeViewModel.currentRecipe = Recipe.deserialize(
                    resources.openRawResource(R.raw.rezept).bufferedReader().readText()
                )
            } catch (e: Exception) {
                ShowError(errorMessage = e.message ?: "")
            }

            val scrollBehavior =
                TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarScrollState())

            HarmonizedTheme {

                val titleColor = TopAppBarDefaults.smallTopAppBarColors().containerColor(
                    scrollFraction = scrollBehavior.scrollFraction
                ).value
                val useDarkIcons = !isSystemInDarkTheme()
                remember(titleColor) {
                    systemUiController.setStatusBarColor(titleColor)
                    systemUiController.navigationBarDarkContentEnabled = useDarkIcons
                    true
                }


                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Scaffold(
                        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                        topBar = {
                            SmallTopAppBar(
                                title = {
                                    Text(
                                        recipeViewModel.currentRecipe?.metadata?.title
                                            ?: "No recipe specified"
                                    )
                                },
                                navigationIcon = {
                                    IconButton(
                                        onClick = { /* "Open nav drawer" */ }
                                    ) {
                                        Icon(
                                            Icons.Filled.Menu,
                                            contentDescription = "Localized description"
                                        )
                                    }
                                },
                                scrollBehavior = scrollBehavior
                            )
                        },
                        floatingActionButtonPosition = FabPosition.End,
                        floatingActionButton = {
                            ExtendedFloatingActionButton(
                                onClick = { /* fab click handler */ }
                            ) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                            }
                        }) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            recipeViewModel.currentRecipe?.let { RecipeView(recipe = it) } ?: Text(
                                text = "Missing recipe"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    RecipesTheme {
        Greeting("Android")
    }
}