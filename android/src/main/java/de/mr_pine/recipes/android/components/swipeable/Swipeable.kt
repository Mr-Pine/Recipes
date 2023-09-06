package de.mr_pine.recipes.android.components.swipeable

import android.util.Log
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintLayoutScope
import androidx.constraintlayout.compose.Dimension
import kotlin.math.roundToInt

private const val TAG = "Swipeable"

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Swipeable(
    animationSpec: AnimationSpec<Float> = SpringSpec(),
    swipeLeftComposable: (@Composable (offsetAbsolute: Float, offsetRelative: Float) -> Unit)? = null,
    swipeRightComposable: (@Composable (offsetAbsolute: Float, offsetRelative: Float) -> Unit)? = null,
    leftSwiped: (() -> Unit)? = null,
    leftSwipedDone: () -> Unit = {},
    rightSwiped: (() -> Unit)? = null,
    rightSwipedDone: () -> Unit = {},
    anchorPositions: ClosedRange<Dp> = (-100).dp..100.dp,
    positionalThreshold: (distance: Float) -> Float = { distance -> distance * 0.6f },
    velocityThreshold: Dp = 125.dp,
    content: @Composable ConstraintLayoutScope.() -> Unit
) {

    /*
    For reference:
    - https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/material/material/src/commonMain/kotlin/androidx/compose/material/Swipeable.kt;l=468?q=Swipeab&sq=&ss=androidx%2Fplatform%2Fframeworks%2Fsupport:compose%2Fmaterial%2Fmaterial%2Fsrc%2F
    - https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/material/material/src/commonMain/kotlin/androidx/compose/material/Swipeable.kt;l=468?q=Swipeab&ss=androidx%2Fplatform%2Fframeworks%2Fsupport:compose%2Fmaterial%2Fmaterial%2Fsrc%2F
    - https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/material/material/src/commonMain/kotlin/androidx/compose/material/Swipeable.kt;l=563?q=Modifier.swipeable&ss=androidx%2Fplatform%2Fframeworks%2Fsupport:compose%2Fmaterial%2Fmaterial%2Fsrc%2F
     */


    ConstraintLayout {
        val (mainCardRef, actionCardRef) = createRefs()
        val density = LocalDensity.current
        val anchoredDraggableState = remember {
            AnchoredDraggableState(
                initialValue = SwipeCardState.DEFAULT,
                animationSpec = animationSpec,
                velocityThreshold = { with(density) { velocityThreshold.toPx() } },
                positionalThreshold = positionalThreshold,
                anchors = with(density) {
                    androidx.compose.foundation.gestures.DraggableAnchors {
                        SwipeCardState.DEFAULT at 0f
                        if (swipeLeftComposable != null) SwipeCardState.LEFT at anchorPositions.start.toPx()
                        if (swipeRightComposable != null) SwipeCardState.RIGHT at anchorPositions.endInclusive.toPx()
                    }
                },
                confirmValueChange = {
                    Log.d(TAG, "Swipeable: $it")
                    when (it) {
                        SwipeCardState.LEFT -> {
                            leftSwipedDone()
                            false
                        }
                        SwipeCardState.RIGHT -> {
                            rightSwipedDone()
                            false
                        }

                        SwipeCardState.DEFAULT -> true
                    }
                }
            )
        }
        val coroutineScope = rememberCoroutineScope()

        var swipeLeftVisible by remember { mutableStateOf(false) }
        // var swipeRightVisible by remember { mutableStateOf(true) }

        var swipeEnabled by remember { mutableStateOf(true) }

        val cardPadding = 2.dp

        Surface(
            color = Color.Transparent,
            content = {
                Box(modifier = Modifier.padding(0.dp)) {
                    (if (swipeLeftVisible) {
                        swipeLeftComposable
                    } else {
                        swipeRightComposable
                    } ?: { _, _ -> }).invoke(
                        anchoredDraggableState.offset,
                        anchoredDraggableState.progress
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(cardPadding)
                .constrainAs(actionCardRef) {
                    top.linkTo(mainCardRef.top)
                    bottom.linkTo(mainCardRef.bottom)
                    height = Dimension.fillToConstraints
                }
        )

        Surface(
            color = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .offset {
                    val offset = anchoredDraggableState.offset
                        .roundToInt()
                        .let { if ((it < 0 && swipeLeftComposable == null) || (it > 0 && swipeRightComposable == null)) 0 else it }
                    IntOffset(offset, 0)
                }
                .anchoredDraggable(
                    state = anchoredDraggableState,
                    orientation = Orientation.Horizontal,
                    enabled = swipeEnabled
                )
                .constrainAs(mainCardRef) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
        ) {
            /*if (anchoredDraggableState.currentValue == SwipeCardState.LEFT && !anchoredDraggableState.isAnimationRunning) {
                leftSwiped?.invoke()
                coroutineScope.launch {
                    swipeEnabled = false
                    anchoredDraggableState.animateTo(SwipeCardState.DEFAULT)
                    swipeEnabled = true
                    leftSwipedDone()
                }
            } else if (anchoredDraggableState.currentValue == SwipeCardState.RIGHT && !anchoredDraggableState.isAnimationRunning) {
                rightSwiped?.invoke()
                coroutineScope.launch {
                    swipeEnabled = false
                    anchoredDraggableState.animateTo(SwipeCardState.DEFAULT)
                    swipeEnabled = true
                    rightSwipedDone()
                }
            }*/

            swipeLeftVisible = anchoredDraggableState.offset <= 0

            val constraints = this

            Box(modifier = Modifier.padding(cardPadding)) {
                with(constraints) { content() }
            }
        }

    }
}

enum class SwipeCardState {
    DEFAULT,
    LEFT,
    RIGHT
}