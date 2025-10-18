package com.barutdev.kora.domain.usecase

import com.barutdev.kora.domain.repository.UserPreferencesRepository
import com.barutdev.kora.util.SmartLocaleDefaults
import javax.inject.Inject

class InitializeSmartDefaultsUseCase @Inject constructor(
    private val repo: UserPreferencesRepository
) {
    suspend operator fun invoke() {
        if (repo.isFirstRunCompleted()) return

        val hasLanguage = repo.getSavedLanguageOrNull() != null
        val hasCurrency = repo.getSavedCurrencyOrNull() != null
        if (hasLanguage || hasCurrency) {
            repo.setFirstRunCompleted()
            return
        }

        val defaults = SmartLocaleDefaults.resolveSmartDefaultsFromDevice()
        repo.updateLanguage(defaults.languageCode)
        repo.updateCurrency(defaults.currencyCode)
        repo.setFirstRunCompleted()
    }
}
