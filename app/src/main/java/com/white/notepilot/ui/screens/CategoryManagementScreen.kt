package com.white.notepilot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.white.notepilot.R
import com.white.notepilot.data.model.Category
import com.white.notepilot.ui.components.CustomPopupDialog
import com.white.notepilot.ui.components.CustomTopBar
import com.white.notepilot.ui.theme.Blue
import com.white.notepilot.ui.theme.Dimens
import com.white.notepilot.viewmodel.CategoryViewModel
import kotlinx.coroutines.launch

@Composable
fun CategoryManagementScreen(
    navController: NavHostController,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            CustomTopBar(
                title = "Manage Categories",
                leftIconRes = R.drawable.back_arrow,
                onLeftIconClick = { navController.popBackStack() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Blue,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Category"
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (categories.isEmpty()) {
            EmptyCategoriesContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = Dimens.PaddingLarge),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                items(
                    items = categories,
                    key = { it.id }
                ) { category ->
                    CategoryItemWithCount(
                        category = category,
                        viewModel = viewModel,
                        onDelete = { categoryToDelete = category }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
        
        // Add Category Dialog
        if (showAddDialog) {
            AddCategoryDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { name, color, icon ->
                    scope.launch {
                        val category = Category(
                            name = name,
                            color = color,
                            icon = icon
                        )
                        viewModel.addCategory(category)
                        showAddDialog = false
                    }
                }
            )
        }
        
        // Delete Confirmation Dialog
        categoryToDelete?.let { category ->
            CustomPopupDialog(
                message = "Delete \"${category.name}\" category? Notes will not be deleted.",
                negativeButtonText = "Cancel",
                positiveButtonText = "Delete",
                onDismiss = { categoryToDelete = null },
                onNegativeClick = { categoryToDelete = null },
                onPositiveClick = {
                    scope.launch {
                        viewModel.deleteCategory(category)
                        categoryToDelete = null
                    }
                }
            )
        }
    }
}

@Composable
private fun CategoryItemWithCount(
    category: Category,
    viewModel: CategoryViewModel,
    onDelete: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var notesCount by remember { mutableStateOf(0) }
    
    // Fetch notes count for this category
    androidx.compose.runtime.LaunchedEffect(category.id) {
        scope.launch {
            notesCount = viewModel.getNotesCountForCategory(category.id)
        }
    }
    
    CategoryItem(
        category = category,
        notesCount = notesCount,
        onDelete = onDelete
    )
}

@Composable
private fun CategoryItem(
    category: Category,
    notesCount: Int,
    onDelete: () -> Unit
) {
    val backgroundColor = try {
        Color(category.color.toColorInt())
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(backgroundColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(getCategoryIcon(category.icon)),
                    contentDescription = category.name,
                    tint = backgroundColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Name and count
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "$notesCount ${if (notesCount == 1) "note" else "notes"}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            // Delete button
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.Red.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun EmptyCategoriesContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.label),
            contentDescription = "No categories",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No Categories Yet",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Create categories to organize your notes",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

private fun getCategoryIcon(iconName: String): Int {
    return when (iconName) {
        "person" -> R.drawable.person
        "work" -> R.drawable.work
        "lightbulb" -> R.drawable.lightbulb
        "star" -> R.drawable.star
        "checklist" -> R.drawable.checklist
        "label" -> R.drawable.label
        else -> R.drawable.label
    }
}

@Composable
private fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit
) {
    // TODO: Implement full add category dialog with name, color picker, and icon selector
    // For now, using a simple implementation
    CustomPopupDialog(
        message = "Add Category feature - Full implementation coming soon",
        negativeButtonText = "Cancel",
        positiveButtonText = "OK",
        onDismiss = onDismiss,
        onNegativeClick = onDismiss,
        onPositiveClick = onDismiss
    )
}
