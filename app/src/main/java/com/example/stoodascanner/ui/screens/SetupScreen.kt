package com.example.stoodascanner.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stoodascanner.viewModel.MainViewModel
import com.example.stoodascanner.R

@Composable
fun SetupScreen(
    viewModel: MainViewModel,
    onStartScan: () -> Unit
) {
    var input by remember { mutableStateOf("") }
    val context = LocalContext.current
    val classes = remember { viewModel.classManager.getAllClasses() }
    var showClassDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (classes.isNotEmpty()) {
            Text("Select Class (Optional):")
            Button(onClick = { showClassDropdown = !showClassDropdown }) {
                Text(viewModel.selectedClass?.title ?: "No Class Selected")
            }
            if (showClassDropdown) {
                LazyColumn(modifier = Modifier.height(150.dp)) {
                    items(classes) { cls ->
                        Button(onClick = {
                            viewModel.selectedClass = cls
                            showClassDropdown = false
                            input = cls.students.size.toString()
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text(cls.title)
                        }
                    }
                    item {
                        Button(onClick = {
                            viewModel.selectedClass = null
                            showClassDropdown = false
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text("Clear Selection")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(text = stringResource(R.string.how_many_qr_codes_to_scan), fontSize = 20.sp)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = input,
            onValueChange = { if (it.length <= 2) input = it },
            modifier = Modifier.width(120.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = { Text(stringResource(R.string._1_64)) },
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 24.sp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = {
            val count = input.toIntOrNull()
            if (count != null && count in 1..64) {
                viewModel.targetCount = count
                onStartScan()
            } else {
                Toast.makeText(context, "Please enter a number between 1 and 64", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text(stringResource(R.string.start_scan))
        }
    }
}
