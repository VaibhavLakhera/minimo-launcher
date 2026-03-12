package com.minimo.launcher.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimo.launcher.R
import com.minimo.launcher.ui.theme.Dimens
import com.minimo.launcher.utils.isInstalledFromPlayStore
import com.minimo.launcher.utils.openHomeSettings
import com.minimo.launcher.utils.openPlayStorePage
import com.minimo.launcher.utils.openSeniorLauncherPlayStorePage
import com.minimo.launcher.utils.sendFeedback

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onFavouriteAppsClick: () -> Unit,
    onHiddenAppsClick: () -> Unit,
    onCustomisationClick: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.settings)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            SettingsItem(
                name = stringResource(R.string.favourite_apps),
                onClick = onFavouriteAppsClick
            )
            SettingsItem(name = stringResource(R.string.hidden_apps), onClick = onHiddenAppsClick)
            SettingsItem(
                name = stringResource(R.string.customisation),
                onClick = onCustomisationClick
            )
            SettingsItem(
                name = stringResource(R.string.set_default_launcher),
                onClick = context::openHomeSettings
            )
            SettingsItem(
                name = stringResource(R.string.send_feedback),
                onClick = context::sendFeedback
            )
            if (context.isInstalledFromPlayStore()) {
                SettingsItem(
                    name = stringResource(R.string.rate_application),
                    onClick = context::openPlayStorePage
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            Text(
                text = stringResource(id = R.string.discover_my_other_app),
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = Dimens.APP_HORIZONTAL_SPACING,
                        top = 20.dp,
                        end = Dimens.APP_HORIZONTAL_SPACING,
                        bottom = 8.dp
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { context.openSeniorLauncherPlayStorePage() }
                    .padding(horizontal = Dimens.APP_HORIZONTAL_SPACING, vertical = 16.dp)) {
                Text(
                    text = stringResource(R.string.senior_launcher),
                    fontSize = 20.sp,
                    lineHeight = 24.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.senior_launcher_description)
                )
            }

        }
    }
}

@Composable
private fun SettingsItem(name: String, onClick: () -> Unit) {
    Text(
        text = name,
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 20.sp,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick
            )
            .padding(horizontal = Dimens.APP_HORIZONTAL_SPACING, vertical = 16.dp),
    )
}