package com.minimo.launcher.ui.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.minimo.launcher.R
import com.minimo.launcher.ui.theme.Dimens

@Composable
fun appIconSizeFor(textSize: TextUnit, scale: Float): Dp {
    return with(LocalDensity.current) { (textSize * scale).toDp() }
}

@Composable
fun AppIcon(
    image: ImageBitmap?,
    size: Dp,
    isWorkProfile: Boolean,
    showNotificationDot: Boolean,
    shape: Shape = CircleShape
) {
    Box(modifier = Modifier.size(size)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(
                    elevation = Dimens.APP_ICON_SHADOW_ELEVATION,
                    shape = shape
                )
        ) {
            if (image != null) {
                Image(
                    bitmap = image,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        if (showNotificationDot) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(size * NOTIFICATION_DOT_SCALE)
                    .shadow(
                        elevation = NOTIFICATION_DOT_SHADOW_ELEVATION,
                        shape = CircleShape,
                        clip = false
                    )
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
        }

        if (isWorkProfile) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .size(size * WORK_BADGE_SCALE)
                    .shadow(
                        elevation = WORK_BADGE_SHADOW_ELEVATION,
                        shape = CircleShape,
                        clip = false
                    )
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_work_profile),
                    contentDescription = null,
                    tint = WORK_BADGE_ICON_COLOR,
                    modifier = Modifier.size(size * WORK_ICON_SCALE)
                )
            }
        }
    }
}

private const val NOTIFICATION_DOT_SCALE = 0.28f
private const val WORK_BADGE_SCALE = 0.46f
private const val WORK_ICON_SCALE = 0.30f
private val NOTIFICATION_DOT_SHADOW_ELEVATION = 1.dp
private val WORK_BADGE_SHADOW_ELEVATION = 1.dp
private val WORK_BADGE_ICON_COLOR = Color(0xFF202124)
