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
fun SumsScreen(costsRepository: CostsRepository) {
    Column (modifier = Modifier.fillMaxSize()){
        AndroidView(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f), factory = { ctx ->
            ChartEngine(ctx).apply {}
        }, update = { chartEngine ->
            val constants = costsRepository.getConstants()
            val barchartlist = ArrayList<BarchartObject>()
            lateinit var costs: List<SumsPerType>

            val allCosts = costsRepository.database.costsDao().getAll
            val firstYear  = allCosts[0].recordDateTime?.year
            val firstMonth = allCosts[0].recordDateTime?.month?.value
            val lasttYear  = allCosts[allCosts.size - 1].recordDateTime?.year
            val lastMonth = allCosts[allCosts.size - 1].recordDateTime?.month?.value

            costs = costsRepository.database.costsDao().getSumsByType()
            for (cost in costs) {
                cost.value = cost.value
            }

            for (cost in costs) {
                val f = ArrayList<Float>()
                f.add(cost.value.toFloat())
                val barchartObject = BarchartObject(cost.type, f, constants[cost.type]!!.color)
                barchartlist.add(barchartObject)
            }
            val descriptionText = "Gesamtsummen ($firstYear-$firstMonth - $lasttYear-$lastMonth)"
            chartEngine.buildChart(barchartlist, constants, descriptionText)
        })
    }
}