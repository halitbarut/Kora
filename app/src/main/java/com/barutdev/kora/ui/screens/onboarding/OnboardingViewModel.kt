package com.barutdev.kora.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barutdev.kora.domain.usecase.SetOnboardingCompletedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val setOnboardingCompleted: SetOnboardingCompletedUseCase
) : ViewModel() {

    private val _consentChecked = MutableStateFlow(false)
    val consentChecked: StateFlow<Boolean> = _consentChecked.asStateFlow()

    fun onConsentCheckedChange(checked: Boolean) {
        _consentChecked.value = checked
    }

    fun completeOnboarding(onCompleted: () -> Unit) {
        viewModelScope.launch {
            setOnboardingCompleted(true)
            onCompleted()
        }
    }
}
