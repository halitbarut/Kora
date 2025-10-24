package com.barutdev.kora.ui.screens.student_profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.barutdev.kora.R
import com.barutdev.kora.util.LocalMessageNotifier
import com.barutdev.kora.util.koraStringResource

@Composable
fun AddStudentProfileScreen(
    onBack: () -> Unit,
    onProfileSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddStudentProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val messageNotifier = LocalMessageNotifier.current
    val successMessage = koraStringResource(id = R.string.add_student_profile_save_success)
    val errorMessage = koraStringResource(id = R.string.student_profile_save_error)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                StudentProfileEvent.ProfileSaved -> {
                    messageNotifier.showMessage(successMessage)
                    onProfileSaved()
                }

                StudentProfileEvent.SaveFailed -> {
                    messageNotifier.showMessage(errorMessage)
                }

                StudentProfileEvent.StudentMissing -> Unit
            }
        }
    }

    StudentProfileContent(
        title = koraStringResource(id = R.string.add_student_profile_title),
        state = uiState,
        onBack = onBack,
        onFullNameChanged = viewModel::onFullNameChanged,
        onParentNameChanged = viewModel::onParentNameChanged,
        onParentContactChanged = viewModel::onParentContactChanged,
        onHourlyRateChanged = viewModel::onHourlyRateChanged,
        onNotesChanged = viewModel::onNotesChanged,
        onSave = viewModel::onSave,
        modifier = modifier
    )
}
