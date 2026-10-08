package com.example.stoodascanner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stoodascanner.R
import com.example.stoodascanner.viewModel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualAttendanceScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val activeClass = viewModel.activeSessionClass ?: return
    val students = activeClass.students ?: emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.review_edit_attendance)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${viewModel.presentStudentIndices.size} / ${students.size} Present",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Button(onClick = onBack) {
                    Text(stringResource(R.string.done))
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(students) { index, studentName ->
                    val isPresent = viewModel.presentStudentIndices.contains(index)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${index + 1}. $studentName",
                            fontSize = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = isPresent,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    viewModel.presentStudentIndices.add(index)
                                } else {
                                    viewModel.presentStudentIndices.remove(index)
                                }
                                viewModel.attendanceTaken = true
                            }
                        )
                    }
                    Divider()
                }
            }
        }
    }
}
