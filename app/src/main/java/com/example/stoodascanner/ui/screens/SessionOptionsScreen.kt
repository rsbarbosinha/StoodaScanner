package com.example.stoodascanner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.stoodascanner.R
import com.example.stoodascanner.data.StudentClass
import com.example.stoodascanner.ui.Mocks
import com.example.stoodascanner.ui.theme.StoodaScannerTheme
import com.example.stoodascanner.viewModel.MainViewModel

@Composable
fun SessionOptionsScreen(
    viewModel: MainViewModel
) {
    val activeClass = viewModel.activeSessionClass ?: return

    SessionOptionsScreenContent(
        activeClass = activeClass,
        onAttendanceClick = { viewModel.startAttendanceCheck() },
        onQuizClick = { viewModel.startQuiz() },
        onFinishSessionClick = { viewModel.finishSession() }
    )
}

@Composable
fun SessionOptionsScreenContent(
    activeClass: StudentClass,
    onAttendanceClick: () -> Unit,
    onQuizClick: () -> Unit,
    onFinishSessionClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = activeClass.title ?: "",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = stringResource(R.string.session_options),
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = onAttendanceClick,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(stringResource(R.string.attendance_check), fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onQuizClick,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(stringResource(R.string.do_quiz), fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = onFinishSessionClick,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text(stringResource(R.string.finish_section), fontSize = 18.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SessionOptionsScreenPreview() {
    StoodaScannerTheme {
        SessionOptionsScreenContent(
            activeClass = Mocks.mockStudentClass,
            onAttendanceClick = {},
            onQuizClick = {},
            onFinishSessionClick = {}
        )
    }
}
