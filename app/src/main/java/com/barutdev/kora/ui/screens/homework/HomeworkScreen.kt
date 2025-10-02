package com.barutdev.kora.ui.screens.homework

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.barutdev.kora.R

@Composable
fun HomeworkScreen(
    studentId: Int,
    modifier: Modifier = Modifier
) {
    val unusedStudentId = studentId
    // TODO: Replace placeholder content with real homework UI using studentId when available.
    Scaffold(modifier = modifier) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(text = stringResource(id = R.string.homework_title))
        }
    }
}
