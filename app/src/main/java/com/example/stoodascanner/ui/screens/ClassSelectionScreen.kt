package com.example.stoodascanner.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.stoodascanner.data.AppState
import com.example.stoodascanner.viewModel.MainViewModel
import com.example.stoodascanner.R
import com.example.stoodascanner.data.StudentClass
import com.example.stoodascanner.ui.Mocks
import com.example.stoodascanner.ui.theme.StoodaScannerTheme

@Preview(showBackground = true)
@Composable
fun ClassSelectionScreenPreview() {
    StoodaScannerTheme {
        ClassSelectionScreenContent(
            classes = Mocks.mockClasses,
            isDebugMode = false,
            onDebugToggle = {},
            onClassClick = {},
            onClassLongClick = {},
            onAddClassClick = {}
        )
    }
}

@Composable
fun ClassSelectionScreen(
    viewModel: MainViewModel,
    onStartScan: () -> Unit
) {
    val classes = viewModel.classManager.getAllClasses()
    
    ClassSelectionScreenContent(
        classes = classes,
        isDebugMode = viewModel.isDebugMode,
        onDebugToggle = { viewModel.isDebugMode = !viewModel.isDebugMode },
        onClassClick = { 
            viewModel.selectClass(it)
            onStartScan()
        },
        onClassLongClick = {
            viewModel.editingClass = it
            viewModel.navigateTo(AppState.CLASS_MANAGEMENT)
        },
        onAddClassClick = { viewModel.navigateTo(AppState.CLASS_CREATION_CHOICE) }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClassSelectionScreenContent(
    classes: List<StudentClass>,
    isDebugMode: Boolean,
    onDebugToggle: () -> Unit,
    onClassClick: (StudentClass) -> Unit,
    onClassLongClick: (StudentClass) -> Unit,
    onAddClassClick: () -> Unit
) {
    var clickCount by remember { mutableIntStateOf(0) }
    var lastClickTime by remember { mutableLongStateOf(0L) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.select_class_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 32.dp)
                .clickable {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastClickTime < 500) {
                        clickCount++
                    } else {
                        clickCount = 1
                    }
                    lastClickTime = currentTime
                    if (clickCount >= 5) {
                        onDebugToggle()
                        clickCount = 0
                    }
                }
        )

        if (isDebugMode) {
            Text(
                text = stringResource(R.string.debug_mode_on),
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(classes) { studentClass ->
                ClassItem(
                    studentClass = studentClass,
                    onClick = { onClassClick(studentClass) },
                    onLongClick = { onClassLongClick(studentClass) }
                )
            }
            item {
                AddClassItem(
                    onClick = onAddClassClick
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClassItem(
    studentClass: StudentClass,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val title = studentClass.title ?: ""
    val nickname = (studentClass.nickname ?: "").ifEmpty { 
        if (title.length >= 3) {
            title.substring(0, 3)
        } else if (title.length >= 2) {
            title.substring(0, 2)
        } else {
            title
        }
    }.uppercase()

    val itemColor = if (studentClass.color != null) Color(studentClass.color) else MaterialTheme.colorScheme.primaryContainer
    val contentColor = if (studentClass.color != null) Color.White else MaterialTheme.colorScheme.onPrimaryContainer

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(100.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(itemColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = nickname,
                fontSize = if (nickname.length > 2) 24.sp else 32.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun AddClassItem(onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(100.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.new_class_creation),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
