package com.example.bare_bonestodolist.pages

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bare_bonestodolist.ui.theme.BarebonesTodoListTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoPage(
    confirmAddTask: (String) -> Unit,
    confirmDeleteTask: (Int) -> Unit,
    confirmAddCompletedTask: (Int) -> Unit,
    todoList: List<String>,
    completedList: List<String>,
    lazyListState: LazyListState,
) {
    var text by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    fun addTask() {
        confirmAddTask(text)
        text = ""
    }


    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
        Column(
            modifier = Modifier.fillMaxHeight(.85.toFloat()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            /* List */
            ListWidget(
                todoList,
                onConfirmDeleteTask = { confirmDeleteTask(it) },
                onConfirmAddCompletedTask = { confirmAddCompletedTask(it) },
                completedList,
                lazyListState
            )


        }

        /* Input field */
        Row(
            modifier = Modifier.padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End
        ) {
            TextField(singleLine = true, keyboardActions = KeyboardActions(onDone = {
                addTask()
            }), value = text, onValueChange = {
                if (it.length <= 36) {
                    text = it
                }

            }, label = { Text("What needs to be done?") }, modifier = Modifier
                .width(300.dp)

                .padding(horizontal = 16.dp, vertical = 0.dp)
            )
            ElevatedButton(
                onClick = {
                    addTask()
                },
                modifier = Modifier

                    .padding(horizontal = 8.dp),
            ) {
                Text(text = "Add")
            }
        }
    }


}

@Composable
fun ListWidget(
    todoList: List<String>,
    onConfirmDeleteTask: (Int) -> Unit,
    onConfirmAddCompletedTask: (Int) -> Unit,
    completedList: List<String>,
    lazyListState: LazyListState
) {

    LazyColumn(modifier = Modifier.padding(horizontal = 16.dp), state = lazyListState) {
        items(todoList.size) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Card(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth(.65f)
                ) {
                    Text(
                        color = Color.White, text = todoList[it], modifier = Modifier.padding(16.dp)
                    )
                }
                Row(modifier = Modifier.padding(start = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    /* Delete Button */
                    ElevatedButton(
                        onClick = { onConfirmDeleteTask(it) },
                        modifier = Modifier.weight(1f).padding(horizontal = 2.dp),
                    ) {
                        Icon(
                            modifier = Modifier.scale(1.5f),
                            imageVector = Icons.Default.Delete, contentDescription = "Delete"
                        )
                    }
                    /* Complete Button */
                    ElevatedButton(
                        onClick = {
                            onConfirmAddCompletedTask(it)
                        },
                        modifier = Modifier.weight(1f).padding(horizontal = 2.dp),
                    ) {
                        Icon(
                            modifier = Modifier.scale(1.5f),
                            imageVector = Icons.Default.Done, contentDescription = "Complete"
                        )
                    }
                }
            }


        }
    }
}

@Preview(showBackground = true)
@Composable
fun TodoPagePreview() {
    BarebonesTodoListTheme {
        TodoPage(
            confirmAddTask = {},
            confirmDeleteTask = {},
            confirmAddCompletedTask = {},
            todoList = mutableListOf(),
            completedList = mutableListOf(),
            lazyListState = rememberLazyListState(),
        )
    }
}