package com.example.stoodascanner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SelectClassScreen(
    viewModel: MainViewModel,
    onStartScan: () -> Unit
) {
    val classes = remember { viewModel.classManager.getAllClasses() }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.select_class),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (classes.isEmpty()) {
            Text(stringResource(R.string.no_classes_found))
        } else {
            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                items(classes) { cls ->
                    Button(
                        onClick = {
                            viewModel.startScanningWithClass(cls)
                            onStartScan()
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text(cls.title)
                    }
                }
            }
        }
    }
}
