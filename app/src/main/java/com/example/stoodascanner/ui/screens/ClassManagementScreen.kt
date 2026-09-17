package com.example.stoodascanner.ui.screens

import android.annotation.SuppressLint
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stoodascanner.data.AppState
import com.example.stoodascanner.viewModel.MainViewModel
import com.example.stoodascanner.R
import com.example.stoodascanner.data.StudentClass

@Composable
fun ClassManagementScreen(
    viewModel: MainViewModel,
    onGeneratePdf: (List<String>) -> Unit
) {
    var classes by remember { mutableStateOf(viewModel.classManager.getAllClasses()) }
    val context = LocalContext.current

    if (viewModel.editingClass != null) {
        EditClassSubScreen(
            initialClass = viewModel.editingClass!!,
            onSave = { updatedClass ->
                viewModel.classManager.deleteClass(viewModel.editingClass?.title ?: "")
                viewModel.classManager.saveClass(updatedClass)
                
                // If we are currently editing the active session class, update it in the view model session state
                if (viewModel.activeSessionClass?.title == viewModel.editingClass?.title) {
                    viewModel.activeSessionClass = updatedClass
                    viewModel.selectedClass = updatedClass
                }
                
                classes = viewModel.classManager.getAllClasses()
                viewModel.editingClass = null
                
                // Check if active session is ongoing. If so, return straight to the session options screen
                if (viewModel.activeSessionClass != null) {
                    viewModel.navigateTo(AppState.SESSION_OPTIONS)
                } else {
                    viewModel.navigateTo(AppState.CLASS_SELECTION)
                }
            },
            onCancel = { 
                viewModel.editingClass = null
                if (viewModel.activeSessionClass != null) {
                    viewModel.navigateTo(AppState.SESSION_OPTIONS)
                } else {
                    viewModel.navigateTo(AppState.CLASS_SELECTION)
                }
            },
            onGeneratePdf = onGeneratePdf
        )
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = stringResource(R.string.manage_classes), fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            if (classes.isEmpty()) {
                Text(stringResource(R.string.no_classes_found))
            } else {
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    items(classes) { cls ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val title = cls.title ?: ""
                            Text(title, modifier = Modifier.weight(1f), fontSize = 18.sp)
                            
                            IconButton(onClick = { onGeneratePdf(cls.students ?: emptyList()) }) {
                                Icon(
                                    imageVector = Icons.Filled.PictureAsPdf,
                                    contentDescription = "Generate PDF",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            Button(onClick = { viewModel.editingClass = cls }, modifier = Modifier.padding(horizontal = 4.dp)) {
                                Text(stringResource(R.string.edit))
                            }
                            
                            Button(onClick = {
                                val titleToDelete = cls.title ?: ""
                                AlertDialog.Builder(context)
                                    .setTitle(context.getString(R.string.delete_class_title))
                                    .setMessage(context.getString(R.string.delete_class_message, titleToDelete))
                                    .setPositiveButton(R.string.yes) { _, _ ->
                                        viewModel.classManager.deleteClass(titleToDelete)
                                        classes = viewModel.classManager.getAllClasses()
                                    }
                                    .setNegativeButton(R.string.no, null)
                                    .show()
                            }) {
                                Text(stringResource(R.string.delete))
                            }
                        }
                        Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { viewModel.navigateTo(AppState.CLASS_SELECTION) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.back_to_menu))
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun EditClassSubScreen(
    initialClass: StudentClass,
    onSave: (StudentClass) -> Unit,
    onCancel: () -> Unit,
    onGeneratePdf: (List<String>) -> Unit
) {
    var classTitle by remember { mutableStateOf(initialClass.title ?: "") }
    var classNickname by remember { mutableStateOf(initialClass.nickname ?: "") }
    val studentsList = initialClass.students ?: emptyList()
    val students = remember { mutableStateListOf<String>().apply { addAll(studentsList) } }
    var newName by remember { mutableStateOf("") }

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
            Text(text = "${stringResource(R.string.editing)}: $titleText", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = { onGeneratePdf(students.toList()) }) {
                Icon(
                    imageVector = Icons.Filled.PictureAsPdf,
                    contentDescription = "Generate PDF",
                    tint = MaterialTheme.colorScheme.primary
                )
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
            onValueChange = { if (it.length <= 2) classNickname = it },
            label = { Text(stringResource(R.string.class_nickname_hint)) },
            modifier = Modifier.fillMaxWidth()
        )
        
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
            Button(onClick = { onSave(StudentClass(classTitle, classNickname, students.toList())) }, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.save))
            }
        }
    }
}
