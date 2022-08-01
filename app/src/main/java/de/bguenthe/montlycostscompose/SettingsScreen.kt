package de.bguenthe.montlycostscompose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.bguenthe.montlycostscompose.repository.CostsRepository
import de.bguenthe.montlycostscompose.ui.theme.ButtonBlue
import de.bguenthe.montlycostscompose.ui.theme.DeepBlue
import de.bguenthe.montlycostscompose.ui.theme.TextWhite

@Composable
fun SettingsScreen(costsRepository: CostsRepository) {
    var value by rememberSaveable { mutableStateOf("") }

    val onValueChange: (String) -> Unit = {
        value = it
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ButtonBlue)
    ) {
        Row() {
            OutlinedTextField(
                singleLine = true,
                value = value,
                onValueChange = onValueChange,
                label = { Text("Income") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = TextStyle(color = Color.Blue, fontWeight = FontWeight.Bold),
            )

            Box(
                modifier = Modifier
                    .padding(start = 1.dp, top = 1.dp, bottom = 1.dp)
                    .clickable(onClick = {
                        if (value != "") {
                            costsRepository.saveIncome(value.toDouble())
                            value = ""
                        }
                    })
                    .clip(RoundedCornerShape(10.dp))
                    .background(DeepBlue)
                    .padding(10.dp)
            ) {
                Text(text = "Income", color = TextWhite)
            }
        }

        Box(
            modifier = Modifier
                .padding(start = 1.dp, top = 1.dp, bottom = 1.dp)
                .clickable(onClick = {
                    costsRepository.saveAllCostsToServerMQTT()
                    costsRepository.saveAllIncomeToServerMQTT()
                })
                .clip(RoundedCornerShape(10.dp))
                .background(DeepBlue)
                .padding(10.dp)
        ) {
            Text(text = "SaveAll", color = TextWhite)
        }

        Box(
            modifier = Modifier
                .padding(start = 1.dp, top = 1.dp, bottom = 1.dp)
                .clickable(onClick = {
                    costsRepository.saveAllCostsToServerMQTT()
                })
                .clip(RoundedCornerShape(10.dp))
                .background(DeepBlue)
                .padding(10.dp)
        ) {
            Text(text = "SaveAllCosts", color = TextWhite)
        }

        Box(
            modifier = Modifier
                .padding(start = 1.dp, top = 1.dp, bottom = 1.dp)
                .clickable(onClick = {
                    costsRepository.saveAllIncomeToServerMQTT()
                })
                .clip(RoundedCornerShape(10.dp))
                .background(DeepBlue)
                .padding(10.dp)
        ) {
            Text(text = "SaveAllIncome", color = TextWhite)
        }

        Box(
            modifier = Modifier
                .padding(start = 1.dp, top = 1.dp, bottom = 1.dp)
                .clickable(onClick = {
                    costsRepository.deleteAllCosts()
                    costsRepository.sendMeServerCostsMQTT()
                })
                .clip(RoundedCornerShape(10.dp))
                .background(DeepBlue)
                .padding(10.dp)
        ) {
            Text(text = "Delete all costs and GetCostsFromServer", color = TextWhite)
        }

        Box(
            modifier = Modifier
                .padding(start = 1.dp, top = 1.dp, bottom = 1.dp)
                .clickable(onClick = {
                    costsRepository.deleteAllIncome()
                    costsRepository.sendMeServerIncomeMQTT()
                })
                .clip(RoundedCornerShape(10.dp))
                .background(DeepBlue)
                .padding(10.dp)
        ) {
            Text(text = "Delete all income and GetIncomeFromServer", color = TextWhite)
        }

        val numberOfCosts = costsRepository.getNumberOfStoredCosts()
        Row {
            Text("Number of Costs stored in the database: ")
            Text(text = numberOfCosts.toString())
        }

        val numberOfIncomes = costsRepository.getNumberOfStoresIncomes()
        Row {
            Text("Number of Incomes  stored in the database: ")
            Text(text = numberOfIncomes.toString())
        }

        val allCostsSum = costsRepository.getAllCostsSum()
        val allIncomeSum = costsRepository.getAllIncomeSum()
        val diff = ((allIncomeSum - allCostsSum) / costsRepository.getNumberOfMonthsToShow()) - (289f /*fixkosten*/ + 508f /*Postbank  Schuldentilgung*/)

        Row {
            Text("Monatliches Geld über: ")
            Text(text = diff.toString())
        }
    }
}