package com.example.stoodascanner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.stoodascanner.scanner.QRDecoder
import com.example.stoodascanner.R
import com.example.stoodascanner.data.StudentClass
import com.example.stoodascanner.ui.components.ResultGraphView

@Composable
fun GraphScreen(
    scannedCodes: List<String>,
    selectedClass: StudentClass?,
    onRestart: () -> Unit
) {
    val decoder = remember { QRDecoder(selectedClass) }
    val counts = remember(scannedCodes.toList()) {
        val map = mutableMapOf<String, Int>()
        scannedCodes.forEachIndexed { index, code ->
            if (code.isNotEmpty()) {
                val decoded = decoder.decode(code, index)
                val type = decoded.split(" - ").lastOrNull() ?: "?"
                map[type] = (map[type] ?: 0) + 1
            }
        }
        map.toSortedMap()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = stringResource(R.string.results_distribution),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        val onBackground = MaterialTheme.colorScheme.onBackground.toArgb()
        val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
        val outline = MaterialTheme.colorScheme.outline.toArgb()

        AndroidView(
            factory = { ctx -> 
                ResultGraphView(ctx).apply {
                    setData(counts, onBackground, onSurfaceVariant, outline) 
                } 
            },
            modifier = Modifier.weight(1f).fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(8.dp),
            update = { it.setData(counts, onBackground, onSurfaceVariant, outline) }
        )

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRestart, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.restart_app))
        }
    }
}
