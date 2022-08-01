package de.bguenthe.montlycostscompose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import de.bguenthe.montlycostscompose.chart.ChartEngine
import de.bguenthe.montlycostscompose.database.SumsPerType
import de.bguenthe.montlycostscompose.repository.CostsRepository

@Composable
fun MonthlyAverageScreen(costsRepository: CostsRepository) {
    Column (modifier = Modifier.fillMaxSize()){
        AndroidView(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f), factory = { ctx ->
            ChartEngine(ctx).apply {}
        }, update = { chartEngine ->
            val constants = costsRepository.getConstants()
            val barchartlist = ArrayList<BarchartObject>()
            lateinit var costs: List<SumsPerType>
            val s = costsRepository.database.costsDao().getMonthCount()
            costs = costsRepository.database.costsDao().getSumsByType()
            val nenner: Double = (s.size - 1).toDouble()
            for (cost in costs) {
                cost.value = cost.value / nenner
            }
            for (cost in costs) {
                val f = ArrayList<Float>()
                f.add(cost.value.toFloat())
                val barchartObject = BarchartObject(cost.type, f, constants[cost.type]!!.color)
                barchartlist.add(barchartObject)
            }

            chartEngine.buildChart(barchartlist, constants, "Monatlicher Durchschnitt")
        })
    }
}