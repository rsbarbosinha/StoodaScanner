package com.example.stoodascanner.frontend

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stoodascanner.backend.AppState
import com.example.stoodascanner.R

@Composable
fun MenuScreen(
    onNavigate: (AppState) -> Unit,
    onExit: () -> Unit,
    isDebugMode: Boolean,
    onDebugToggle: (Boolean) -> Unit
) {
    var clickCount by remember { mutableIntStateOf(0) }
    var lastClickTime by remember { mutableLongStateOf(0L) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.main_menu),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333),
            modifier = Modifier.clickable {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime < 500) {
                    clickCount++
                } else {
                    clickCount = 1
                }
                lastClickTime = currentTime
                if (clickCount >= 5) {
                    onDebugToggle(!isDebugMode)
                    clickCount = 0
                }
            }
        )
        if (isDebugMode) {
            Text(
                text = stringResource(R.string.debug_mode_on),
                color = Color.Red,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = { onNavigate(AppState.CLASS_CREATION) },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text(stringResource(R.string.new_class_creation))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onNavigate(AppState.CLASS_MANAGEMENT) },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text(stringResource(R.string.manage_classes))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onNavigate(AppState.QUICK_SETUP) },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text(stringResource(R.string.quick_custom_quiz))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onNavigate(AppState.SELECT_CLASS) },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text(stringResource(R.string.new_quiz_creation))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onExit,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text(stringResource(R.string.quit))
        }
    }
}
