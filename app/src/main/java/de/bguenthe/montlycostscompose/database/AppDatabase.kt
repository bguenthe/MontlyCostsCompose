package de.bguenthe.montlycostscompose.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = arrayOf(Costs::class, Income::class), version = 10, exportSchema = false)
@TypeConverters(DateTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun costsDao(): CostsDao
    abstract fun incomeDao(): IncomeDao

    companion object {
        private var appDatabase: AppDatabase? = null

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    create table new_income (
                    id INTEGER PRIMARY KEY NOT NULL,
                    incomeDateTime INTEGER NOT NULL DEFAULT 0,
                    uniqueID TEXT NOT NULL DEFAULT '',
                    income REAL NOT NULL DEFAULT 0,
                    mqttsend INTEGER NOT NULL DEFAULT 0,
                    deleted INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                       INSERT into new_income (id, incomeDateTime, income)
                         select id, incomeDateTime, income from income
                    """.trimIndent()
                )
                database.execSQL(
                    """
                       drop table income
                    """.trimIndent()
                )
                database.execSQL(
                    """
                       ALTER TABLE new_income RENAME TO income
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            if (appDatabase == null) {
                appDatabase = Room.databaseBuilder(context, AppDatabase::class.java, "userdatabase")
                    .allowMainThreadQueries()
                    .addMigrations(MIGRATION_9_10, MIGRATION_9_10)
                    .build()
            }
            return appDatabase!!
        }
    }
}