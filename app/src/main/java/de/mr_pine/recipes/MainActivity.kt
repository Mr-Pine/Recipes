package de.mr_pine.recipes

import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import de.mr_pine.recipes.models.Destination
import de.mr_pine.recipes.models.RecipeNavHost
import de.mr_pine.recipes.ui.theme.HarmonizedTheme
import de.mr_pine.recipes.ui.theme.RecipesTheme
import de.mr_pine.recipes.viewModels.RecipeViewModel
import de.mr_pine.recipes.viewModels.RecipeViewModelFactory
import kotlinx.coroutines.launch
import java.io.File

/*
 * TODO: Splash screen: https://developer.android.com/guide/topics/ui/splash-screen
 * TODO: Maybe animated icons: https://www.youtube.com/watch?v=hiDaPrcZbco
 * TODO: Think about navigation: https://developer.android.com/jetpack/compose/navigation, JetNews
 * TODO: Nav Drawer Content: https://github.com/Mr-Pine/Shintaikan/tree/update-dependecies
 * TODO: Loading of recipes & receiving of intents, importing files
 * TODO: Main navigation from Homescreen
 * TODO: Implement Settings (see XKCDFeed)
 */

private const val TAG = "MainActivity"


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


            val recipeFolder =
                File(getExternalFilesDir(null), getString(R.string.externalFiles_subfolder))

            val recipeViewModel: RecipeViewModel =
                viewModel(factory = RecipeViewModelFactory(recipeFolder))

            LaunchedEffect(null) {
                recipeViewModel.loadRecipeFiles()
            }

            val recipeImporter =
                rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                    val filename = uri?.let {
                        contentResolver.query(it, arrayOf(OpenableColumns.DISPLAY_NAME), null, null)
                            ?.apply { moveToFirst() }
                            ?.let { cursor ->
                                val filename = cursor.getString(0)
                                cursor.close()
                                filename
                            }
                    }
                    if (filename?.endsWith(".rcp") == true) {
                        val content = contentResolver.openInputStream(uri)?.reader()?.readText()
                        content?.let {
                            recipeViewModel.saveRecipeFile(
                                it,
                                filename
                            )
                        }
                       Log.d(TAG, "onCreate: importing $content")
                    }
                }

            recipeViewModel.importRecipe = { recipeImporter.launch(arrayOf("application/*")) }

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

                val navHostController = rememberNavController()

                recipeViewModel.showNavDrawer = { coroutineScope.launch { drawerState.open() } }
                recipeViewModel.navigate = { navHostController.navigate(it.toString()) }

                // A surface container using the 'background' color from the theme

                ModalNavigationDrawer(
                    drawerContent = {
                        NavDrawerContent(
                            currentDestination = Destination.values()
                                .find { it.toString() == navHostController.currentBackStackEntryAsState().value?.destination?.route }
                                ?: Destination.RECIPE) { navHostController.navigate(it.toString()); tryCloseNavigationDrawer() }
                    },
                    drawerState = drawerState,
                    modifier = Modifier.imePadding()
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        //Home(viewModel = recipeViewModel)
                        //RecipeView(viewModel = recipeViewModel)
                        RecipeNavHost(
                            navController = navHostController,
                            viewModel = recipeViewModel
                        )
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        if (!tryCloseNavigationDrawer()) super.onBackPressed()
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