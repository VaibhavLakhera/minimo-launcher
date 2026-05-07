package com.minimo.launcher.ui.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.minimo.launcher.R
import com.minimo.launcher.ui.theme.Dimens

@Composable
fun HomeShortcutItem(
    modifier: Modifier,
    shortcutName: String,
    isWorkProfile: Boolean,
    appsArrangement: Arrangement.Horizontal,
    textSize: TextUnit,
    verticalPadding: Dp = 16.dp,
    onClick: () -> Unit,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    shadow: Shadow? = null
) {
    val lineHeight by remember { derivedStateOf { textSize * 1.2 } }

    val paddingValues = remember(isWorkProfile) {
        if (isWorkProfile) {
            PaddingValues(
                start = 0.dp,
                end = Dimens.APP_HORIZONTAL_SPACING,
                top = verticalPadding,
                bottom = verticalPadding
            )
        } else {
            PaddingValues(horizontal = Dimens.APP_HORIZONTAL_SPACING, vertical = verticalPadding)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(paddingValues),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = appsArrangement
    ) {
        if (isWorkProfile) {
            if (shadow != null) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_work_profile),
                    modifier = Modifier
                        .size(16.dp)
                        .padding(horizontal = 8.dp)
                        .offset(
                            x = with(LocalDensity.current) { shadow.offset.x.toDp() },
                            y = with(LocalDensity.current) { shadow.offset.y.toDp() }
                        )
                        .blur(with(LocalDensity.current) { shadow.blurRadius.toDp() }),
                    tint = shadow.color,
                    contentDescription = null
                )
            }
            Icon(
                painter = painterResource(id = R.drawable.ic_work_profile),
                modifier = Modifier
                    .size(16.dp)
                    .padding(horizontal = 8.dp),
                tint = textColor,
                contentDescription = null
            )
        }

        Text(
            text = shortcutName,
            color = textColor,
            fontSize = textSize,
            lineHeight = lineHeight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = LocalTextStyle.current.copy(shadow = shadow)
        )
    }
}
