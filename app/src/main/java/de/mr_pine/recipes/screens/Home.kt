package de.mr_pine.recipes.screens

import android.util.Log
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.mr_pine.recipes.viewModels.RecipeViewModel

private const val TAG = "Home"

@ExperimentalMaterial3Api
@Composable
fun Home(viewModel: RecipeViewModel) {
    Home()
}

@ExperimentalMaterial3Api
@Composable
fun Home() {
    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        decayAnimationSpec,
        rememberTopAppBarScrollState()
    )

    Log.d(TAG, "Home: ${MaterialTheme.colorScheme}")

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Large TopAppBar") },
                actions = {
                    IconButton(onClick = { /* doSomething() */ }) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Localized description"
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = {/*TODO*/ }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "menu"
                        )
                    }
                }
            )
        },
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .statusBarsPadding()
            .imePadding()
    ) { paddingValues ->
        Box(Modifier.padding(paddingValues)) {
            LazyColumn {
                items(20) {
                    Text("hi")
                }
            }
        }
    }
}

@Composable
fun SearchAppBar(
    searchContent: String,
    setSearchContent: (String) -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    colors: TopAppBarColors = TopAppBarDefaults.largeTopAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
    collapsedHeight: Dp = 64.dp,
    expandedHeight: Dp = 152.dp,
    titleBottomPadding: Dp = 28.dp
) {
    if (expandedHeight <= collapsedHeight) {
        throw IllegalArgumentException(
            "A TwoRowsTopAppBar max height should be greater than its pinned height"
        )
    }
    val collapsedHeightPx: Float
    val expandedHeightPx: Float
    val titleBottomPaddingPx: Int
    LocalDensity.current.run {
        collapsedHeightPx = collapsedHeight.toPx()
        expandedHeightPx = expandedHeight.toPx()
        titleBottomPaddingPx = titleBottomPadding.roundToPx()
    }

    // Set a scroll offset limit that will hide just the title area and will keep the small title
    // area visible.
    SideEffect {
        if (scrollBehavior?.state?.offsetLimit != collapsedHeightPx - expandedHeightPx) {
            scrollBehavior?.state?.offsetLimit = collapsedHeightPx - expandedHeightPx
        }
    }

    val scrollPercentage =
        if (scrollBehavior == null || scrollBehavior.state.offsetLimit == 0f) {
            0f
        } else {
            scrollBehavior.state.offset / scrollBehavior.state.offsetLimit
        }

    // Obtain the container Color from the TopAppBarColors.
    // This will potentially animate or interpolate a transition between the container color and the
    // container's scrolled color according to the app bar's scroll state.
    val scrollFraction = scrollBehavior?.scrollFraction ?: 0f
    val appBarContainerColor by colors.containerColor(scrollFraction)

    // Wrap the given actions in a Row.
    val actionsRow = @Composable {
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
            content = actions
        )
    }
}