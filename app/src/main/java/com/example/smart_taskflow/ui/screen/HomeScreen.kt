package com.example.smart_taskflow.ui.screen

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smart_taskflow.data.model.Task
import com.example.smart_taskflow.viewmodel.TaskViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ---------------------------
// Priority calculation
// ---------------------------
fun Task.calculatePriority(): Int {
    if (isDone) return 0
    var score = 0
    if (isImportant) score += 3

    dueDate?.let { date ->
        val now = System.currentTimeMillis()
        val daysLeft = ((date.time - now) / (1000 * 60 * 60 * 24)).toInt()

        score += when {
            daysLeft == 0 -> 5
            daysLeft == 1 -> 3
            daysLeft in 2..3 -> 2
            else -> 0
        }
    }
    return score
}

// ---------------------------
// Date Grouping
// ---------------------------
fun Task.dateGroup(): String {
    dueDate ?: return "No Date"

    val today = Calendar.getInstance()
    val d = Calendar.getInstance().apply { time = dueDate }

    return when {
        d.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                d.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) ->
            "Today"

        d.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                d.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) + 1 ->
            "Tomorrow"

        d.get(Calendar.WEEK_OF_YEAR) == today.get(Calendar.WEEK_OF_YEAR) ->
            "This Week"

        else -> "Upcoming"
    }
}

// ---------------------------
// Category logic
// ---------------------------
fun Task.assignCategory(): String {
    val categories = linkedMapOf(
        "Home" to listOf("clean", "cook", "laundry", "room", "house"),
        "Work" to listOf("meeting", "project", "office", "deadline"),
        "Bills" to listOf("payment", "invoice", "bill"),
        "Studies" to listOf("exam", "study", "school"),
        "Shopping" to listOf("buy", "shop", "store"),
        "Sport" to listOf("run", "gym", "exercise"),
        "Health" to listOf("doctor", "medicine", "hospital"),
        "Transportation" to listOf("car", "drive", "bus"),
        "Other" to emptyList()
    )

    for ((cat, list) in categories) {
        if (list.any { title.contains(it, true) || description.contains(it, true) })
            return cat
    }
    return "Other"
}

fun priorityColor(priority: Int): Color = when (priority) {
    0 -> Color.Gray
    1, 2 -> Color(0xFFFFC107)
    3, 4 -> Color(0xFFFF9800)
    else -> Color.Red
}

// ---------------------------
// Home Screen
// ---------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: TaskViewModel = viewModel(),
    category: String = "all"
) {
    val tasks by viewModel.tasks.collectAsState()

    val userId = viewModel.userId
    val context = LocalContext.current

    var showAdd by remember { mutableStateOf(false) }
    var showEdit by remember { mutableStateOf(false) }
    var editTask by remember { mutableStateOf<Task?>(null) }
    var showDetails by remember { mutableStateOf<Task?>(null) }
    var search by remember { mutableStateOf("") }
    var archiveExpanded by remember { mutableStateOf(false) }

    val knownCategories = listOf(
        "Home", "Work", "Bills", "Studies", "Shopping",
        "Sport", "Health", "Personal Projects", "Transportation"
    )

    val filtered = tasks
        .filter { it.userId == userId }
        .filter {
            when (category.lowercase()) {
                "all" -> !it.isDone
                "other" -> !it.isDone && it.assignCategory() !in knownCategories
                else -> !it.isDone && it.assignCategory().equals(category, true)
            }
        }
        .filter { it.title.contains(search, true) || it.description.contains(search, true) }

    val grouped = filtered.sortedByDescending { it.calculatePriority() }
        .groupBy { it.dateGroup() }

    val archived = tasks.filter { it.userId == userId && it.isDone }

    Scaffold(
        topBar = {
            Column {
                SmallTopAppBar(
                    title = { Text("TaskFlow", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color(0xFF6200EE))
                )
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    placeholder = { Text("Search tasks...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAdd = true },
                containerColor = Color(0xFF6200EE)
            ) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            grouped.forEach { (groupName, list) ->

                item {
                    Text(
                        groupName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.LightGray.copy(alpha = 0.3f))
                            .padding(8.dp),
                        fontWeight = FontWeight.Bold
                    )
                }

                items(list) { task ->
                    ModernTaskItem(
                        task = task,
                        onDelete = { viewModel.deleteTask(task) },
                        onToggleDone = {
                            viewModel.updateTask(task.copy(isDone = !task.isDone))
                        },
                        onImportantToggle = {
                            viewModel.updateTask(task.copy(isImportant = !task.isImportant))
                        },
                        onEdit = {
                            editTask = task
                            showEdit = true
                        },
                        onShowDetails = { showDetails = task },
                        isArchiveItem = false
                    )
                }
            }

            // archived tasks
            if (archived.isNotEmpty()) {

                item {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(Color.LightGray.copy(alpha = 0.3f))
                            .padding(12.dp)
                            .clickable { archiveExpanded = !archiveExpanded },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Archived Tasks", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            if (archiveExpanded) Icons.Default.KeyboardArrowUp
                            else Icons.Default.KeyboardArrowDown,
                            null
                        )
                    }
                }

                if (archiveExpanded) {
                    items(archived) { task ->
                        ModernTaskItem(
                            task = task,
                            onDelete = { viewModel.deleteTask(task) },
                            onToggleDone = {
                                viewModel.updateTask(task.copy(isDone = false))
                            },
                            onImportantToggle = {
                                viewModel.updateTask(task.copy(isImportant = !task.isImportant))
                            },
                            onEdit = {
                                editTask = task
                                showEdit = true
                            },
                            onShowDetails = { showDetails = task },
                            isArchiveItem = true
                        )
                    }
                }
            }
        }
    }

    // dialogs
    if (showAdd)
        TaskDialog(
            title = "New Task",
            onConfirm = { title, desc, date, important ->
                viewModel.addTask(
                    Task(
                        title = title,
                        description = desc,
                        dueDate = date,
                        isImportant = important,
                        userId = userId
                    )
                )
                showAdd = false
            },
            onDismiss = { showAdd = false }
        )

    if (showEdit && editTask != null)
        TaskDialog(
            title = "Edit Task",
            initialTitle = editTask!!.title,
            initialDescription = editTask!!.description,
            initialDueDate = editTask!!.dueDate,
            initialImportant = editTask!!.isImportant,
            onConfirm = { title, desc, date, important ->
                viewModel.updateTask(
                    editTask!!.copy(
                        title = title,
                        description = desc,
                        dueDate = date,
                        isImportant = important
                    )
                )
                showEdit = false
            },
            onDismiss = { showEdit = false }
        )

    if (showDetails != null)
        TaskDetailsDialog(task = showDetails!!) { showDetails = null }
}

// ------------------------------
// Modern Task Item
// ------------------------------
@Composable
fun ModernTaskItem(
    task: Task,
    onDelete: () -> Unit,
    onToggleDone: () -> Unit,
    onImportantToggle: () -> Unit,
    onEdit: () -> Unit,
    onShowDetails: () -> Unit,
    isArchiveItem: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .animateContentSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, drag ->
                    if (drag > 100) onToggleDone()
                    if (drag < -100) onDelete()
                }
            }
            .clickable { onShowDetails() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            val col = animateColorAsState(priorityColor(task.calculatePriority())).value

            Box(
                Modifier
                    .width(6.dp)
                    .height(60.dp)
                    .background(col)
            )

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(task.title, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = onImportantToggle) {
                        Icon(
                            Icons.Default.Star,
                            null,
                            tint = if (task.isImportant) Color.Yellow else Color.Gray
                        )
                    }
                }
                if (task.description.isNotEmpty())
                    Text(task.description, color = Color.Gray)
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, null)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, null)
            }
        }
    }
}

// ------------------------------
// Task Details Dialog
// ------------------------------
@Composable
fun TaskDetailsDialog(task: Task, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(task.title) },
        text = {
            Column {
                Text(task.description)
                task.dueDate?.let {
                    Text("Due: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)}")
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Close") }
        }
    )
}

// ------------------------------
// Task Dialog (Add / Edit)
// ------------------------------
@Composable
fun TaskDialog(
    title: String,
    initialTitle: String = "",
    initialDescription: String = "",
    initialDueDate: Date? = null,
    initialImportant: Boolean = false,
    onConfirm: (String, String, Date?, Boolean) -> Unit,
    onDismiss: () -> Unit
) {

    val context = LocalContext.current

    var t by remember { mutableStateOf(initialTitle) }
    var d by remember { mutableStateOf(initialDescription) }
    var due by remember { mutableStateOf(initialDueDate) }
    var imp by remember { mutableStateOf(initialImportant) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {

                OutlinedTextField(
                    value = t,
                    onValueChange = { t = it },
                    label = { Text("Title") }
                )

                OutlinedTextField(
                    value = d,
                    onValueChange = { d = it },
                    label = { Text("Description") }
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Important")
                    Switch(checked = imp, onCheckedChange = { imp = it })
                }

                OutlinedButton(
                    onClick = {
                        val cal = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _: DatePicker, y: Int, m: Int, day: Int ->
                                cal.set(y, m, day)
                                due = cal.time
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
                ) {
                    Text(
                        due?.let {
                            "Due: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)}"
                        } ?: "Select date"
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (t.isNotBlank()) {
                        onConfirm(t, d, due, imp)
                    }
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
