package com.minimo.launcher.ui.intro

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minimo.launcher.R
import com.minimo.launcher.ui.components.ToggleAppItem
import com.minimo.launcher.ui.home.components.SearchItem
import com.minimo.launcher.utils.Constants.INTRO_MINIMUM_FAVOURITE_COUNT

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun IntroPage2(
    viewModel: IntroViewModel,
    onContinueClick: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = {
                Text(stringResource(id = R.string.favourite_apps))
            },
            windowInsets = WindowInsets(0, 0, 0, 0)
        )
        SearchItem(
            modifier = Modifier.fillMaxWidth(),
            searchText = state.searchText,
            onSearchTextChange = viewModel::onSearchTextChange
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 20.dp)
        ) {
            items(items = state.filteredAllApps, key = { it.id }) { appInfo ->
                ToggleAppItem(
                    modifier = Modifier.animateItem(),
                    appName = appInfo.name,
                    isChecked = appInfo.isFavourite,
                    isWorkProfile = appInfo.isWorkProfile,
                    onToggleClick = { viewModel.onToggleFavouriteAppClick(appInfo) }
                )
            }
        }
        IntroBottomButton(
            text = if (state.minimumFavouriteAdded) {
                stringResource(R.string.btn_continue)
            } else {
                stringResource(R.string.add_at_least_to_continue, INTRO_MINIMUM_FAVOURITE_COUNT)
            },
            onClick = {
                if (state.minimumFavouriteAdded) {
                    onContinueClick()
                }
            }
        )
    }
}