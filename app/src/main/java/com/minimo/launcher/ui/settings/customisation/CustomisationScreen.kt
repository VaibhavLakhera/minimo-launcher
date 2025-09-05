package com.minimo.launcher.ui.settings.customisation

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.minimo.launcher.R
import com.minimo.launcher.ui.settings.customisation.components.AppSizeSlider
import com.minimo.launcher.ui.settings.customisation.components.AppsAlignmentDropdown
import com.minimo.launcher.ui.settings.customisation.components.ClockAlignmentDropdown
import com.minimo.launcher.ui.settings.customisation.components.ClockModeDropdown
import com.minimo.launcher.ui.settings.customisation.components.EnableAccessibilityDialog
import com.minimo.launcher.ui.settings.customisation.components.EnableNotificationsDialog
import com.minimo.launcher.ui.settings.customisation.components.IgnoreSpecialCharacters
import com.minimo.launcher.ui.settings.customisation.components.ThemeDropdown
import com.minimo.launcher.ui.settings.customisation.components.ToggleItem
import com.minimo.launcher.ui.theme.ThemeMode
import com.minimo.launcher.utils.AndroidUtils
import com.minimo.launcher.utils.HomeAppsAlignment
import com.minimo.launcher.utils.HomeClockAlignment
import com.minimo.launcher.utils.HomeClockMode
import com.minimo.launcher.utils.StringUtils
import com.minimo.launcher.utils.hasLockScreenPermission
import com.minimo.launcher.utils.isNotificationPermissionGranted
import com.minimo.launcher.utils.openNotificationSettings
import com.minimo.launcher.utils.removeLockScreenPermission
import com.minimo.launcher.utils.requestLockScreenPermission

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
                onOptionSelected = viewModel::onThemeModeChanged
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

            AppSizeSlider(
                homeTextSize = state.homeTextSize,
                onHomeTextSizeChanged = viewModel::onHomeTextSizeChanged,
                homeAppVerticalPadding = state.homeAppVerticalPadding,
                onHomeVerticalPaddingChanged = viewModel::onHomeVerticalPaddingChanged
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
                onOptionSelected = viewModel::onHomeAppsAlignmentChanged
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
                    onOptionSelected = viewModel::onHomeClockAlignmentChanged
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
                    onOptionSelected = viewModel::onHomeClockModeChanged
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

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            IgnoreSpecialCharacters(
                currentCharacters = state.ignoreSpecialCharacters,
                onUpdateCharacters = viewModel::onUpdateIgnoreSpecialCharacters
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