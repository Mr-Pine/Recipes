package de.mr_pine.recipes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.mr_pine.recipes.screens.Destination
import de.mr_pine.recipes.screens.Destination.HOME
import de.mr_pine.recipes.screens.Destination.RECIPE

@ExperimentalMaterial3Api
@Composable
fun NavDrawerContent(currentDestination: Destination, navigate: (Destination) -> Unit) {
    ModalDrawerSheet() {
        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
            Spacer(modifier = Modifier.statusBarsPadding())
            NavigationDrawerItem(label = { Text(text = "Home") }, selected = currentDestination == HOME, onClick = { navigate(HOME) })
            NavigationDrawerItem(label = { Text(text = "Rezept") }, selected = currentDestination == RECIPE, onClick = { navigate(RECIPE) })
        }
    }
}