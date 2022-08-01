package de.bguenthe.montlycostscompose

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.bguenthe.montlycostscompose.repository.CostsRepository
import java.time.format.DateTimeFormatter

@Composable
fun CostsScreen(costsRepository: CostsRepository, year: Int, month: Int) {
    LazyColumn(modifier = Modifier.padding(vertical = 2.dp)) {
        val costsPerMonth = costsRepository.database.costsDao().getAllCostsByMonth(year, month)

        items(items = costsPerMonth) { cost ->
            Surface(
                color = MaterialTheme.colors.primary,
                modifier = Modifier.padding(vertical = 1.dp)
            ) {
                Row(modifier = Modifier.padding(2.dp)) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text(fontSize = 16.sp,
                            text = "Date: "
                                    + cost.recordDateTime!!.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                                    + ", " + "Type: " + cost.type
                                    + "\nCosts: " + cost.costs
                                    + ", " + "Comment: " + cost.comment
                        )
                    }
                    OutlinedButton(modifier = Modifier.padding(0.dp),
                        onClick = {
                            costsRepository.deleteCostsByUniqueID(cost.uniqueID)
                        }
                    ) {
                        Text(fontSize = 16.sp, text = "D")
                    }
                }
            }
        }
    }
}