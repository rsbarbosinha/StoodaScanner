package com.example.stoodascanner.ui.screens

import android.annotation.SuppressLint
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stoodascanner.data.AppState
import com.example.stoodascanner.viewModel.MainViewModel
import com.example.stoodascanner.R
import com.example.stoodascanner.data.StudentClass
import com.example.stoodascanner.utils.StudentImportParser
import com.example.stoodascanner.viewModel.CreationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("DefaultLocale")
@Composable
fun ClassCreationScreen(
    viewModel: MainViewModel,
    onGeneratePdf: (List<String>) -> Unit
) {
    var classTitle by remember { mutableStateOf("") }
    var nameColumnIndex by remember { mutableStateOf("1") }
    val manualNames = remember { mutableStateListOf<String>() }
    var newName by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val colIdx = (nameColumnIndex.toIntOrNull() ?: 1) - 1
            scope.launch {
                val names = withContext(Dispatchers.IO) {
                    StudentImportParser.parseFileToNames(it, context, colIdx)
                }
                manualNames.clear()
                manualNames.addAll(names)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val titleText = if (viewModel.creationType == CreationType.IMPORT) {
            stringResource(R.string.import_via_spreadsheet)
        } else {
            stringResource(R.string.custom_creation)
        }
        
        Text(text = titleText, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = classTitle,
            onValueChange = { classTitle = it },
            label = { Text(stringResource(R.string.class_title_hint)) },
            modifier = Modifier.fillMaxWidth()
        )

        if (viewModel.creationType == CreationType.IMPORT) {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = nameColumnIndex,
                onValueChange = { nameColumnIndex = it },
                label = { Text(stringResource(R.string.name_column_index)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { filePickerLauncher.launch("*/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.upload_file_csv_xlsx))
            }
        } else {
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
                    if (newName.isNotBlank() && manualNames.size < 64) {
                        manualNames.add(newName.trim())
                        newName = ""
                    }
                }) {
                    Text(stringResource(R.string.add))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Text("${stringResource(R.string.students)} (${manualNames.size}/64):", fontWeight = FontWeight.Bold)
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            items(manualNames) { name ->
                val index = manualNames.indexOf(name)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${(index + 1).toString().padStart(2, '0')}: $name", modifier = Modifier.weight(1f))
                    Button(onClick = { manualNames.remove(name) }) {
                        Text("X")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                if (classTitle.isBlank()) {
                    Toast.makeText(context, context.getString(R.string.error_enter_title), Toast.LENGTH_SHORT).show()
                } else if (manualNames.isEmpty()) {
                    Toast.makeText(context, context.getString(R.string.error_add_student), Toast.LENGTH_SHORT).show()
                } else {
                    val newClass = StudentClass(classTitle, manualNames.toList())
                    viewModel.classManager.saveClass(newClass)
                    
                    onGeneratePdf(manualNames.toList())
                    viewModel.navigateTo(AppState.CLASS_SELECTION)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.save_and_generate_qr_pdf))
        }
    }
}
