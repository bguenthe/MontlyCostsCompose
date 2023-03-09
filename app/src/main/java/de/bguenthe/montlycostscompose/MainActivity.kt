package de.bguenthe.montlycostscompose

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.pager.ExperimentalPagerApi
import de.bguenthe.montlycostscompose.repository.Constants
import de.bguenthe.montlycostscompose.repository.CostsRepository
import de.bguenthe.montlycostscompose.ui.theme.ButtonBlue
import kotlin.collections.LinkedHashMap

data class BarchartObject(val name: String, val value: ArrayList<Float>, val color: Int)

class MainActivity : ComponentActivity() {
    private lateinit var costsRepository: CostsRepository
    private lateinit var constants: LinkedHashMap<String, Constants.Consts>

    var currentYearOfPage = 0
    var currentMonthOfPage = 0

    @OptIn(ExperimentalAnimationApi::class, ExperimentalPagerApi::class, ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        costsRepository = CostsRepository(applicationContext)

//        costsRepository.saveConcreteIncome(3498.63)
//        costsRepository.deleteByIds()

        constants = costsRepository.getConstants()
        costsRepository.subscribeServerCostsMQTT()
        costsRepository.subscribeServerIncomeMQTT()

        super.onCreate(savedInstanceState)
        setContent {
            Column {
                MyApp()
            }
        }
    }

    @OptIn(ExperimentalAnimationApi::class, ExperimentalPagerApi::class)
    @Composable
    fun MyApp() {
        val navController = rememberNavController()
        Scaffold(
            bottomBar = { BottomNavigationBar(navController) }
        ) {
            Navigation(navController = navController)
        }
    }

    @Composable
    fun Navigation(navController: NavHostController) {
        NavHost(navController, startDestination = NavigationItem.Home.route) {
            composable(NavigationItem.Home.route) {
                HomeScreen(costsRepository, currentYearOfPage = { n: Int ->
                    currentYearOfPage = n
                }, currentMonthOfPage = { n: Int ->
                    currentMonthOfPage = n
                })
            }
            composable(NavigationItem.Costs.route) {
                CostsScreen(costsRepository, currentYearOfPage, currentMonthOfPage)
            }
            composable(NavigationItem.Monthly_Average.route) {
                         MonthlyAverageScreen(costsRepository)
            }
            composable(NavigationItem.Daily_Average.route) {
                DailyAverageScreen(costsRepository)
            }
            composable(NavigationItem.Sums.route) {
                SumsScreen(costsRepository)
            }
            composable(NavigationItem.Settings.route) {
                SettingsScreen(costsRepository)
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        NavigationItem.Home,
        NavigationItem.Costs,
        NavigationItem.Monthly_Average,
        NavigationItem.Daily_Average,
        NavigationItem.Sums,
        NavigationItem.Settings
    )
    BottomNavigation(
        backgroundColor = ButtonBlue,
        contentColor = androidx.compose.ui.graphics.Color.White
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { item ->
            BottomNavigationItem(
                icon = { Icon(painterResource(id = item.icon), contentDescription = item.title, modifier = Modifier.size(20.dp)) },
                label = { Text(text = item.title) },
                selectedContentColor = androidx.compose.ui.graphics.Color.White,
                unselectedContentColor = androidx.compose.ui.graphics.Color.White.copy(0.4f),
                alwaysShowLabel = true,
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}