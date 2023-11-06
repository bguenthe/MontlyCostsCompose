package de.bguenthe.montlycostscompose.database

import androidx.room.*

@Dao
interface IncomeDao {
    @get:Query("select * from Income where mqttsend = 0")
    val allNotSentViaMqtt: List<Income>

    @get:Query("select count(*) from Income")
    val count: Long

    @get:Query("select * from income order by id")
    val getAll: List<Income>

    @Query("select * from income where uniqueID = :uniqueID")
    fun getByUniqueID(uniqueID: String?): Income

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(income: Income): Long

    @Query("select sum(income) from Income where deleted = 0")
    fun getAllIncomeSum(): Double

    @Query("delete from income")
    fun deleteAll()

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(income: Income)

    @Query("select * from income where deleted = 0 and CAST(strftime('%Y', incomeDateTime / 1000, 'unixepoch', 'localtime') as int) = :year and CAST(strftime('%m', incomeDateTime / 1000, 'unixepoch', 'localtime') as int) = :month")
    fun getMonthlyIncome(year: Int, month: Int): Income?

    @Query("delete from income where id in (1726,1727,1728,1729,1730,1731,1732,1733)")
    fun deleteById()

    @Query("delete from income where deleted = 1")
    fun deleteAllDeletedIncome()
}