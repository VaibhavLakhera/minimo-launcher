package com.minimo.launcher.ui.settings.customisation

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.minimo.launcher.R
import com.minimo.launcher.ui.components.DropdownView
import com.minimo.launcher.ui.theme.Dimens
import com.minimo.launcher.ui.theme.ThemeMode
import com.minimo.launcher.utils.AndroidUtils
import com.minimo.launcher.utils.Constants
import com.minimo.launcher.utils.HomeAppsAlignment
import com.minimo.launcher.utils.HomeClockAlignment
import com.minimo.launcher.utils.HomeClockMode
import com.minimo.launcher.utils.StringUtils
import com.minimo.launcher.utils.hasLockScreenPermission
import com.minimo.launcher.utils.isNotificationPermissionGranted
import com.minimo.launcher.utils.openNotificationSettings
import com.minimo.launcher.utils.removeLockScreenPermission
import com.minimo.launcher.utils.requestLockScreenPermission
import kotlin.math.roundToInt

@Composable
fun CustomisationScreen(
    viewModel: CustomisationViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    var showEnableAccessibilityDialog by remember { mutableStateOf(false) }
    var showEnableNotificationPermissionDialog by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            if (!context.hasLockScreenPermission()) {
                viewModel.onLockScreenPermissionNotEnableOnStarted()
            }

            if (!context.isNotificationPermissionGranted()) {
                viewModel.onNotificationPermissionNotGrantedOnStarted()
            }
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 4.dp)
                    .height(64.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.customisation),
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 64.dp)
                )

                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            ThemeDropdown(
                selectedOption = StringUtils.themeModeText(
                    context = context,
                    mode = state.themeMode
                ),
                options = listOf(
                    ThemeMode.System to StringUtils.themeModeText(
                        context,
                        ThemeMode.System
                    ),
                    ThemeMode.Dark to StringUtils.themeModeText(
                        context,
                        ThemeMode.Dark
                    ),
                    ThemeMode.Light to StringUtils.themeModeText(
                        context,
                        ThemeMode.Light
                    )
                ),
                onOptionSelected = { selected ->
                    viewModel.onThemeModeChanged(selected)
                }
            )

            ToggleItem(
                title = stringResource(R.string.black_theme),
                subtitle = stringResource(R.string.applied_only_when_the_app_theme_is_in_dark_mode),
                isChecked = state.blackTheme,
                onToggleClick = viewModel::onToggleBlackTheme
            )

            if (AndroidUtils.isDynamicThemeSupported()) {
                Spacer(modifier = Modifier.height(4.dp))

                ToggleItem(
                    title = stringResource(R.string.dynamic_colours),
                    subtitle = stringResource(R.string.adapt_theme_colours_based_on_system_settings),
                    isChecked = state.dynamicTheme,
                    onToggleClick = viewModel::onToggleDynamicTheme
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.padding(
                    horizontal = Dimens.APP_HORIZONTAL_SPACING,
                )
            ) {
                Text(
                    text = stringResource(R.string.home_app_size),
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = state.homeTextSize.roundToInt().toString(),
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Slider(
                modifier = Modifier
                    .padding(horizontal = Dimens.APP_HORIZONTAL_SPACING),
                value = state.homeTextSize,
                onValueChange = {
                    viewModel.onHomeTextSizeChanged(it.roundToInt())
                },
                valueRange = Constants.HOME_TEXT_SIZE_RANGE,
                steps = 16,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.padding(
                    horizontal = Dimens.APP_HORIZONTAL_SPACING,
                )
            ) {
                Text(
                    text = stringResource(R.string.home_app_spacing),
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = state.homeAppVerticalPadding.roundToInt().toString(),
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Slider(
                modifier = Modifier
                    .padding(horizontal = Dimens.APP_HORIZONTAL_SPACING),
                value = state.homeAppVerticalPadding,
                onValueChange = {
                    viewModel.onHomeVerticalPaddingChanged(it.roundToInt())
                },
                valueRange = Constants.HOME_VERTICAL_PADDING_RANGE,
                steps = 7,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                stringResource(R.string.sample_app),
                fontSize = state.homeTextSize.sp,
                lineHeight = state.homeTextSize.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
                    .padding(
                        horizontal = Dimens.APP_HORIZONTAL_SPACING,
                        vertical = state.homeAppVerticalPadding.dp
                    )
            )

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            ToggleItem(
                title = stringResource(R.string.apply_to_all_apps),
                subtitle = stringResource(R.string.apply_the_home_app_size_to_all_apps_in_the_app_drawer),
                isChecked = state.applyHomeAppSizeToAllApps,
                onToggleClick = viewModel::onToggleApplyHomeAppSizeToAllApps
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            AppsAlignmentDropdown(
                selectedOption = StringUtils.homeAppsAlignmentText(
                    context = context,
                    alignment = state.homeAppsAlignment
                ),
                options = listOf(
                    HomeAppsAlignment.Start to StringUtils.homeAppsAlignmentText(
                        context,
                        HomeAppsAlignment.Start
                    ),
                    HomeAppsAlignment.Center to StringUtils.homeAppsAlignmentText(
                        context,
                        HomeAppsAlignment.Center
                    ),
                    HomeAppsAlignment.End to StringUtils.homeAppsAlignmentText(
                        context,
                        HomeAppsAlignment.End
                    ),
                ),
                onOptionSelected = { selected ->
                    viewModel.onHomeAppsAlignmentChanged(selected)
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            ToggleItem(
                title = stringResource(R.string.show_home_clock),
                isChecked = state.showHomeClock,
                onToggleClick = viewModel::onToggleShowHomeClock
            )
            if (state.showHomeClock) {
                Spacer(modifier = Modifier.height(4.dp))

                ClockAlignmentDropdown(
                    selectedOption = StringUtils.homeClockAlignmentText(
                        context = context,
                        alignment = state.homeClockAlignment
                    ),
                    options = listOf(
                        HomeClockAlignment.Start to StringUtils.homeClockAlignmentText(
                            context,
                            HomeClockAlignment.Start
                        ),
                        HomeClockAlignment.Center to StringUtils.homeClockAlignmentText(
                            context,
                            HomeClockAlignment.Center
                        ),
                        HomeClockAlignment.End to StringUtils.homeClockAlignmentText(
                            context,
                            HomeClockAlignment.End
                        ),
                    ),
                    onOptionSelected = { selected ->
                        viewModel.onHomeClockAlignmentChanged(selected)
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                ClockModeDropdown(
                    selectedOption = StringUtils.homeClockModeText(
                        context = context,
                        mode = state.homeClockMode
                    ),
                    options = listOf(
                        HomeClockMode.Full to StringUtils.homeClockModeText(
                            context,
                            HomeClockMode.Full
                        ),
                        HomeClockMode.TimeOnly to StringUtils.homeClockModeText(
                            context,
                            HomeClockMode.TimeOnly
                        ),
                        HomeClockMode.DateOnly to StringUtils.homeClockModeText(
                            context,
                            HomeClockMode.DateOnly
                        ),
                    ),
                    onOptionSelected = { selected ->
                        viewModel.onHomeClockModeChanged(selected)
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                ToggleItem(
                    title = stringResource(R.string.twenty_four_hour_format),
                    isChecked = state.twentyFourHourFormat,
                    onToggleClick = viewModel::onToggleTwentyFourHourFormat
                )

                if (state.homeClockMode == HomeClockMode.DateOnly ||
                    state.homeClockMode == HomeClockMode.Full
                ) {
                    Spacer(modifier = Modifier.height(4.dp))

                    ToggleItem(
                        title = stringResource(R.string.show_battery_level),
                        isChecked = state.showBatteryLevel,
                        onToggleClick = viewModel::onToggleShowBatteryLevel
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            ToggleItem(
                title = stringResource(R.string.show_status_bar),
                isChecked = state.showStatusBar,
                onToggleClick = viewModel::onToggleShowStatusBar
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            ToggleItem(
                title = stringResource(R.string.show_keyboard),
                subtitle = stringResource(R.string.show_keyboard_when_the_drawer_is_opened),
                isChecked = state.autoOpenKeyboardAllApps,
                onToggleClick = viewModel::onToggleAutoOpenKeyboardAllApps
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            ToggleItem(
                title = stringResource(R.string.double_tap_to_lock),
                subtitle = stringResource(R.string.on_home_screen_double_tap_on_empty_space_to_lock),
                isChecked = state.doubleTapToLock,
                onToggleClick = {
                    if (state.doubleTapToLock) {
                        viewModel.onToggleDoubleTapToLock()
                        context.removeLockScreenPermission()
                    } else {
                        if (context.hasLockScreenPermission()) {
                            viewModel.onToggleDoubleTapToLock()
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                showEnableAccessibilityDialog = true
                            } else {
                                viewModel.onToggleDoubleTapToLock()
                                context.requestLockScreenPermission()
                            }
                        }
                    }
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            ToggleItem(
                title = stringResource(R.string.show_hidden_apps),
                subtitle = stringResource(R.string.show_hidden_apps_in_the_search_result_of_the_app_drawer),
                isChecked = state.showHiddenAppsInSearch,
                onToggleClick = viewModel::onToggleShowHiddenAppsInSearch
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            ToggleItem(
                title = stringResource(R.string.search_bar_at_bottom),
                subtitle = stringResource(R.string.change_the_position_of_the_app_drawer_search_bar_to_the_bottom),
                isChecked = state.drawerSearchBarAtBottom,
                onToggleClick = viewModel::onToggleDrawerSearchBarAtBottom
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            ToggleItem(
                title = stringResource(R.string.auto_open_app),
                subtitle = stringResource(R.string.automatically_open_the_app_if_it_is_the_only_search_result),
                isChecked = state.autoOpenApp,
                onToggleClick = viewModel::onToggleAutoOpenApp
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            ToggleItem(
                title = stringResource(R.string.hide_app_drawer_arrow),
                subtitle = stringResource(R.string.hide_app_drawer_arrow_description),
                isChecked = state.hideAppDrawerArrow,
                onToggleClick = viewModel::onToggleHideAppDrawerArrow
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            ToggleItem(
                title = stringResource(R.string.notification_dots),
                subtitle = stringResource(R.string.display_a_notification_dot_on_the_home_screen_apps),
                isChecked = state.notificationDot,
                onToggleClick = {
                    if (state.notificationDot) {
                        viewModel.onToggleNotificationDot()
                    } else {
                        if (context.isNotificationPermissionGranted()) {
                            viewModel.onToggleNotificationDot()
                        } else {
                            showEnableNotificationPermissionDialog = true
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        if (showEnableAccessibilityDialog) {
            EnableAccessibilityDialog(
                onConfirm = {
                    viewModel.onToggleDoubleTapToLock()
                    context.requestLockScreenPermission()
                    showEnableAccessibilityDialog = false
                },
                onDismiss = {
                    showEnableAccessibilityDialog = false
                }
            )
        }

        if (showEnableNotificationPermissionDialog) {
            EnableNotificationsDialog(
                onConfirm = {
                    context.openNotificationSettings()
                    showEnableNotificationPermissionDialog = false
                    viewModel.onToggleNotificationDot()
                },
                onDismiss = {
                    showEnableNotificationPermissionDialog = false
                }
            )
        }
    }
}

@Composable
private fun ThemeDropdown(
    selectedOption: String,
    options: List<Pair<ThemeMode, String>>,
    onOptionSelected: (ThemeMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = Dimens.APP_HORIZONTAL_SPACING,
                vertical = 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.theme),
            modifier = Modifier.weight(1f),
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.width(16.dp))
        DropdownView(
            selectedOption = selectedOption,
            options = options.map { it.second },
            onOptionSelected = { selected ->
                onOptionSelected(options.first { it.second == selected }.first)
            }
        )
    }
}

@Composable
private fun AppsAlignmentDropdown(
    selectedOption: String,
    options: List<Pair<HomeAppsAlignment, String>>,
    onOptionSelected: (HomeAppsAlignment) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = Dimens.APP_HORIZONTAL_SPACING,
                vertical = 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.home_apps_alignment),
            modifier = Modifier.weight(1f),
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.width(16.dp))
        DropdownView(
            selectedOption = selectedOption,
            options = options.map { it.second },
            onOptionSelected = { selected ->
                onOptionSelected(options.first { it.second == selected }.first)
            }
        )
    }
}

@Composable
private fun ClockAlignmentDropdown(
    selectedOption: String,
    options: List<Pair<HomeClockAlignment, String>>,
    onOptionSelected: (HomeClockAlignment) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = Dimens.APP_HORIZONTAL_SPACING,
                vertical = 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.clock_alignment),
            modifier = Modifier.weight(1f),
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.width(16.dp))
        DropdownView(
            selectedOption = selectedOption,
            options = options.map { it.second },
            onOptionSelected = { selected ->
                onOptionSelected(options.first { it.second == selected }.first)
            }
        )
    }
}

@Composable
private fun ToggleItem(
    title: String,
    subtitle: String? = null,
    isChecked: Boolean,
    onToggleClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onToggleClick)
            .padding(horizontal = Dimens.APP_HORIZONTAL_SPACING, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 20.sp
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = isChecked,
            onCheckedChange = {
                onToggleClick()
            }
        )
    }
}

@Composable
private fun ClockModeDropdown(
    selectedOption: String,
    options: List<Pair<HomeClockMode, String>>,
    onOptionSelected: (HomeClockMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = Dimens.APP_HORIZONTAL_SPACING,
                vertical = 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.clock_mode),
            modifier = Modifier.weight(1f),
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.width(16.dp))
        DropdownView(
            selectedOption = selectedOption,
            options = options.map { it.second },
            onOptionSelected = { selected ->
                onOptionSelected(options.first { it.second == selected }.first)
            }
        )
    }
}

@Composable
private fun EnableAccessibilityDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.enable_accessibility_dialog_title)) },
        text = {
            Text(
                text = stringResource(
                    R.string.enable_accessibility_dialog_message,
                    stringResource(id = R.string.app_name)
                )
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm
            ) {
                Text(text = stringResource(R.string.open_settings))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(text = stringResource(id = R.string.dismiss))
            }
        }
    )
}

@Composable
fun EnableNotificationsDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.notification_access_required)) },
        text = {
            Text(
                stringResource(R.string.notification_access_required_description)
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(
                    stringResource(R.string.open_settings)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    stringResource(R.string.dismiss)
                )
            }
        }
    )
}