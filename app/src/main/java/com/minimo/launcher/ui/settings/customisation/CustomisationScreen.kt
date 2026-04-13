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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.minimo.launcher.R
import com.minimo.launcher.ui.settings.app_picker.AppPickerDialog
import com.minimo.launcher.ui.settings.customisation.components.AppSizeSlider
import com.minimo.launcher.ui.settings.customisation.components.AppsAlignmentHorizontalDropdown
import com.minimo.launcher.ui.settings.customisation.components.AppsAlignmentVerticalDropdown
import com.minimo.launcher.ui.settings.customisation.components.ClockAlignmentDropdown
import com.minimo.launcher.ui.settings.customisation.components.ClockModeDropdown
import com.minimo.launcher.ui.settings.customisation.components.EnableAccessibilityDialog
import com.minimo.launcher.ui.settings.customisation.components.EnableAppUsageDialog
import com.minimo.launcher.ui.settings.customisation.components.EnableNotificationsDialog
import com.minimo.launcher.ui.settings.customisation.components.EnableSetWallpaperToThemeColorDialog
import com.minimo.launcher.ui.settings.customisation.components.FontDropdown
import com.minimo.launcher.ui.settings.customisation.components.IgnoreSpecialCharacters
import com.minimo.launcher.ui.settings.customisation.components.MinimoSettingsPositionDropdown
import com.minimo.launcher.ui.settings.customisation.components.ThemeDropdown
import com.minimo.launcher.ui.settings.customisation.components.ToggleItem
import com.minimo.launcher.ui.theme.Dimens
import com.minimo.launcher.ui.theme.ThemeMode
import com.minimo.launcher.utils.AndroidUtils
import com.minimo.launcher.utils.HomeAppsAlignmentHorizontal
import com.minimo.launcher.utils.HomeAppsAlignmentVertical
import com.minimo.launcher.utils.HomeClockAlignment
import com.minimo.launcher.utils.HomeClockMode
import com.minimo.launcher.utils.MinimoSettingsPosition
import com.minimo.launcher.utils.StringUtils
import com.minimo.launcher.utils.hasLockScreenPermission
import com.minimo.launcher.utils.isAppUsagePermissionGranted
import com.minimo.launcher.utils.isNotificationPermissionGranted
import com.minimo.launcher.utils.openNotificationSettings
import com.minimo.launcher.utils.openUsageAccessSettings
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
    var showEnableAppUsagePermissionDialog by remember { mutableStateOf(false) }
    var showSetWallpaperToThemeColorDialog by remember { mutableStateOf(false) }

    var showClockAppPicker by remember { mutableStateOf(false) }
    var showCalendarAppPicker by remember { mutableStateOf(false) }
    var showScreenTimeAppPicker by remember { mutableStateOf(false) }

    var showSwipeLeftAppPicker by remember { mutableStateOf(false) }
    var showSwipeRightAppPicker by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            if (!context.hasLockScreenPermission()) {
                viewModel.onLockScreenPermissionNotEnableOnStarted()
            }

            if (!context.isNotificationPermissionGranted()) {
                viewModel.onNotificationPermissionNotGrantedOnStarted()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !context.isAppUsagePermissionGranted()) {
                viewModel.onAppUsagePermissionNotGrantedOnStarted()
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
            FontDropdown(
                selectedFont = state.fontPreference,
                onFontSelected = viewModel::onFontPreferenceChanged
            )
            
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

            ToggleItem(
                title = stringResource(R.string.enable_wallpaper),
                isChecked = state.enableWallpaper,
                onToggleClick = viewModel::onToggleEnableWallpaper
            )

            if (state.enableWallpaper) {
                ToggleItem(
                    title = stringResource(R.string.light_text_on_wallpaper),
                    isChecked = state.lightTextOnWallpaper,
                    onToggleClick = viewModel::onToggleLightTextOnWallpaper
                )

                ToggleItem(
                    title = stringResource(R.string.dim_wallpaper),
                    isChecked = state.dimWallpaper,
                    onToggleClick = viewModel::onToggleDimWallpaper
                )
            }

            ToggleItem(
                title = stringResource(R.string.set_wallpaper_to_theme_color),
                isChecked = state.setWallpaperToThemeColor,
                onToggleClick = {
                    if (state.setWallpaperToThemeColor) {
                        viewModel.onToggleSetWallpaperToThemeColor()
                    } else {
                        showSetWallpaperToThemeColorDialog = true
                    }
                }
            )

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

            AppsAlignmentHorizontalDropdown(
                selectedOption = StringUtils.homeAppsAlignmentHorizontalText(
                    context = context,
                    alignment = state.homeAppsAlignmentHorizontal
                ),
                options = listOf(
                    HomeAppsAlignmentHorizontal.Start to StringUtils.homeAppsAlignmentHorizontalText(
                        context,
                        HomeAppsAlignmentHorizontal.Start
                    ),
                    HomeAppsAlignmentHorizontal.Center to StringUtils.homeAppsAlignmentHorizontalText(
                        context,
                        HomeAppsAlignmentHorizontal.Center
                    ),
                    HomeAppsAlignmentHorizontal.End to StringUtils.homeAppsAlignmentHorizontalText(
                        context,
                        HomeAppsAlignmentHorizontal.End
                    ),
                ),
                onOptionSelected = viewModel::onHomeAppsAlignmentHorizontalChanged
            )

            AppsAlignmentVerticalDropdown(
                selectedOption = StringUtils.homeAppsAlignmentVerticalText(
                    context = context,
                    alignment = state.homeAppsAlignmentVertical
                ),
                options = listOf(
                    HomeAppsAlignmentVertical.Top to StringUtils.homeAppsAlignmentVerticalText(
                        context,
                        HomeAppsAlignmentVertical.Top
                    ),
                    HomeAppsAlignmentVertical.Center to StringUtils.homeAppsAlignmentVerticalText(
                        context,
                        HomeAppsAlignmentVertical.Center
                    ),
                    HomeAppsAlignmentVertical.Bottom to StringUtils.homeAppsAlignmentVerticalText(
                        context,
                        HomeAppsAlignmentVertical.Bottom
                    ),
                ),
                onOptionSelected = viewModel::onHomeAppsAlignmentVerticalChanged
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

                Spacer(modifier = Modifier.height(4.dp))

                AppSelectionItem(
                    title = stringResource(R.string.clock_app),
                    selectedAppName = state.clockAppName,
                    onDefaultClick = { viewModel.onClockAppChanged("") },
                    onChooseClick = { showClockAppPicker = true }
                )

                Spacer(modifier = Modifier.height(4.dp))

                AppSelectionItem(
                    title = stringResource(R.string.calendar_app),
                    selectedAppName = state.calendarAppName,
                    onDefaultClick = { viewModel.onCalendarAppChanged("") },
                    onChooseClick = { showCalendarAppPicker = true }
                )
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

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            ToggleItem(
                title = stringResource(R.string.hide_app_drawer_search),
                subtitle = stringResource(R.string.hide_app_drawer_search_description),
                isChecked = state.hideAppDrawerSearch,
                onToggleClick = viewModel::onToggleHideAppDrawerSearch
            )

            if (state.hideAppDrawerSearch) {
                MinimoSettingsPositionDropdown(
                    selectedOption = StringUtils.minimoSettingsPositionText(
                        context = context,
                        position = state.minimoSettingsPosition
                    ),
                    options = listOf(
                        MinimoSettingsPosition.Auto to StringUtils.minimoSettingsPositionText(
                            context,
                            MinimoSettingsPosition.Auto
                        ),
                        MinimoSettingsPosition.Top to StringUtils.minimoSettingsPositionText(
                            context,
                            MinimoSettingsPosition.Top
                        ),
                        MinimoSettingsPosition.Bottom to StringUtils.minimoSettingsPositionText(
                            context,
                            MinimoSettingsPosition.Bottom
                        )
                    ),
                    onOptionSelected = viewModel::onMinimoSettingsPositionChanged
                )
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                ToggleItem(
                    title = stringResource(R.string.show_screen_time),
                    subtitle = stringResource(R.string.show_screen_time_description),
                    isChecked = state.showScreenTimeWidget,
                    onToggleClick = {
                        if (state.showScreenTimeWidget) {
                            viewModel.onToggleShowScreenTimeWidget()
                        } else {
                            if (context.isAppUsagePermissionGranted()) {
                                viewModel.onToggleShowScreenTimeWidget()
                            } else {
                                showEnableAppUsagePermissionDialog = true
                            }
                        }
                    }
                )

                if (state.showScreenTimeWidget) {
                    Spacer(modifier = Modifier.height(4.dp))

                    AppSelectionItem(
                        title = stringResource(R.string.screen_time_app),
                        selectedAppName = state.screenTimeAppName,
                        onDefaultClick = { viewModel.onScreenTimeAppChanged("") },
                        onChooseClick = { showScreenTimeAppPicker = true }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            AppSelectionItem(
                title = stringResource(R.string.home_swipe_left_app),
                selectedAppName = state.swipeLeftAppName,
                defaultText = stringResource(R.string.none),
                onDefaultClick = { viewModel.onSwipeLeftAppChanged("") },
                onChooseClick = { showSwipeLeftAppPicker = true }
            )

            Spacer(modifier = Modifier.height(4.dp))

            AppSelectionItem(
                title = stringResource(R.string.home_swipe_right_app),
                selectedAppName = state.swipeRightAppName,
                defaultText = stringResource(R.string.none),
                onDefaultClick = { viewModel.onSwipeRightAppChanged("") },
                onChooseClick = { showSwipeRightAppPicker = true }
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

        if (showEnableAppUsagePermissionDialog) {
            EnableAppUsageDialog(
                onConfirm = {
                    context.openUsageAccessSettings()
                    showEnableAppUsagePermissionDialog = false
                },
                onDismiss = {
                    showEnableAppUsagePermissionDialog = false
                }
            )
        }

        if (showSetWallpaperToThemeColorDialog) {
            EnableSetWallpaperToThemeColorDialog(
                onConfirm = {
                    viewModel.onToggleSetWallpaperToThemeColor()
                    showSetWallpaperToThemeColorDialog = false
                },
                onDismiss = {
                    showSetWallpaperToThemeColorDialog = false
                }
            )
        }

        if (showClockAppPicker) {
            AppPickerDialog(
                onDismissRequest = { showClockAppPicker = false },
                onAppSelected = { appInfo ->
                    viewModel.onClockAppChanged("${appInfo.packageName}|${appInfo.className}|${appInfo.userHandle}")
                    showClockAppPicker = false
                }
            )
        }

        if (showCalendarAppPicker) {
            AppPickerDialog(
                onDismissRequest = { showCalendarAppPicker = false },
                onAppSelected = { appInfo ->
                    viewModel.onCalendarAppChanged("${appInfo.packageName}|${appInfo.className}|${appInfo.userHandle}")
                    showCalendarAppPicker = false
                }
            )
        }

        if (showScreenTimeAppPicker) {
            AppPickerDialog(
                onDismissRequest = { showScreenTimeAppPicker = false },
                onAppSelected = { appInfo ->
                    viewModel.onScreenTimeAppChanged("${appInfo.packageName}|${appInfo.className}|${appInfo.userHandle}")
                    showScreenTimeAppPicker = false
                }
            )
        }

        if (showSwipeLeftAppPicker) {
            AppPickerDialog(
                onDismissRequest = { showSwipeLeftAppPicker = false },
                onAppSelected = { appInfo ->
                    viewModel.onSwipeLeftAppChanged("${appInfo.packageName}|${appInfo.className}|${appInfo.userHandle}")
                    showSwipeLeftAppPicker = false
                }
            )
        }

        if (showSwipeRightAppPicker) {
            AppPickerDialog(
                onDismissRequest = { showSwipeRightAppPicker = false },
                onAppSelected = { appInfo ->
                    viewModel.onSwipeRightAppChanged("${appInfo.packageName}|${appInfo.className}|${appInfo.userHandle}")
                    showSwipeRightAppPicker = false
                }
            )
        }
    }
}

@Composable
fun AppSelectionItem(
    title: String,
    selectedAppName: String,
    defaultText: String = stringResource(R.string.default_app),
    onDefaultClick: () -> Unit,
    onChooseClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showMenu = true }
            .padding(
                horizontal = Dimens.APP_HORIZONTAL_SPACING,
                vertical = 16.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(0.65f),
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.width(16.dp))
        Box(
            modifier = Modifier.weight(0.35f),
            contentAlignment = Alignment.CenterEnd
        ) {
            Box {
                Text(
                    text = selectedAppName.ifEmpty { defaultText },
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End
                )
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(defaultText) },
                        onClick = {
                            showMenu = false
                            onDefaultClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.choose)) },
                        onClick = {
                            showMenu = false
                            onChooseClick()
                        }
                    )
                }
            }
        }
    }
}