package com.white.notepilot.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.white.notepilot.R
import com.white.notepilot.data.model.Note
import com.white.notepilot.ui.components.CustomTopBar
import com.white.notepilot.ui.theme.Blue
import com.white.notepilot.ui.theme.Dimens
import com.white.notepilot.ui.theme.NotesTheme
import com.white.notepilot.viewmodel.AuthViewModel
import com.white.notepilot.viewmodel.NotesViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun UnsyncedNotesScreen(
    navController: NavHostController,
    viewModel: NotesViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(stringResource(R.string.create_edit), stringResource(R.string.recycle_bin))

    Scaffold(
        topBar = {
            CustomTopBar(
                leftIconRes = R.drawable.back_arrow,
                onLeftIconClick = { navController.popBackStack() },
                title = stringResource(R.string.unsynced_notes)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = Blue,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = Blue,
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) Blue else MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.6f
                                )
                            )
                        }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> CreateEditTab(viewModel, authViewModel)
                1 -> RecycleBinTab(viewModel, authViewModel)
            }
        }
    }
}

@Composable
private fun CreateEditTab(
    viewModel: NotesViewModel,
    authViewModel: AuthViewModel
) {
    var unsyncedNotes by remember { mutableStateOf<List<Note>>(emptyList()) }
    val selectedNotes = remember { mutableStateMapOf<Int, Boolean>() }
    val syncingNotes = remember { mutableStateMapOf<Int, Boolean>() }
    val syncedNotes = remember { mutableStateMapOf<Int, Boolean>() }
    var isSyncing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val currentUser = authViewModel.getCurrentUser()

    LaunchedEffect(Unit) {
        viewModel.getUnsyncedNotesCount { _ ->
            scope.launch {
                viewModel.getUnsyncedNotesList { notes ->
                    unsyncedNotes = notes
                }
            }
        }
    }

    val selectedCount = selectedNotes.count { it.value }
    val allSelected =
        unsyncedNotes.isNotEmpty() && selectedNotes.size == unsyncedNotes.size && selectedNotes.all { it.value }

    Box(modifier = Modifier.fillMaxSize()) {
        if (unsyncedNotes.isEmpty()) {
            EmptyStateContent(
                icon = R.drawable.check,
                title = stringResource(R.string.all_notes_synced),
                message = stringResource(R.string.all_your_notes_are_backed_up_to_the_cloud)
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                SelectAllHeader(
                    allSelected = allSelected,
                    selectedCount = selectedCount,
                    totalCount = unsyncedNotes.size,
                    onSelectAllClick = {
                        if (allSelected) {
                            selectedNotes.clear()
                        } else {
                            unsyncedNotes.forEach { note ->
                                selectedNotes[note.id] = true
                            }
                        }
                    }
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(
                        horizontal = Dimens.PaddingMedium,
                        vertical = Dimens.PaddingSmall
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium)
                ) {
                    items(
                        items = unsyncedNotes,
                        key = { note -> note.id }
                    ) { note ->
                        UnsyncedNoteItem(
                            note = note,
                            isSelected = selectedNotes[note.id] == true,
                            isSyncing = syncingNotes[note.id] == true,
                            isSynced = syncedNotes[note.id] == true,
                            onSelectionChange = { selected ->
                                selectedNotes[note.id] = selected
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }

            AnimatedVisibility(
                visible = selectedCount > 0,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            ) {
                SyncButton(
                    selectedCount = selectedCount,
                    isSyncing = isSyncing,
                    buttonText = stringResource(R.string.sync),
                    onClick = {
                        isSyncing = true
                        scope.launch {
                            val notesToSync = unsyncedNotes.filter { selectedNotes[it.id] == true }

                            notesToSync.forEach { note ->
                                syncingNotes[note.id] = true

                                currentUser?.uid?.let { userId ->
                                    viewModel.forceSyncNote(note, userId) { success ->
                                        scope.launch {
                                            delay(500)
                                            syncingNotes[note.id] = false

                                            if (success) {
                                                syncedNotes[note.id] = true
                                                delay(1000)

                                                unsyncedNotes =
                                                    unsyncedNotes.filter { it.id != note.id }
                                                selectedNotes.remove(note.id)
                                                syncedNotes.remove(note.id)
                                            }
                                        }
                                    }
                                }

                                delay(300)
                            }

                            isSyncing = false
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun RecycleBinTab(
    viewModel: NotesViewModel,
    authViewModel: AuthViewModel
) {
    var deletedNotes by remember { mutableStateOf<List<Note>>(emptyList()) }
    val selectedNotes = remember { mutableStateMapOf<Int, Boolean>() }
    val deletingNotes = remember { mutableStateMapOf<Int, Boolean>() }
    val deletedNotesSuccess = remember { mutableStateMapOf<Int, Boolean>() }
    var isDeleting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val currentUser = authViewModel.getCurrentUser()

    LaunchedEffect(Unit) {
        scope.launch {
            viewModel.getUnsyncedDeletedNotesList { notes ->
                deletedNotes = notes
            }
        }
    }

    val selectedCount = selectedNotes.count { it.value }
    val allSelected =
        deletedNotes.isNotEmpty() && selectedNotes.size == deletedNotes.size && selectedNotes.all { it.value }

    Box(modifier = Modifier.fillMaxSize()) {
        if (deletedNotes.isEmpty()) {
            EmptyStateContent(
                icon = R.drawable.delete,
                title = stringResource(R.string.recycle_bin_empty),
                message = "Deleted notes will appear here.\nYou can restore or permanently delete them."
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                SelectAllHeader(
                    allSelected = allSelected,
                    selectedCount = selectedCount,
                    totalCount = deletedNotes.size,
                    onSelectAllClick = {
                        if (allSelected) {
                            selectedNotes.clear()
                        } else {
                            deletedNotes.forEach { note ->
                                selectedNotes[note.id] = true
                            }
                        }
                    }
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = deletedNotes,
                        key = { note -> note.id }
                    ) { note ->
                        RecycleBinNoteItem(
                            note = note,
                            isSelected = selectedNotes[note.id] == true,
                            isDeleting = deletingNotes[note.id] == true,
                            isDeleted = deletedNotesSuccess[note.id] == true,
                            onSelectionChange = { selected ->
                                selectedNotes[note.id] = selected
                            },
                            onRestoreClick = {
                                scope.launch {
                                    viewModel.restoreDeletedNote(note) { success ->
                                        if (success) {
                                            scope.launch {
                                                deletedNotes =
                                                    deletedNotes.filter { it.id != note.id }
                                                selectedNotes.remove(note.id)
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }

            AnimatedVisibility(
                visible = selectedCount > 0,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            ) {
                DeletePermanentlyButton(
                    selectedCount = selectedCount,
                    isDeleting = isDeleting,
                    onClick = {
                        isDeleting = true
                        scope.launch {
                            val notesToDelete = deletedNotes.filter { selectedNotes[it.id] == true }

                            notesToDelete.forEach { note ->
                                deletingNotes[note.id] = true

                                currentUser?.uid?.let { userId ->
                                    viewModel.forceSyncDeletedNote(note, userId) { success ->
                                        scope.launch {
                                            delay(500)
                                            deletingNotes[note.id] = false

                                            if (success) {
                                                deletedNotesSuccess[note.id] = true
                                                delay(1000)

                                                deletedNotes =
                                                    deletedNotes.filter { it.id != note.id }
                                                selectedNotes.remove(note.id)
                                                deletedNotesSuccess.remove(note.id)
                                            }
                                        }
                                    }
                                }

                                delay(300)
                            }

                            isDeleting = false
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SelectAllHeader(
    allSelected: Boolean,
    selectedCount: Int,
    totalCount: Int,
    onSelectAllClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onSelectAllClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = allSelected,
                onCheckedChange = { onSelectAllClick() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Blue,
                    uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                ),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = stringResource(R.string.select_all),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Text(
            text = "$selectedCount / $totalCount",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun UnsyncedNoteItem(
    note: Note,
    isSelected: Boolean,
    isSyncing: Boolean,
    isSynced: Boolean,
    onSelectionChange: (Boolean) -> Unit
) {
    val backgroundColor = try {
        Color(note.colorCode.toColorInt())
    } catch (e: Exception) {
        MaterialTheme.colorScheme.surface
    }

    val textColor = Color.Black

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectionChange(!isSelected) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.PaddingMedium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                when {
                    isSynced -> {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(R.string.synced),
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    isSyncing -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = Blue,
                            strokeWidth = 3.dp
                        )
                    }

                    else -> {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = onSelectionChange,
                            colors = CheckboxDefaults.colors(
                                checkedColor = Blue,
                                uncheckedColor = textColor.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isSyncing) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = Blue,
                        trackColor = textColor.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun RecycleBinNoteItem(
    note: Note,
    isSelected: Boolean,
    isDeleting: Boolean,
    isDeleted: Boolean,
    onSelectionChange: (Boolean) -> Unit,
    onRestoreClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectionChange(!isSelected) },
        shape = RoundedCornerShape(8.dp)
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = note.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (note.content.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = note.content.replace(Regex("<[^>]*>"), "").trim(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onRestoreClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.restore),
                            contentDescription = "Restore",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                when {
                    isDeleted -> {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Deleted",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    isDeleting -> {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.Red,
                                strokeWidth = 2.dp
                            )
                        }
                    }

                    else -> {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) Color.Red
                                    else MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                                )
                                .border(
                                    width = 2.dp,
                                    color = if (isSelected) Color.Red
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                                .clickable { onSelectionChange(!isSelected) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (isDeleting) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.BottomCenter),
                    color = Color.Red,
                    trackColor = Color.Transparent
                )
            }
        }
    }
}

@Composable
private fun DeletePermanentlyButton(
    selectedCount: Int,
    isDeleting: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = !isDeleting,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .height(50.dp),
        shape = RoundedCornerShape(25.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Red,
            disabledContainerColor = Color.Red.copy(alpha = 0.6f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            if (isDeleting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Deleting...",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.delete),
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Delete $selectedCount Permanently",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun SyncButton(
    selectedCount: Int,
    isSyncing: Boolean,
    buttonText: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = !isSyncing,
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (buttonText == stringResource(R.string.delete)) Color.Red else Blue,
            disabledContainerColor = if (buttonText == stringResource(R.string.delete)) Color.Red.copy(
                alpha = 0.6f
            ) else Blue.copy(
                alpha = 0.6f
            )
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            if (isSyncing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (buttonText == stringResource(R.string.delete)) stringResource(R.string.deleting) else stringResource(
                        R.string.syncing
                    ),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Text(
                    text = "$buttonText $selectedCount ${if (selectedCount == 1) "Note" else "Notes"}",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun EmptyStateContent(
    icon: Int,
    title: String,
    message: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.PaddingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = title,
            modifier = Modifier.size(80.dp),
            tint = Color(0xFF4CAF50)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            lineHeight = 26.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun UnsyncedNotesScreenPreview() {
    NotesTheme {
        UnsyncedNotesScreen(navController = rememberNavController())
    }
}
