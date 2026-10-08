package com.example.stoodascanner.ui.screens

import android.annotation.SuppressLint
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stoodascanner.data.AppState
import com.example.stoodascanner.viewModel.MainViewModel
import com.example.stoodascanner.R
import com.example.stoodascanner.data.StudentClass
import com.example.stoodascanner.ui.Mocks
import com.example.stoodascanner.ui.theme.StoodaScannerTheme

@SuppressLint("DefaultLocale")
@Composable
fun ClassEditingScreen(
    viewModel: MainViewModel,
    onGeneratePdf: (List<String>) -> Unit
) {
    val initialClass = viewModel.editingClass

    if (initialClass == null) {
        // Only trigger a fallback if the active session is ALSO missing (meaning we fully exited everything)
        // If we still have an active session, let the router handle navigating there organically.
        if (viewModel.appState == AppState.CLASS_EDITING) {
             LaunchedEffect(Unit) {
                 if (viewModel.activeSessionClass != null) {
                     viewModel.navigateTo(AppState.SESSION_OPTIONS)
                 } else {
                     viewModel.navigateTo(AppState.CLASS_SELECTION)
                 }
             }
        }
        return
    }

    ClassEditingScreenContent(
        initialClass = initialClass,
        onSave = { updatedClass ->
            viewModel.classManager.deleteClass(initialClass.title ?: "")
            viewModel.classManager.saveClass(updatedClass)
            
            if (viewModel.activeSessionClass?.title == initialClass.title) {
                viewModel.activeSessionClass = updatedClass
                viewModel.selectedClass = updatedClass
            }
            
            // Always set the app state to the next screen first
            if (viewModel.activeSessionClass != null) {
                viewModel.navigateTo(AppState.SESSION_OPTIONS)
            } else {
                viewModel.navigateTo(AppState.CLASS_SELECTION)
            }
            
            // Then clear editingClass state
            viewModel.editingClass = null
        },
        onCancel = {
            if (viewModel.activeSessionClass != null) {
                viewModel.navigateTo(AppState.SESSION_OPTIONS)
            } else {
                viewModel.navigateTo(AppState.CLASS_SELECTION)
            }
            viewModel.editingClass = null
        },
        onDelete = {
            val titleToDelete = initialClass.title ?: ""
            viewModel.classManager.deleteClass(titleToDelete)
            viewModel.editingClass = null
            if (viewModel.activeSessionClass?.title == titleToDelete) {
                viewModel.finishSession()
            } else {
                viewModel.navigateTo(AppState.CLASS_SELECTION)
            }
        },
        onGeneratePdf = onGeneratePdf
    )
}

@SuppressLint("DefaultLocale")
@Composable
fun ClassEditingScreenContent(
    initialClass: StudentClass,
    onSave: (StudentClass) -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    onGeneratePdf: (List<String>) -> Unit
) {
    val context = LocalContext.current
    var classTitle by remember { mutableStateOf(initialClass.title ?: "") }
    var classNickname by remember { mutableStateOf(initialClass.nickname ?: "") }
    var selectedColor by remember { mutableStateOf(initialClass.color) }
    val studentsList = initialClass.students ?: emptyList()
    val students = remember { mutableStateListOf<String>().apply { addAll(studentsList) } }
    var newName by remember { mutableStateOf("") }

    val handleDelete: () -> Unit = {
        val titleToDelete = initialClass.title ?: ""
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.delete_class_title))
            .setMessage(context.getString(R.string.delete_class_message, titleToDelete))
            .setPositiveButton(R.string.yes) { _, _ ->
                onDelete()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val titleText = initialClass.title ?: ""
            Text(text = "${stringResource(R.string.editing)}: $titleText", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Row(modifier = Modifier.padding(end = 80.dp)) {
                IconButton(onClick = handleDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                IconButton(onClick = { onGeneratePdf(students.toList()) }) {
                    Icon(
                        imageVector = Icons.Filled.PictureAsPdf,
                        contentDescription = "Generate PDF",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        OutlinedTextField(
            value = classTitle,
            onValueChange = { classTitle = it },
            label = { Text(stringResource(R.string.class_title_hint)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = classNickname,
            onValueChange = { if (it.length <= 3) classNickname = it },
            label = { Text(stringResource(R.string.class_nickname_hint)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Choose Team Color:", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        val colors = listOf(
            Color(0xFFFFADAD), // Pastel Red
            Color(0xFFFFD6A5), // Pastel Orange
            Color(0xFFFDFFB6), // Pastel Yellow
            Color(0xFFCAFFBF), // Pastel Green
            Color(0xFF9BF6FF), // Pastel Cyan
            Color(0xFFA0C4FF), // Pastel Blue
            Color(0xFFBDB2FF), // Pastel Purple
            Color(0xFFFFC6FF), // Pastel Pink
            Color(0xFFF0E6EF), // Pastel Lilac
            Color(0xFFE5E5E5), // Pastel Greyish
            Color(0xFFFFD1CC), // Pastel Peach
            Color(0xFFC1FBA4)  // Pastel Lime
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(colors) { color ->
                val colorInt = color.toArgb()
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (selectedColor == colorInt) 3.dp else 1.dp,
                            color = if (selectedColor == colorInt) MaterialTheme.colorScheme.primary else Color.LightGray,
                            shape = CircleShape
                        )
                        .clickable { selectedColor = colorInt }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text(stringResource(R.string.manual_name_entry)) },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                if (newName.isNotBlank() && students.size < 64) {
                    students.add(newName.trim())
                    newName = ""
                }
            }) {
                Text(stringResource(R.string.add))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Text("${stringResource(R.string.students)} (${students.size}/64):", fontWeight = FontWeight.Bold)
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            items(students) { name ->
                val index = students.indexOf(name)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${String.format("%02d", index + 1)}: $name", modifier = Modifier.weight(1f))
                    Button(onClick = { students.remove(name) }) {
                        Text("X")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.cancel))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                onSave(StudentClass(classTitle, classNickname, selectedColor, students.toList()))
            }, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.save))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ClassEditingScreenPreview() {
    StoodaScannerTheme {
        ClassEditingScreenContent(
            initialClass = Mocks.mockStudentClass,
            onSave = {},
            onCancel = {},
            onDelete = {},
            onGeneratePdf = {}
        )
    }
}
