package de.bguenthe.montlycostscompose.chart

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import de.bguenthe.montlycostscompose.BarchartObject
import de.bguenthe.montlycostscompose.repository.Constants

class ChartEngine(context: Context) : BarChart(context) {
    init {
        this.setDrawBarShadow(false)
        this.setDrawValueAboveBar(true)
        this.axisRight.isEnabled = false
        this.axisLeft.isEnabled = false
        this.xAxis.isEnabled = true
        this.xAxis.granularity = 1f
        this.xAxis.position = XAxis.XAxisPosition.BOTTOM
        //this.xAxis.setAvoidFirstLastClipping(true)
        this.xAxis.textColor = Color.BLACK
        this.setPinchZoom(false)
        //this.setDrawGridBackground(true)
        this.legend.isEnabled = false
        this.description.isEnabled = true
        this.setTouchEnabled(false)
    }

    fun buildChart(
        monthlyCosts: ArrayList<BarchartObject>,
        constants: LinkedHashMap<String, Constants.Consts>,
        descriptionText: String,
    ) {
        this.data?.clearValues()
        this.xAxis.valueFormatter = null
        this.notifyDataSetChanged()
        this.clear()
        this.invalidate()
        this.description.textSize = 20f
        this.description.textColor = Color.BLACK
        this.description.setPosition(100f, 150f)
        this.description.textAlign = Paint.Align.LEFT
        this.description.text = descriptionText

        val labels = ArrayList<String>()
        val bars: ArrayList<IBarDataSet> = ArrayList()
        val costshash: LinkedHashMap<String, BarchartObject> = LinkedHashMap<String, BarchartObject>()
        for (constant in constants) {
            for (cost in monthlyCosts)
                if (constant.key == cost.name) {
                    costshash[constant.key] = cost
                }
        }

        for ((i, costs) in monthlyCosts.withIndex()) {
            labels.add(costs.name)
            val be = BarEntry(i.toFloat(), costs.value.toFloatArray())
            val entries: ArrayList<BarEntry> = ArrayList()
            entries.add(be)
            val barDataSet = BarDataSet(entries, "")
            barDataSet.valueTextSize = 12f
            if (i < monthlyCosts.size - 1) {
                barDataSet.color = constants[costs.name]!!.color
            } else { // Statistik
                barDataSet.setColors(
                    Color.rgb(255, 0, 0), // ROT
                    Color.rgb(38, 203, 217), //BLAU
                    Color.rgb(0, 255, 0) // GRÜN
                )
            }
            bars.add(barDataSet)
        }

        this.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        this.xAxis.granularity = 1f

        val barData = BarData(bars)
        barData.barWidth = 0.9f
        this.data = barData
        this.notifyDataSetChanged()
        this.invalidate()
    }
}