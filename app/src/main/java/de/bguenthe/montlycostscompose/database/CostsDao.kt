package de.bguenthe.montlycostscompose.database

import androidx.room.*

@Dao
interface CostsDao {
    @get:Query("select * from Costs order by recordDateTime")
    val getAll: List<Costs>

    @get:Query("select count(*) from Costs")
    val count: Long

    @get:Query("select * from Costs where mqttsend = 0")
    val allNotSentViaMqtt: List<Costs>

    @Query("select * from Costs where uniqueID = :uniqueID")
    fun getByUniqueID(uniqueID: String?): Costs

    @get:Query("select count(*) from (select count(*) from Costs where deleted = 0 group by strftime('%Y%m',recordDateTime / 1000, 'unixepoch'))")
    val numberOfMonthsToShow: Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(costs: Costs): Long

    @Query("select * from Costs where id = :taskId")
    fun getCost(taskId: Long): List<Costs>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(costs: Costs)

    @Query("delete from Costs")
    fun deleteAll()

    @Query("update costs set mqttsend = 0")
    fun resendAll()

    @Query("select * from Costs where id = (select max(id) from costs where deleted = 0 AND CAST(strftime('%Y', recordDateTime / 1000, 'unixepoch') as int) = :year and CAST(strftime('%m', recordDateTime / 1000, 'unixepoch') as int) = :month)")
    fun getLast(year: Int, month: Int): Costs?

    @Query("select * from Costs where deleted = 0 AND CAST(strftime('%Y', recordDateTime / 1000, 'unixepoch') as int) = :year and CAST(strftime('%m', recordDateTime / 1000, 'unixepoch') as int) = :month order by recordDateTime DESC")
    fun getAllCostsByMonth(year: Int, month: Int): List<Costs>

    @Query("select * from Costs where deleted = 0 AND CAST(strftime('%Y', recordDateTime / 1000, 'unixepoch') as int) = :year and CAST(strftime('%m', recordDateTime / 1000, 'unixepoch') as int) = :month and type = :type order by recordDateTime DESC")
    fun getAllCostsByMonthAndType(year: Int, month: Int, type: String): List<Costs>

    @Query("select sum(costs) from Costs where deleted = 0")
    fun getAllCostsSum(): Double

    @Query("select type, sum(costs) as value from Costs where deleted = 0 AND CAST(strftime('%Y', recordDateTime / 1000, 'unixepoch') as int) = :year and CAST(strftime('%m', recordDateTime / 1000, 'unixepoch') as int) = :month group by type")
    fun getSumsPerTypeForAGivenMonth(year: Int, month: Int): List<SumsPerType>

    @Query("select type, sum(costs) as value from Costs where deleted = 0 group by type")
    fun getSumsByType(): List<SumsPerType>

    @Query("select strftime('%Y%m', recordDateTime / 1000, 'unixepoch') from Costs where deleted = 0 group by strftime('%Y%m', recordDateTime / 1000, 'unixepoch')")
    fun getMonthCount(): List<String>

    @Query("select (strftime('%s', max(recordDateTime / 1000), 'unixepoch') - strftime('%s', min(recordDateTime / 1000), 'unixepoch')) /  86400.0  from Costs where deleted = 0")
    fun getDaysCount(): Double

    @Query("select 'sum' as type, sum(costs) as value from Costs where deleted = 0 AND CAST(strftime('%Y', recordDateTime / 1000, 'unixepoch') as int) = :year and CAST(strftime('%m', recordDateTime / 1000, 'unixepoch') as int) = :month")
    fun getMonthlySumOfCosts(year: Int, month: Int): List<SumsPerType>

    @Query("select uniqueID from Costs")
    fun getAllUUIds(): List<String>

    @Query("delete from Costs where uniqueID = :uniqueId")
    fun deleteByUniqueId(uniqueId: String?)
}