package de.mr_pine.recipes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.mr_pine.recipes.screens.Destination
import de.mr_pine.recipes.screens.Destination.*

@ExperimentalMaterial3Api
@Composable
fun NavDrawerContent(currentDestination: Destination, navigate: (Destination) -> Unit) {
    LazyColumn{
        item { Box(modifier = Modifier.statusBarsPadding()) }
        item { NavigationDrawerItem(label = { Text(text = "Home") }, selected = currentDestination == HOME, onClick = { navigate(HOME) }) }
        item { NavigationDrawerItem(label = { Text(text = "Rezept") }, selected = currentDestination == RECIPE, onClick = { navigate(RECIPE) }) }
    }
}