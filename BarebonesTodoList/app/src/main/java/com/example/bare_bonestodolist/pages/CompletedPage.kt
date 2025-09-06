package com.example.bare_bonestodolist.pages

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
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bare_bonestodolist.ui.theme.BarebonesTodoListTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletedPage(completedList: MutableList<String>, confirmDeleteCompletedTask: (Int) -> Unit) {
    var text by remember { mutableStateOf("") }


    Column() {
        /* List */
        CompletedListWidget(
            completedList,
            onConfirmDeleteCompletedTask = { confirmDeleteCompletedTask(it) })

    }


}

@Composable
fun CompletedListWidget(
    todoList: MutableList<String>,
    onConfirmDeleteCompletedTask: (Int) -> Unit
) {
    LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
        items(todoList.size) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Card(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        color = Color.White, text = todoList[it], modifier = Modifier.padding(16.dp)
                    )
                }
                ElevatedButton(
                    onClick = { onConfirmDeleteCompletedTask(it) },
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
fun CompletedPagePreview() {
    BarebonesTodoListTheme {
        CompletedPage(completedList = mutableListOf(), confirmDeleteCompletedTask = {})
    }
}