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
import com.example.stoodascanner.data.AppState
import com.example.stoodascanner.viewModel.MainViewModel
import com.example.stoodascanner.R
import com.example.stoodascanner.data.StudentClass

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClassSelectionScreen(
    viewModel: MainViewModel,
    onStartScan: () -> Unit
) {
    val classes = viewModel.classManager.getAllClasses()
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
                        viewModel.isDebugMode = !viewModel.isDebugMode
                        clickCount = 0
                    }
                }
        )

        if (viewModel.isDebugMode) {
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
                    onClick = {
                        viewModel.selectClass(studentClass)
                        onStartScan()
                    },
                    onLongClick = {
                        viewModel.editingClass = studentClass
                        viewModel.navigateTo(AppState.CLASS_MANAGEMENT)
                    }
                )
            }
            item {
                AddClassItem(
                    onClick = { viewModel.navigateTo(AppState.CLASS_CREATION) }
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
    val initials = if (studentClass.title.length >= 2) {
        studentClass.title.substring(0, 2).uppercase()
    } else {
        studentClass.title.uppercase()
    }

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
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = studentClass.title,
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
