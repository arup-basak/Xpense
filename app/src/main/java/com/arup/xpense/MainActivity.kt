package com.arup.xpense

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arup.xpense.ui.theme.XpenseTheme
import kotlinx.coroutines.launch
import models.TransactionModel
import java.text.SimpleDateFormat
import java.util.Date


class MainActivity : ComponentActivity() {
    private lateinit var db: DBHandler
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = DBHandler(this)

        val list = mutableStateListOf<TransactionModel>()

        list.addAll(getThisMonthData())

        setContent {
            XpenseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        Box(modifier = Modifier) {
                            Title(text = "This Month")
                        }
                        Box(modifier = Modifier) {
                            LazyColumn(
                                modifier = Modifier
                            ) {
                                items(list) { transaction ->
                                    TransactionCard(data = transaction)
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        NavigationBar(db, onClose = {
                            list.clear()
                            list.addAll(getThisMonthData())
                        }, onFilterClick = {state, fromAmount, toAmount ->
                            list.clear()
                            list.addAll(db.readTransactions(
                                minimumAmount = fromAmount,
                                maximumAmount = toAmount,
                                fromTime = state.selectedStartDateMillis,
                                toTime = state.selectedEndDateMillis
                            ))
                        })
                    }

                }
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun getThisMonthData(): List<TransactionModel> { //Not Optimized
        val date = Date(getCurrentDateTime())
        val sdf = SimpleDateFormat("MM/yyyy")
        val str = sdf.format(date)

        return db.readTransactions(fromTime = sdf.parse(str)?.let { getTimeToLong(it) })
    }

//    @SuppressLint("SimpleDateFormat")
//    private fun getThisYearData(): List<TransactionModel> { // Not Optimized
//        val sdf = SimpleDateFormat("yyyy")
//        val str = sdf.format(Date(getCurrentDateTime()))
//        return db.readTransactions(fromTime = sdf.parse(str)?.let { getTimeToLong(it) })
//    }

    private fun getCurrentDateTime(): Long {
        return System.currentTimeMillis()
    }
    private fun getTimeToLong(date: Date): Long {
        return date.time
    }
}


@Composable
fun TransactionCard(data: TransactionModel) {
    val date = Date(data.timeLong)

    Surface(
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .padding(start = 8.dp, end = 8.dp, bottom = 4.dp, top = 4.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = data.transactionName,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = data.transactionAmount.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Text(
                text = date.toString(),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = data.transactionNote,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(db: DBHandler? = null, onClose: () -> Unit) {
    val scope = rememberCoroutineScope()
    var bottomSheetVisible by remember { mutableStateOf(false) }
    val skipPartiallyExpanded by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )


    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.End
        ) {
            FloatingActionButton(
                onClick = {
                    bottomSheetVisible = true
                },
                shape = RoundedCornerShape(16.dp),
                containerColor = BottomAppBarDefaults.bottomAppBarFabColor
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        }
    }

    if(bottomSheetVisible) {
        ModalBottomSheet(
            modifier = Modifier,
            onDismissRequest = { bottomSheetVisible = false },
            sheetState = bottomSheetState,
            containerColor = MaterialTheme.colorScheme.secondary
        ) {
            var title by remember { mutableStateOf("") }
            var amount by remember { mutableStateOf("") }
            var note by remember { mutableStateOf("") }

            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                MTextField(value = title, onValueChange = { title = it }, placeholder = "Title")
                MTextField(value = amount, onValueChange = { amount = it }, keyboardType = KeyboardType.Number, placeholder = "Amount")
                MTextField(value = note, onValueChange = { note = it }, placeholder = "Description")

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    onClick = {
                        db?.addNewTransaction(title, amount.toIntOrNull() ?: 0, note)
                        scope.launch {
                            bottomSheetState.hide()
                            onClose()
                        }.invokeOnCompletion {
                            if (!bottomSheetState.isVisible) {
                                bottomSheetVisible = false
                            }
                        }
                    }) {
                    Text(text = "Submit")
                }
            }
        }
    }
}

@Composable
fun Title(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(18.dp)
    )
}

@Composable
fun MTextField(
    keyboardType : KeyboardType = KeyboardType.Text,
    value: String?,
    onValueChange: (newValue: String) -> Unit,
    placeholder: String
) {
    val pad = 4.dp
    Box(
        modifier = Modifier
            .fillMaxWidth()
        ,
        contentAlignment = Alignment.Center
    ) {
        OutlinedTextField(
            value = value ?: "",
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = pad, bottom = pad),
            placeholder = { Text(text = placeholder, color = MaterialTheme.colorScheme.onTertiary) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationBar(db: DBHandler?, onClose: () -> Unit, onFilterClick: (state: DateRangePickerState, fromAmount: Int, toAmount: Int) -> Unit) {
    var dialogState by remember { mutableStateOf(false) }

    val state = rememberDateRangePickerState(initialDisplayMode = DisplayMode.Input)
    val fromAmountState = remember { mutableStateOf("0") }
    val toAmountState = remember { mutableStateOf("0") }

    val inputModifier = remember { Modifier.padding(4.dp) }

    @Composable
    fun FilterDialog(
        state: DateRangePickerState,
        fromAmountState: MutableState<String>,
        toAmountState: MutableState<String>,
        onFilterClick: () -> Unit
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ),

                ) {
                DateRangePicker(state = state, modifier = Modifier.padding(12.dp))

                Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    OutlinedTextField(
                        value = fromAmountState.value,
                        modifier = inputModifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        onValueChange = { fromAmountState.value = it }
                    )

                    OutlinedTextField(
                        value = toAmountState.value,
                        modifier = inputModifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        onValueChange = { toAmountState.value = it }
                    )
                }

                Button(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    onClick = {
                        onFilterClick()
                        dialogState = false
                    }
                ) {
                    Text(text = "Filter")
                }
            }
        }
    }

    Box(
        modifier = Modifier
    ) {
        Crossfade(targetState = dialogState) { showDialog ->
            if (showDialog) {
                FilterDialog(
                    state = state,
                    fromAmountState = fromAmountState,
                    toAmountState = toAmountState,
                    onFilterClick = {
                        onFilterClick(
                            state,
                            Integer.parseInt(fromAmountState.value),
                            Integer.parseInt(toAmountState.value))
                    }
                )
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {
        BottomAppBar(
            actions = {
                IconButton(onClick = {
                    dialogState = true
                }) {
                    Icon(
                        Icons.Filled.FilterList,
                        contentDescription = "Localized description",
                    )
                }
            },
            floatingActionButton = {
                BottomSheet(db) { onClose() }
            }
        )
    }
}



@Composable
@Preview(showBackground = true)
fun PreviewCard() {
    TransactionCard(data = TransactionModel(
        0,
        "Milk",
        35,
        1455555555555555,
        "This is Note"
    ))
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewNavigationBar() {
    NavigationBar(null, onClose = {}, onFilterClick = {_, _, _ -> })
}