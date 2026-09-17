package com.example.stoodascanner.ui.screens

import android.annotation.SuppressLint
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
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
    var classNickname by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf<Int?>(null) }
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
            Color(0xFFE53935), // Red
            Color(0xFFD81B60), // Pink
            Color(0xFF8E24AA), // Purple
            Color(0xFF5E35B1), // Deep Purple
            Color(0xFF3949AB), // Indigo
            Color(0xFF1E88E5), // Blue
            Color(0xFF039BE5), // Light Blue
            Color(0xFF00ACC1), // Cyan
            Color(0xFF00897B), // Teal
            Color(0xFF43A047), // Green
            Color(0xFF7CB342), // Light Green
            Color(0xFFC0CA33), // Lime
            Color(0xFFFDD835), // Yellow
            Color(0xFFFFB300), // Amber
            Color(0xFFFB8C00), // Orange
            Color(0xFFF4511E), // Deep Orange
            Color(0xFF6D4C41), // Brown
            Color(0xFF757575), // Grey
            Color(0xFF546E7A)  // Blue Grey
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
                    val newClass = StudentClass(classTitle, classNickname, selectedColor, manualNames.toList())
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
