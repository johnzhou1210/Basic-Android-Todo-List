package com.example.bare_bonestodolist

import android.content.Context
import android.os.Bundle
import kotlinx.coroutines.flow.combine
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.flow.combine
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
    val todoList = remember { mutableStateListOf<String>() }
    val completedList = remember { mutableStateListOf<String>() }
    val activeTab = remember { mutableStateOf<String>("todo") }
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    // Load saved tasks and completed tasks
    LaunchedEffect(Unit) {
        TodoDataStore.getTodoList(context).collect { savedTodoList ->
            todoList.clear()
            todoList.addAll(savedTodoList)
        }
    }
    LaunchedEffect(Unit) {
        TodoDataStore.getCompletedList(context).collect { savedCompletedList ->
            completedList.clear()
            completedList.addAll(savedCompletedList)
        }
    }

    fun confirmAddTask(text: String) {
        if (text.isNotBlank()) {
            todoList.add(text)
            Log.d("TodoPage", "FRONTEND added task to todoList")
            coroutineScope.launch {
                TodoDataStore.setTodoList(context, todoList)
                lazyListState.animateScrollToItem(todoList.size)
            }
        }
    }

    fun confirmDeleteTask(index: Int) {
        todoList.removeAt(index)
        Log.d("TodoPage", "FRONTEND removed task from todoList")
        coroutineScope.launch {
            TodoDataStore.setTodoList(context, todoList)
        }
    }

    fun confirmAddCompletedTask(text: String) {
        completedList.add(text)
        Log.d("TodoPage", "FRONTEND added task to completedList")
        coroutineScope.launch {
            TodoDataStore.setCompletedList(context, completedList)
        }
    }

    fun confirmDeleteCompletedTask(index: Int) {
        completedList.removeAt(index)
        Log.d("TodoPage", "FRONTEND removed task from completedList")
        coroutineScope.launch {
            TodoDataStore.setCompletedList(context, completedList)
        }
    }

    Scaffold(modifier = Modifier,
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
        Column(modifier = Modifier.padding(innerPadding).padding(start = 4.dp).consumeWindowInsets(innerPadding).imePadding()) {
            if (activeTab.value == "todo") {
                TodoPage(
                    confirmAddTask = { confirmAddTask(it) },
                    confirmDeleteTask = { confirmDeleteTask(it) },
                    confirmAddCompletedTask = { confirmAddCompletedTask(it) },
                    todoList,
                    completedList,
                    lazyListState
                )
            } else if (activeTab.value == "completed") {
                CompletedPage(
                    completedList,
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