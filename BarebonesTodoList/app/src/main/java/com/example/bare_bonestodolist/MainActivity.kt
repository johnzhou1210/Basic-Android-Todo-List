package com.example.bare_bonestodolist

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.bare_bonestodolist.ui.theme.BarebonesTodoListTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")


object TodoDataStore {
    private val TODO_LIST_KEY = stringSetPreferencesKey("todo_list")
    fun getTodoList(context: Context): Flow<List<String>> {
        return context.dataStore.data.map { preferences ->
            preferences[TODO_LIST_KEY]?.toList() ?: emptyList()
        }
    }
    suspend fun setTodoList(context: Context, todoList: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[TODO_LIST_KEY] = todoList.toSet()
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
    var text by remember { mutableStateOf("") }
    val todoList = remember { mutableStateListOf<String>() }
    val coroutineScope = rememberCoroutineScope()

    // Load saved tasks
    LaunchedEffect(Unit) {
        TodoDataStore.getTodoList(context).collect { savedTodoList ->
            todoList.clear()
            todoList.addAll(savedTodoList)
        }
    }

    fun confirmAddTask(text: String) {
        if (text.isNotBlank()) {
            todoList.add(text)
            coroutineScope.launch {
                TodoDataStore.setTodoList(context, todoList)
            }
        }
    }

    fun confirmDeleteTask(index: Int) {
        todoList.removeAt(index)
        coroutineScope.launch {
            TodoDataStore.setTodoList(context, todoList)
        }
    }

    Scaffold(modifier = Modifier, topBar = {
        TopAppBar(title = { Text(text = "Todo List") })
    }, content = { innerPadding ->
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                TextField(
                    singleLine = true,
                    keyboardActions = KeyboardActions(onDone = {
                        confirmAddTask(text); text = ""
                    }),
                    value = text,
                    onValueChange = {
                        if (it.length <= 36) {
                            text = it
                        }

                    },
                    label = { Text("What needs to be done?") },
                    modifier = Modifier
                        .width(300.dp)
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp)
                )
                ElevatedButton(
                    onClick = { confirmAddTask(text); text = "" },
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(horizontal = 8.dp),
                ) {
                    Text(text = "Add")
                }
            }

            /* List */
            ListWidget(todoList, onConfirmDeleteTask = { confirmDeleteTask(it) })

        }

    })
}

@Composable
fun ListWidget(todoList: MutableList<String>, onConfirmDeleteTask : (Int) -> Unit) {
    LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
        items(todoList.size) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()

            ) {
                Box(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .background(color = Color.Blue)

                ) {
                    Text(
                        color = Color.White, text = todoList[it], modifier = Modifier.padding(16.dp)
                    )
                }
                ElevatedButton(
                    onClick = { onConfirmDeleteTask(it) },
                    modifier = Modifier.padding(horizontal = 8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete, contentDescription = "Delete"
                    )
                }
            }


        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BarebonesTodoListTheme {
        App()
    }
}