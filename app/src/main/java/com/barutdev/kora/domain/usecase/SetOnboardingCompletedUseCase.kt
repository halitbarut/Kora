package com.barutdev.kora.domain.usecase

import com.barutdev.kora.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class SetOnboardingCompletedUseCase @Inject constructor(
    private val repo: UserPreferencesRepository
) {
    suspend operator fun invoke(completed: Boolean = true) {
        repo.setOnboardingCompleted(completed)
    }
}
