package com.minimo.launcher.ui.home.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimo.launcher.ui.entities.AppInfo
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun AppDrawerFastScroller(
    apps: List<AppInfo>,
    listState: LazyListState,
    modifier: Modifier = Modifier,
    onInteractionStart: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val hapticFeedback = LocalHapticFeedback.current

    val letters = remember(apps) {
        val seen = linkedSetOf<String>()
        var hasNonLetters = false

        apps.forEach { appInfo ->
            val firstChar = appInfo.name.firstOrNull()?.uppercaseChar()
            when {
                firstChar?.isLetter() == true -> seen.add(firstChar.toString())
                else -> hasNonLetters = true
            }
        }

        buildList {
            if (hasNonLetters) {
                add("#")
            }
            addAll(seen.sorted())
        }
    }

    val letterToIndexMap = remember(apps) {
        val mapping = mutableMapOf<String, Int>()
        apps.forEachIndexed { index, appInfo ->
            val firstChar = appInfo.name.firstOrNull()?.uppercaseChar()
            val letter = if (firstChar?.isLetter() == true) {
                firstChar.toString()
            } else {
                "#"
            }
            if (!mapping.containsKey(letter)) {
                mapping[letter] = index
            }
        }
        mapping
    }

    var isInteracting by remember { mutableStateOf(false) }
    var currentDragPosition by remember { mutableFloatStateOf(0f) }
    var lettersColumnTop by remember { mutableFloatStateOf(0f) }
    var lettersColumnHeight by remember { mutableIntStateOf(0) }
    var currentSelectedLetter by remember { mutableStateOf("") }
    var previousSelectedLetter by remember { mutableStateOf("") }

    LaunchedEffect(apps) {
        isInteracting = false
        currentDragPosition = 0f
    }

    fun scrollToLetter(letter: String) {
        letterToIndexMap[letter]?.let { index ->
            coroutineScope.launch {
                listState.scrollToItem(index = index)
            }
        }
    }

    fun handlePositionAndSelectLetter(yPosition: Float) {
        if (lettersColumnHeight <= 0 || letters.isEmpty()) return

        val letterHeightPx = lettersColumnHeight.toFloat() / letters.size
        val relativeY = (yPosition - lettersColumnTop).coerceIn(0f, lettersColumnHeight - 1f)
        val letterIndex = (relativeY / letterHeightPx).toInt().coerceIn(0, letters.size - 1)
        val selectedLetter = letters[letterIndex]

        if (selectedLetter != previousSelectedLetter) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            previousSelectedLetter = selectedLetter
        }

        currentSelectedLetter = selectedLetter
        scrollToLetter(selectedLetter)
    }

    val width = 40.dp
    val letterHeight = 20.dp

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(width)
            .background(
                color = Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            )
            .pointerInput(apps) {
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitFirstDown()
                        onInteractionStart()
                        isInteracting = true

                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)

                        currentDragPosition = down.position.y
                        handlePositionAndSelectLetter(down.position.y)

                        val change = awaitTouchSlopOrCancellation(down.id) { change, _ ->
                            change.consume()
                        }

                        if (change != null) {
                            drag(change.id) { dragChange ->
                                currentDragPosition = dragChange.position.y
                                handlePositionAndSelectLetter(currentDragPosition)
                            }
                        }
                        isInteracting = false
                        previousSelectedLetter = ""
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .wrapContentHeight()
                .width(width)
                .padding(vertical = 16.dp)
                .onGloballyPositioned { layoutCoordinates ->
                    lettersColumnTop = layoutCoordinates.positionInParent().y
                    lettersColumnHeight = layoutCoordinates.size.height
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val letterHeightPx = with(density) { letterHeight.toPx() }
            val currentPosition = if (lettersColumnHeight > 0) {
                val relativeY = currentDragPosition - lettersColumnTop
                (relativeY / letterHeightPx).coerceIn(0f, letters.size.toFloat() - 1f)
            } else {
                0f
            }

            letters.forEachIndexed { index, letter ->
                val distance = abs(index - currentPosition)

                val scale by animateFloatAsState(
                    targetValue = if (isInteracting) {
                        when {
                            distance < 0.5f -> 1.6f
                            distance < 1.5f -> 1.3f
                            distance < 2.5f -> 1.1f
                            else -> 0.9f
                        }
                    } else {
                        1.0f
                    },
                    animationSpec = spring(dampingRatio = 0.8f),
                    label = "letter_scale_$index"
                )

                val alpha by animateFloatAsState(
                    targetValue = if (isInteracting) {
                        when {
                            distance < 0.5f -> 1.0f
                            distance < 1.5f -> 0.9f
                            distance < 2.5f -> 0.7f
                            else -> 0.5f
                        }
                    } else {
                        0.7f
                    },
                    animationSpec = spring(),
                    label = "letter_alpha_$index"
                )

                Box(
                    modifier = Modifier
                        .height(letterHeight)
                        .width(width),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            alpha = alpha
                        )
                    )
                }
            }
        }

        if (isInteracting && currentSelectedLetter.isNotEmpty()) {
            FloatingLetterIndicator(
                letter = currentSelectedLetter,
                yPosition = currentDragPosition
            )
        }
    }
}

@Composable
private fun FloatingLetterIndicator(
    letter: String,
    yPosition: Float,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val indicatorSize = 80.dp

    Surface(
        modifier = modifier
            .size(indicatorSize)
            .aspectRatio(1f)
            .offset {
                IntOffset(
                    x = with(density) { (-60).dp.roundToPx() },
                    y = (yPosition - with(density) { (indicatorSize / 2).toPx() }).toInt()
                )
            },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.onSurface
    ) {
        Text(
            text = letter,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.surface,
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Proportional,
                    trim = LineHeightStyle.Trim.Both
                )
            ),
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
        )
    }
}
