package com.example.bare_bonestodolist

import android.content.Context
import android.os.Bundle
import kotlinx.coroutines.flow.combine // This seems unused, consider removing
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable // Ensure this is imported
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager // Import LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.bare_bonestodolist.pages.CompletedPage
import com.example.bare_bonestodolist.pages.TodoPage
import com.example.bare_bonestodolist.ui.theme.BarebonesTodoListTheme
import kotlinx.coroutines.flow.Flow
// import kotlinx.coroutines.flow.combine // Already imported above
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")


object TodoDataStore {
    private val TODO_LIST_KEY = stringSetPreferencesKey("todo_list")
    fun getTodoList(context: Context): Flow<List<String>> {
        Log.d("TodoDataStore", "BACKEND getting todo list")
        return context.dataStore.data.map { preferences ->
            preferences[TODO_LIST_KEY]?.toList() ?: emptyList()
        }
    }

    suspend fun setTodoList(context: Context, todoList: List<String>) {
        Log.d("TodoDataStore", "BACKEND setting todo list: $todoList")
        context.dataStore.edit { preferences ->
            preferences[TODO_LIST_KEY] = todoList.toSet()
        }
    }

    private val COMPLETED_LIST_KEY = stringSetPreferencesKey("completed_list")
    fun getCompletedList(context: Context): Flow<List<String>> {
        Log.d("TodoDataStore", "BACKEND getting completed list")
        return context.dataStore.data.map {
            it[COMPLETED_LIST_KEY]?.toList() ?: emptyList()
        }
    }

    suspend fun setCompletedList(context: Context, completedList: List<String>) {
        Log.d("TodoDataStore", "BACKEND setting completed list $completedList")
        context.dataStore.edit {
            it[COMPLETED_LIST_KEY] = completedList.toSet()
        }
    }

}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BarebonesTodoListTheme {
                App()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(context: Context = LocalContext.current) {
    val activeTab = remember { mutableStateOf<String>("todo") }
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    val focusManager = LocalFocusManager.current // Get FocusManager

    // Load saved tasks and completed tasks
    val todoList by TodoDataStore.getTodoList(context).collectAsState(initial = emptyList())
    val completedList by TodoDataStore.getCompletedList(context).collectAsState(initial = emptyList())


    fun confirmAddTask(text: String) {
        if (text.isNotBlank()) {
            val updated = todoList + text
            coroutineScope.launch {
                TodoDataStore.setTodoList(context, updated)
                lazyListState.animateScrollToItem(todoList.size - 1)
            }
        }
    }

    fun confirmDeleteTask(index: Int) {
        val updated = todoList.toMutableList().apply { removeAt(index) }
        coroutineScope.launch {
            TodoDataStore.setTodoList(context, updated)
        }
    }

    fun confirmAddCompletedTask(index: Int) {
        val task = todoList[index]
        val updatedTodo = todoList.toMutableList().apply { removeAt(index) }
        val updatedCompleted = completedList + task

        coroutineScope.launch {
            TodoDataStore.setTodoList(context, updatedTodo)
            TodoDataStore.setCompletedList(context, updatedCompleted)

        }
    }

    fun confirmDeleteCompletedTask(index: Int) {
        val updatedCompleted = completedList.toMutableList().apply { removeAt(index) }
        coroutineScope.launch {
            TodoDataStore.setCompletedList(context, updatedCompleted)
        }
    }

    Scaffold(
        modifier = Modifier.clickable( // Your existing clickable
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = { // Add the onClick lambda here
                focusManager.clearFocus()
            }
        ),
        topBar = {
            TopAppBar(title = {
                Text(text = if (activeTab.value == "todo") "Todo" else "Completed", modifier = Modifier.padding(horizontal = 4.dp))
            })
        },
        bottomBar = {
            BottomAppBar(modifier = Modifier,
                actions = {
                    IconButton(onClick = { activeTab.value = "todo" }) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = "Todo")
                    }
                    IconButton(onClick = { activeTab.value = "completed" }) {
                        Icon(imageVector = Icons.Default.Done, contentDescription = "Completed")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(start = 4.dp)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .clickable( // Add clickable to the Column as well
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        focusManager.clearFocus()
                    }
                )
        ) {
            if (activeTab.value == "todo") {
                TodoPage(
                    confirmAddTask = { confirmAddTask(it) },
                    confirmDeleteTask = { confirmDeleteTask(it) },
                    confirmAddCompletedTask = { confirmAddCompletedTask(it) },
                    todoList = todoList,
                    completedList = completedList,
                    lazyListState = lazyListState
                )
            } else if (activeTab.value == "completed") {
                CompletedPage(
                    completedList = completedList,
                    confirmDeleteCompletedTask = { confirmDeleteCompletedTask(it) })
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    BarebonesTodoListTheme {
        App()
    }
}
