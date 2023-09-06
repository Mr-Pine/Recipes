package de.mr_pine.recipes.android

import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import de.mr_pine.recipes.android.screens.Destination
import de.mr_pine.recipes.android.screens.RecipeNavHost
import de.mr_pine.recipes.android.ui.theme.HarmonizedTheme
import de.mr_pine.recipes.android.viewModels.RecipeViewModel
import de.mr_pine.recipes.android.viewModels.RecipeViewModelFactory
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import net.pwall.json.schema.JSONSchema
import java.io.File

private const val TAG = "MainActivity"


@ExperimentalSerializationApi
@ExperimentalFoundationApi
@ExperimentalMaterial3Api
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

            rememberCoroutineScope()

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
                        contentResolver.openInputStream(uri)?.use {
                            val content = it.reader().readText()
                            recipeViewModel.saveRecipeFileContent(
                                content,
                                filename,
                                true
                            )
                            Log.d(TAG, "onCreate: importing $content")
                        }
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