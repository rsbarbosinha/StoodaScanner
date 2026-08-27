package com.example.stoodascanner.frontend

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stoodascanner.R
import com.example.stoodascanner.backend.AppState
import com.example.stoodascanner.backend.MainViewModel
import com.example.stoodascanner.backend.StudentClass

@Composable
fun SelectClassScreen(
    viewModel: MainViewModel,
    onStartScan: () -> Unit
) {
    var isManageMode by remember { mutableStateOf(false) }
    var classes by remember { mutableStateOf(viewModel.classManager.getAllClasses()) }
    
    val profileColors = listOf(
        Color(0xFFE50914), // Netflix Red
        Color(0xFF54B948), // Green
        Color(0xFF00A8E1), // Blue
        Color(0xFFF5C518), // Yellow
        Color(0xFFBB86FC), // Purple
        Color(0xFF03DAC6)  // Teal
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF141414) // Dark Netflix background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.select_class),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
                
                TextButton(
                    onClick = { isManageMode = !isManageMode },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Text(
                        text = if (isManageMode) stringResource(R.string.done) else stringResource(R.string.manage),
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(classes.indices.toList()) { index ->
                    val studentClass = classes[index]
                    ClassProfileItem(
                        studentClass = studentClass,
                        backgroundColor = profileColors[index % profileColors.size],
                        isManageMode = isManageMode,
                        onClick = {
                            if (!isManageMode) {
                                viewModel.startScanningWithClass(studentClass)
                                onStartScan()
                            }
                        },
                        onEdit = {
                            viewModel.editingClass = studentClass
                            viewModel.navigateTo(AppState.CLASS_MANAGEMENT)
                        },
                        onDelete = {
                            viewModel.classManager.deleteClass(studentClass.title)
                            classes = viewModel.classManager.getAllClasses()
                        }
                    )
                }

                item {
                    AddClassItem(onClick = {
                        viewModel.navigateTo(AppState.CLASS_CREATION)
                    })
                }

                item {
                    QuickScanItem(onClick = {
                        viewModel.navigateTo(AppState.QUICK_SETUP)
                    })
                }
            }
        }
    }
}

@Composable
fun QuickScanItem(onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(120.dp)
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF2B2B2B))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add, // Using Add for now, or something else
                contentDescription = stringResource(R.string.quick_custom_quiz),
                tint = Color.Gray,
                modifier = Modifier.size(48.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.quick_custom_quiz),
            color = Color.Gray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ClassProfileItem(
    studentClass: StudentClass,
    backgroundColor: Color,
    isManageMode: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(120.dp)
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(backgroundColor)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = studentClass.title.take(1).uppercase(),
                color = Color.White,
                fontSize = 60.sp,
                fontWeight = FontWeight.Bold
            )
            
            if (isManageMode) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = studentClass.title,
            color = if (isManageMode) Color.Gray else Color.LightGray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AddClassItem(onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(120.dp)
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF2B2B2B))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.add_class),
                tint = Color.Gray,
                modifier = Modifier.size(48.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.add_class),
            color = Color.Gray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}
