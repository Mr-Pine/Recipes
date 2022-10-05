package de.mr_pine.recipes.android.components.swipeabe

import android.annotation.SuppressLint
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
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
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@SuppressLint("CoroutineCreationDuringComposition")
@ExperimentalMaterialApi
@Composable
fun Swipeable(
    animationSpec: AnimationSpec<Float> = SwipeableDefaults.AnimationSpec,
    swipeLeftComposable: (@Composable (offsetAbsolute: Float, offsetRelative: Float) -> Unit)? = null,
    swipeRightComposable: (@Composable (offsetAbsolute: Float, offsetRelative: Float) -> Unit)? = null,
    leftSwiped: (() -> Unit)? = null,
    leftSwipedDone: () -> Unit = {},
    rightSwiped: (() -> Unit)? = null,
    rightSwipedDone: () -> Unit = {},
    anchorPositions: ClosedRange<Dp> = (-100).dp..100.dp,
    thresholds: (from: SwipeCardState, to: SwipeCardState) -> ThresholdConfig = { _, _ ->
        FractionalThreshold(
            0.6f
        )
    },
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
        val swipeableState = rememberSwipeableState(
            initialValue = SwipeCardState.DEFAULT,
            animationSpec = animationSpec
        )
        val coroutineScope = rememberCoroutineScope()

        var swipeLeftVisible by remember { mutableStateOf(false) }
//        var swipeRightVisible by remember { mutableStateOf(true) }

        var swipeEnabled by remember { mutableStateOf(true) }
        val anchorPositionsPx = with(LocalDensity.current) {
            anchorPositions.start.toPx()..anchorPositions.endInclusive.toPx()
        }

        val anchors = hashMapOf(0f to SwipeCardState.DEFAULT)

        if (swipeLeftComposable != null) anchors[anchorPositionsPx.start] = SwipeCardState.LEFT
        if (swipeRightComposable != null) anchors[anchorPositionsPx.endInclusive] =
            SwipeCardState.RIGHT

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
                        swipeableState.offset.value,
                        swipeableState.progress.fraction
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
                    val offset = swipeableState.offset.value
                        .roundToInt()
                        .let { if ((it < 0 && swipeLeftComposable == null) || (it > 0 && swipeRightComposable == null)) 0 else it }
                    IntOffset(offset, 0)
                }
                .swipeable(
                    state = swipeableState,
                    anchors = anchors,
                    orientation = Orientation.Horizontal,
                    enabled = swipeEnabled,
                    thresholds = thresholds,
                    velocityThreshold = velocityThreshold
                )
                .constrainAs(mainCardRef) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
        ) {
            if (swipeableState.currentValue == SwipeCardState.LEFT && !swipeableState.isAnimationRunning) {
                leftSwiped?.invoke()
                coroutineScope.launch {
                    swipeEnabled = false
                    swipeableState.animateTo(SwipeCardState.DEFAULT)
                    swipeEnabled = true
                    leftSwipedDone()
                }
            } else if (swipeableState.currentValue == SwipeCardState.RIGHT && !swipeableState.isAnimationRunning) {
                rightSwiped?.invoke()
                coroutineScope.launch {
                    swipeEnabled = false
                    swipeableState.animateTo(SwipeCardState.DEFAULT)
                    swipeEnabled = true
                    rightSwipedDone()
                }
            }

            swipeLeftVisible = swipeableState.offset.value <= 0

            val contstraints = this

            Box(modifier = Modifier.padding(cardPadding)) {
                with(contstraints) { content() }
            }
        }

    }
}

enum class SwipeCardState {
    DEFAULT,
    LEFT,
    RIGHT
}