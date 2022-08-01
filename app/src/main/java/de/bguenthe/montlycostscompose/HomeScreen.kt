package de.bguenthe.montlycostscompose

import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import de.bguenthe.montlycostscompose.chart.ChartEngine
import de.bguenthe.montlycostscompose.database.SumsPerType
import de.bguenthe.montlycostscompose.repository.Constants
import de.bguenthe.montlycostscompose.repository.CostsRepository
import de.bguenthe.montlycostscompose.ui.theme.*
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.collect

var firstRun = true

@Composable
fun TopOfHomescreen(costsRepository: CostsRepository, newValuePressed: (Boolean) -> Unit) {
    var value by rememberSaveable { mutableStateOf("") }
    var comment by rememberSaveable { mutableStateOf("") }

    val constants = costsRepository.getConstants()

    InputSection(value, onValueChange = { value = it }, comment, onBezeichnungChange = { comment = it })
    ButtonSection(constants, onButtonClicked = {
        if (value != "") {
            costsRepository.saveCosts(it, value, comment)
            newValuePressed(true)
        }
        value = ""
        comment = ""
    })
}

@Composable
fun InputSection(
    value: String, onValueChange: (String) -> Unit, bezeichnung: String, onBezeichnungChange: (String) -> Unit
) {
    Row(modifier = Modifier.padding(10.dp)) {
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            singleLine = true,
            value = value,
            onValueChange = onValueChange,
            label = { Text("Value") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = TextStyle(color = Color.Blue, fontWeight = FontWeight.Bold),
        )
        OutlinedTextField(
            value = bezeichnung,
            singleLine = true,
            onValueChange = onBezeichnungChange,
            label = { Text("Comment") })
    }
}

@Composable
fun ButtonSection(constants: LinkedHashMap<String, Constants.Consts>, onButtonClicked: (String) -> Unit) {
    var selectedChipIndex by remember {
        mutableStateOf(0)
    }
    LazyRow {
        items(constants.size) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(start = 1.dp).clickable(onClick = {
                        selectedChipIndex = it
                        onButtonClicked(constants.keys.elementAt(it))
                    }).clip(RoundedCornerShape(5.dp)).background(
                        if (selectedChipIndex == it) ButtonBlue
                        else DarkerButtonBlue
                    ).padding(15.dp)
            ) {
                Text(text = constants.values.elementAt(it).label, fontSize = 20.sp, color = TextWhite)
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalPagerApi::class)
@Composable
fun HomeScreen(costsRepository: CostsRepository, currentYearOfPage: (Int) -> Unit, currentMonthOfPage: (Int) -> Unit) {
    var newValuePressed by mutableStateOf(false)
    var year by mutableStateOf(0)
    var month by mutableStateOf(0)

    Column {
        TopOfHomescreen(costsRepository, newValuePressed = { n: Boolean ->
            newValuePressed = n
        })
        HorizontalPages(costsRepository, newValuePressed, currentYearOfPage, currentMonthOfPage)
    }
}

@OptIn(InternalCoroutinesApi::class)
@ExperimentalAnimationApi
@ExperimentalPagerApi // HorizontalPager is experimental
@Composable
fun HorizontalPages(
    costsRepository: CostsRepository,
    newValuePressed: Boolean,
    currentYearOfPage: (Int) -> Unit,
    currenMonthOfPage: (Int) -> Unit,
) {

    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState()

    val numberOfMonthsToShow = costsRepository.getNumberOfMonthsToShow()
    val constants = costsRepository.getConstants()

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            val year = LocalDate.now().minus((numberOfMonthsToShow - (page + 1)).toLong(), ChronoUnit.MONTHS).year
            val month =
                LocalDate.now().minus((numberOfMonthsToShow - (page + 1)).toLong(), ChronoUnit.MONTHS).monthValue
            currentYearOfPage(year)
            currenMonthOfPage(month)
        }
    }

    HorizontalPager(count = numberOfMonthsToShow, state = pagerState) { page ->
        // Our page content
        AndroidView(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f), factory = { ctx ->
            ChartEngine(ctx).apply {}
        }, update = { chartEngine ->
            val year = LocalDate.now().minus((numberOfMonthsToShow - (page + 1)).toLong(), ChronoUnit.MONTHS).year
            val month =
                LocalDate.now().minus((numberOfMonthsToShow - (page + 1)).toLong(), ChronoUnit.MONTHS).monthValue
            val barchartlist = ArrayList<BarchartObject>()
            lateinit var costs: List<SumsPerType>
            costs = costsRepository.getMonthlySumsPerType(year, month)
            for (cost in costs) {
                val f = ArrayList<Float>()
                f.add(cost.value.toFloat())
                val barchartObject = BarchartObject(cost.type, f, constants[cost.type]!!.color)
                barchartlist.add(barchartObject)
            }

            // Summe der Kosten
            val sums = costsRepository.getMonthlySumOfCosts(year, month)
            val income = costsRepository.getMonthlyIncome(year, month)
            val f = ArrayList<Float>()
            f.add(sums[0].value.toFloat()) // Alle Kosten zusammen

            // Fixkosten
            val fixcosts = 289f /*fixkosten*/ + 508f /*Postbank  Schuldentilgung*/
            f.add(fixcosts)

            // Geld über Einkommen - Fixkosten - Kosten des Monats
            var result = 0f
            if (income != null) {
                result = (income.income - fixcosts - sums[0].value).toFloat() // Monatsgewinn
            }
            f.add(result)

            val barchartObject = BarchartObject("sum", f, 0)
            barchartlist.add(barchartObject)

            val descriptionText = year.toString() + "." + month.toString()
            chartEngine.buildChart(barchartlist, constants, descriptionText)
        })
    }

    scope.launch {
        if (numberOfMonthsToShow > 0) {
            if (firstRun) {
                pagerState.scrollToPage(numberOfMonthsToShow - 1)
            }
        }
        firstRun = false
    }
}