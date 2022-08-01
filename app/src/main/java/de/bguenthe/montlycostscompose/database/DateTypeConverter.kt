package de.bguenthe.montlycostscompose.database

import androidx.room.TypeConverter

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.TimeZone

object DateTypeConverter {
    @TypeConverter
    @JvmStatic
    fun toDate(timestamp: Long?): LocalDateTime? {
        return if (timestamp == null) null else LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), TimeZone.getDefault().toZoneId())
    }

    @TypeConverter
    @JvmStatic
    fun toTimestamp(date: LocalDateTime?): Long? {
        return date?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    }
}
