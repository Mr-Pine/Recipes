package de.mr_pine.recipes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import de.mr_pine.recipes.models.Recipe
import de.mr_pine.recipes.screens.RecipeView
import de.mr_pine.recipes.screens.ShowError
import de.mr_pine.recipes.ui.theme.HarmonizedTheme
import de.mr_pine.recipes.ui.theme.RecipesTheme
import de.mr_pine.recipes.viewModels.RecipeViewModel
import kotlinx.coroutines.launch

/*
 * TODO: Splash screen: https://developer.android.com/guide/topics/ui/splash-screen
 * TODO: Maybe animated icons: https://www.youtube.com/watch?v=hiDaPrcZbco
 * TODO: Think about navigation: https://developer.android.com/jetpack/compose/navigation, JetNews
 * TODO: Nav Drawer Content: https://github.com/Mr-Pine/Shintaikan/tree/update-dependecies
 * TODO: Loading of recipes
 * TODO: Main navigation from Homescreen
 */

@ExperimentalFoundationApi
@ExperimentalMaterial3Api
@ExperimentalAnimationApi
@ExperimentalMaterialApi
class MainActivity : ComponentActivity() {

    var tryCloseNavigationDrawer: () -> Boolean = { false }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {

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
                val coroutineScope = rememberCoroutineScope()

                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                tryCloseNavigationDrawer = remember(drawerState) {
                    {
                        val open = drawerState.isOpen
                        coroutineScope.launch { drawerState.close() }
                        open
                    }
                }

                // A surface container using the 'background' color from the theme

                ModalNavigationDrawer(
                    drawerContent = { Text("Drawer") },
                    drawerState = drawerState,
                    modifier = Modifier.imePadding()
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Scaffold(
                            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                            topBar = {
                                val containerColor by TopAppBarDefaults.smallTopAppBarColors()
                                    .containerColor(scrollBehavior.scrollFraction)
                                Box(modifier = Modifier.background(containerColor)) {
                                    SmallTopAppBar(
                                        modifier = Modifier
                                            .statusBarsPadding(),
                                        title = {
                                            Text(
                                                recipeViewModel.currentRecipe?.metadata?.title
                                                    ?: "No recipe specified"
                                            )
                                        },
                                        navigationIcon = {
                                            IconButton(
                                                onClick = { coroutineScope.launch { drawerState.open() } }
                                            ) {
                                                Icon(
                                                    Icons.Filled.Menu,
                                                    contentDescription = "Localized description"
                                                )
                                            }
                                        },
                                        scrollBehavior = scrollBehavior
                                    )
                                }
                            },
                            floatingActionButtonPosition = FabPosition.End,
                            floatingActionButton = {
                                ExtendedFloatingActionButton(
                                    onClick = { /* fab click handler */ }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null
                                    )
                                }
                            }) { innerPadding ->
                            Box(modifier = Modifier.padding(innerPadding)) {
                                recipeViewModel.currentRecipe?.let { RecipeView(recipe = it) }
                                    ?: Text(
                                        text = "Missing recipe"
                                    )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        if(!tryCloseNavigationDrawer()) super.onBackPressed()
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