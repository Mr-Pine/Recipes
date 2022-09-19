package de.mr_pine.recipes

import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import de.mr_pine.recipes.screens.Destination
import de.mr_pine.recipes.screens.RecipeNavHost
import de.mr_pine.recipes.ui.theme.HarmonizedTheme
import de.mr_pine.recipes.ui.theme.RecipesTheme
import de.mr_pine.recipes.viewModels.RecipeViewModel
import de.mr_pine.recipes.viewModels.RecipeViewModelFactory
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import net.pwall.json.schema.JSONSchema
import java.io.File

/*
 * TODO: Splash screen: https://developer.android.com/guide/topics/ui/splash-screen
 * TODO: Maybe animated icons: https://www.youtube.com/watch?v=hiDaPrcZbco
 * TODO: Think about navigation: https://developer.android.com/jetpack/compose/navigation, JetNews
 * TODO: Nav Drawer Content: https://github.com/Mr-Pine/Shintaikan/tree/update-dependecies
 * TODO: Loading of recipes & receiving of intents, importing files
 * TODO: Main navigation from Homescreen
 * TODO: Implement Settings (see XKCDFeed)
 * TODO: No screen lock when viewing Recipe
 */

private const val TAG = "MainActivity"


@ExperimentalMaterialApi
@ExperimentalSerializationApi
@ExperimentalFoundationApi
@ExperimentalMaterial3Api
@ExperimentalAnimationApi
class MainActivity : ComponentActivity() {

    var closeNavigationDrawer: () -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        resources.openRawResource(R.raw.rezept).bufferedReader().readText()

        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val onBackPressedCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                closeNavigationDrawer()
            }
        }
        onBackPressedDispatcher.addCallback(onBackPressedCallback)

        setContent {

            val recipeFolder =
                File(getExternalFilesDir(null), getString(R.string.externalFiles_subfolder))

            val recipeViewModel: RecipeViewModel =
                viewModel(
                    factory = RecipeViewModelFactory(
                        recipeFolder = recipeFolder,
                        recipeSchema = JSONSchema.parse(
                            resources.openRawResource(R.raw.rcp).bufferedReader().readText()
                        )
                    )
                )

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
                            recipeViewModel.saveRecipeFileContent(
                                it,
                                filename,
                                true
                            )
                        }
                        Log.d(TAG, "onCreate: importing $content")
                    }
                }

            recipeViewModel.importRecipe = { recipeImporter.launch(arrayOf("application/*")) }

            HarmonizedTheme {
                val coroutineScope = rememberCoroutineScope()


                val navHostController = rememberNavController()

                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed) {
                    onBackPressedCallback.isEnabled = it == DrawerValue.Open
                    navHostController.enableOnBackPressed(it == DrawerValue.Closed)
                    true
                }

                closeNavigationDrawer = {
                    coroutineScope.launch { drawerState.close() }
                    onBackPressedCallback.isEnabled = false
                    navHostController.enableOnBackPressed(true)
                }


                recipeViewModel.showNavDrawer = {
                    coroutineScope.launch { drawerState.open() }
                    onBackPressedCallback.isEnabled = true
                    navHostController.enableOnBackPressed(false)
                }
                recipeViewModel.navigate = { navHostController.navigate(it.toString()) }

                ModalNavigationDrawer(
                    drawerContent = {
                        NavDrawerContent(
                            currentDestination = Destination.values()
                                .find { it.toString() == navHostController.currentBackStackEntryAsState().value?.destination?.route }
                                ?: Destination.RECIPE) { navHostController.navigate(it.toString()); closeNavigationDrawer() }
                    },
                    drawerState = drawerState,
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