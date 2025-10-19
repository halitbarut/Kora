package com.barutdev.kora.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barutdev.kora.domain.usecase.GetOnboardingCompletedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class StartDestinationViewModel @Inject constructor(
    private val getOnboardingCompleted: GetOnboardingCompletedUseCase
) : ViewModel() {

    private val _startRoute = MutableStateFlow<String?>(null)
    val startRoute: StateFlow<String?> = _startRoute.asStateFlow()

    init {
        viewModelScope.launch {
            val completed = getOnboardingCompleted()
            _startRoute.value = if (completed) {
                KoraDestination.StudentList.route
            } else {
                KoraDestination.Onboarding.route
            }
        }
    }
}
