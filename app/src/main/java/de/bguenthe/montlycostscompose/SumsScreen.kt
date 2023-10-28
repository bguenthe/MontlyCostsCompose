package de.bguenthe.montlycostscompose

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import de.bguenthe.montlycostscompose.chart.ChartEngine
import de.bguenthe.montlycostscompose.database.SumsPerType
import de.bguenthe.montlycostscompose.repository.CostsRepository

@Composable
fun SumsScreen(costsRepository: CostsRepository) {
    Column(modifier = Modifier.fillMaxSize()) {
        val constants = costsRepository.getConstants()
        val barchartlist = ArrayList<BarchartObject>()
        lateinit var costs: List<SumsPerType>

        val allCosts = costsRepository.database.costsDao().getAll
        val firstYear = allCosts[0].recordDateTime?.year
        val firstMonth = allCosts[0].recordDateTime?.month?.value
        val lasttYear = allCosts[allCosts.size - 1].recordDateTime?.year
        val lastMonth = allCosts[allCosts.size - 1].recordDateTime?.month?.value

        costs = costsRepository.database.costsDao().getSumsByType()

        val descriptionText = "Gesamtsummen ($firstYear-$firstMonth bis $lasttYear-$lastMonth)"
        Row {
            Text(text = descriptionText)
        }

        for (cost in costs) {
            Row {
                Text(text = cost.type + ": ")
                Text(text = cost.value.toString())
            }
        }

        val allCostsSum = costsRepository.getAllCostsSum()
        val allIncomeSum = costsRepository.getAllIncomeSum()
        val diff =
            ((allIncomeSum - allCostsSum) / costsRepository.getNumberOfMonthsToShow()) - (289f /*fixkosten*/ + 508f /*Postbank  Schuldentilgung*/)
        val i = 1

        Row {
            Text("Monatliches Geld über: ")
            Text(text = diff.toString())
        }
        AndroidView(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.8f), factory = { ctx ->
            ChartEngine(ctx).apply {}
        }, update = { chartEngine ->
            val constants = costsRepository.getConstants()
            val barchartlist = ArrayList<BarchartObject>()
            lateinit var costs: List<SumsPerType>

            val allCosts = costsRepository.database.costsDao().getAll
            val firstYear = allCosts[0].recordDateTime?.year
            val firstMonth = allCosts[0].recordDateTime?.month?.value
            val lasttYear = allCosts[allCosts.size - 1].recordDateTime?.year
            val lastMonth = allCosts[allCosts.size - 1].recordDateTime?.month?.value

            costs = costsRepository.database.costsDao().getSumsByType()

            for (cost in costs) {
                val f = ArrayList<Float>()
                f.add(cost.value.toFloat())
                val barchartObject = BarchartObject(cost.type, f, constants[cost.type]!!.color)
                barchartlist.add(barchartObject)
            }
            val descriptionText = "Gesamtsummen ($firstYear-$firstMonth bis $lasttYear-$lastMonth)"
            chartEngine.buildChart(barchartlist, constants, descriptionText)
        })
    }
}