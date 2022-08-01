package de.bguenthe.montlycostscompose.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull

import java.time.LocalDateTime
import java.util.*

@Entity
class Income(
    @NotNull var incomeDateTime: LocalDateTime,
    @NotNull var income: Double
) {
    @PrimaryKey(autoGenerate = true)
    @NotNull
    var id: Long = 0

    @NotNull
    var uniqueID: String = UUID.randomUUID().toString()

    @NotNull
    var mqttsend = false

    @NotNull
    var deleted = false
}