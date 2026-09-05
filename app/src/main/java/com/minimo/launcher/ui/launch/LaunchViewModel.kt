package com.minimo.launcher.ui.launch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimo.launcher.data.PreferenceHelper
import com.minimo.launcher.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LaunchViewModel @Inject constructor(
    private val preferenceHelper: PreferenceHelper
) : ViewModel() {
    private val _navigateToRoute = Channel<String>(Channel.BUFFERED)
    val navigateToRoute: Flow<String> = _navigateToRoute.receiveAsFlow()

    init {
        viewModelScope.launch {
            preferenceHelper.getIsIntroCompletedFlow()
                .distinctUntilChanged()
                .collect { isIntroCompleted ->
                if (isIntroCompleted) {
                    _navigateToRoute.send(Routes.HOME)
                } else {
                    _navigateToRoute.send(Routes.INTRO)
                }
            }
        }
    }
}
