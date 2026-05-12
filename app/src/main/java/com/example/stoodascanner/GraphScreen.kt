package com.example.stoodascanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun GraphScreen(
    scannedCodes: List<String>,
    selectedClass: StudentClass?,
    onRestart: () -> Unit
) {
    val decoder = remember { QRDecoder(selectedClass) }
    val counts = remember(scannedCodes.size) {
        val map = mutableMapOf<String, Int>()
        scannedCodes.forEach { code ->
            val decoded = decoder.decode(code)
            val type = decoded.split(" - ").getOrNull(1) ?: "?"
            map[type] = (map[type] ?: 0) + 1
        }
        map.toSortedMap()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = stringResource(R.string.results_distribution),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        AndroidView(
            factory = { ctx -> ResultGraphView(ctx).apply { setData(counts) } },
            modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFFF5F5F5)).padding(8.dp),
            update = { it.setData(counts) }
        )

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRestart, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.restart_app))
        }
    }
}
