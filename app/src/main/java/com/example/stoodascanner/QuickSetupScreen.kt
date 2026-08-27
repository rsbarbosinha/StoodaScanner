package com.example.stoodascanner

import android.widget.Toast
import androidx.compose.foundation.layout.*
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

@Composable
fun QuickSetupScreen(
    viewModel: MainViewModel,
    onStartScan: () -> Unit
) {
    var input by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(R.string.enter_count), fontSize = 20.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = input,
            onValueChange = { if (it.length <= 2) input = it },
            modifier = Modifier.width(120.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = { Text("1-64") },
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 24.sp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = {
            val count = input.toIntOrNull()
            if (count != null && count in 1..64) {
                viewModel.startScanning(count)
                onStartScan()
            } else {
                Toast.makeText(context, "Please enter a number between 1 and 64", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text(stringResource(R.string.start_scan))
        }
    }
}
