package edits

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import de.mr_pine.recipes.common.models.RecipeMetadata
import de.mr_pine.recipes.common.views.EditColumn

@ExperimentalMaterial3Api
@Composable
fun RecipeMetadata.EditCard() {
    val focusRequester = remember { FocusRequester() }

    ElevatedCard(modifier = Modifier.padding(bottom = 4.dp, start = 4.dp, end = 4.dp)) {
        Column(modifier = Modifier.padding(10.dp)) {
            EditColumn(focusRequester = focusRequester)
        }
    }

    LaunchedEffect(this) {
        focusRequester.requestFocus()
    }
}