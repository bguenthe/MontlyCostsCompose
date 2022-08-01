package de.bguenthe.montlycostscompose.database

import androidx.room.Entity
import androidx.room.PrimaryKey

import java.time.LocalDateTime
import java.util.UUID

@Entity
class Costs(var type: String?, var recordDateTime: LocalDateTime?, var costs: Double, var comment: String?) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var uniqueID: String?
    var mqttsend = false
    var deleted = false

    init {
        this.uniqueID = UUID.randomUUID().toString()
    }
}
