package com.example.stoodascanner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ResultsScreen(
    scannedCodes: List<String>,
    selectedClass: StudentClass?,
    onShowGraph: () -> Unit,
    onRestart: () -> Unit
) {
    val decoder = remember { QRDecoder(selectedClass) }
    val decodedList = remember(scannedCodes.toList()) { 
        scannedCodes.mapIndexed { index, code -> decoder.decode(code, index) } 
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = stringResource(R.string.scan_results),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(decodedList) { item ->
                Text(
                    text = item,
                    modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onShowGraph, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.show_visual_graph))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRestart, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.restart_app))
        }
    }
}
