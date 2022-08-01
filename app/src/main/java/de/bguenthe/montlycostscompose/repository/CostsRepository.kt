package de.bguenthe.montlycostscompose.repository

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.Log
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.lifecycle.MqttClientConnectedContext
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedContext
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult
import de.bguenthe.montlycostscompose.database.AppDatabase
import de.bguenthe.montlycostscompose.database.Costs
import de.bguenthe.montlycostscompose.database.Income
import de.bguenthe.montlycostscompose.database.SumsPerType
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.MonthDay
import java.time.Year
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.CompletableFuture

class Constants {
    data class Consts(var label: String, var color: Int, var comment: String)

    var liste: LinkedHashMap<String, Consts> = linkedMapOf()

    init {
        liste["lbm"] = Consts("\uD83C\uDF5E", Color.GREEN, "LBM")
        liste["allo"] = Consts("\uD83C\uDF7A", Color.DKGRAY, "ALLO")
        liste["sonst"] = Consts("\uD83D\uDE22", Color.MAGENTA, "SONST")
        liste["kaffee"] = Consts("\u2615", Color.BLACK, "KAF")
        liste["rest"] = Consts("\uD83C\uDF74", Color.RED, "REST")
        liste["beauty"] = Consts("\uD83D\uDC87", Color.YELLOW, "BEAUTY")
        liste["haushalt"] = Consts("\uD83C\uDFE0", Color.GRAY, "HAUS")
        liste["drinks"] = Consts("\uD83C\uDF78", Color.BLUE, "DRINKS")
    }
}

class CostsRepository {
    var clientconnected: Boolean = false
    val context: Context
    val constants = Constants()

    val database: AppDatabase
    val manmod = Build.MANUFACTURER + "_" + Build.MODEL
    var client: Mqtt5AsyncClient

    constructor(context: Context) {
        this.context = context
        this.database = AppDatabase.getDatabase(context)
        this.stringToDateTimelamda = { datetime: LocalDateTime ->
            val formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss") // // Format: 2018-03-22 19:02:12.337909
            datetime.format(formatter)
        }
        this.client = Mqtt5Client.builder()
            .identifier(UUID.randomUUID().toString())
            .addConnectedListener { context: MqttClientConnectedContext ->
                Log.e(
                    "BG*** connect listener: alles (wieder) gut", "nix"
                )
                this.clientconnected = true
                syncCostsIncomeMQTT()
            }
            .addDisconnectedListener { context: MqttClientDisconnectedContext ->
                val cause = context.cause
                if (cause is Mqtt5ConnAckException) {
                    Log.e(
                        "BG*** disconnect listener: Connect failed because of Negative CONNACK with code {} ",
                        cause.mqttMessage.toString()
                    )
                } else {
                    Log.e(
                        "BG*** disconnect listener: MQTT connect failed because of {}",
                        cause.message.toString()
                    )
                    this.clientconnected = false
                }
            }
            .automaticReconnectWithDefaultConfig()
            .serverHost("192.168.178.32")
            .buildAsync()
        client.connect() // ruft den Connect oder Disconnect Listener auf
    }

    // synchronize local with remote
    // IDEE hole all uuid local und remote,
    // 1.) mache uuid diff local - remote, es bleiben alle remote schlüssel, die holen und in die db einfügen
    // 2) mache uuids diff remote - local, es bleiben alle local schlüssel, diese senden
    // IST DIE PERFORMANT GENUG?
    // WANN RUFE ICH DIES AUF? NUR BEI WLAN?

    fun stringToDateTime(datetime: LocalDateTime): String {
        val formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss") // // Format: 2018-03-22 19:02:12.337909
        return datetime.format(formatter)
    }

    var stringToDateTimelamda: (LocalDateTime) -> String

    fun saveAllCostsToServerMQTT() {
        val allCosts = database.costsDao().getAll
        for (costs in allCosts) {
            saveCostsToMQTT(costs)
        }
    }

    fun deleteAllCosts() {
        database.costsDao().deleteAll()
    }

    fun getConstants(): LinkedHashMap<String, Constants.Consts> {
        return constants.liste
    }

    fun saveCostsToMQTT(costs: Costs): Boolean {
        val json = JSONObject()
        json.put("manmod", manmod)
        json.put("costs", costs.costs)
        json.put("comment", costs.comment)
        json.put("id", costs.id)
        json.put("recordDateTime", costs.recordDateTime)
        json.put("uniqueID", costs.uniqueID)
        json.put("type", costs.type)
        json.put("deleted", costs.deleted)
        val s = json.toString()
        if (clientconnected) {
            val publishResultFuture: CompletableFuture<Mqtt5PublishResult> =
                client.publishWith().topic("monthlycosts/clientcosts").qos(MqttQos.EXACTLY_ONCE)
                    .payload(s.toByteArray()).send()
            return true
        }
        return false
    }

    fun syncCostsIncomeMQTT() {
        val allCosts = database.costsDao().allNotSentViaMqtt
        for (costs in allCosts) {
            if (saveCostsToMQTT(costs) == true) {
                costs.mqttsend = true
                database.costsDao().update(costs)
            } else
                continue
        }

        val allIncomeNotSent = database.incomeDao().allNotSentViaMqtt
        for (income in allIncomeNotSent) {
            if (saveIncomeToMQTT(income) == true) {
                income.mqttsend = true
                database.incomeDao().update(income)
            } else
                continue
        }
    }

    fun sendMeServerCostsMQTT(): Boolean {
        if (clientconnected) {
            val publishResultFuture: CompletableFuture<Mqtt5PublishResult> =
                client.publishWith().topic("monthlycosts/sendmeservercosts").qos(MqttQos.EXACTLY_ONCE)
                    .payload("".toByteArray()).send()
            return true
        }
        return false
    }

    fun sendMeServerIncomeMQTT(): Boolean {
        if (clientconnected) {
            val publishResultFuture: CompletableFuture<Mqtt5PublishResult> =
                client.publishWith().topic("monthlycosts/sendmeserverincome").qos(MqttQos.EXACTLY_ONCE)
                    .payload("".toByteArray()).send()
            return true
        }
        return false
    }

    fun subscribeServerCostsMQTT() {
        var count = 0
        client.subscribeWith()
            .topicFilter("monthlycosts/servercosts")
            .qos(MqttQos.EXACTLY_ONCE)
            .callback { publish: Mqtt5Publish ->
                val res = StandardCharsets.UTF_8.decode(publish.payload.get()).toString()
                val json = JSONObject(res)
                val sdate = json.get("recordDateTime").toString()
                val localDateTime = LocalDateTime.parse(sdate)
                val costs = Costs(
                    json.get("type").toString(),
                    localDateTime,
                    json.getDouble("costs"),
                    json.get("comment").toString()
                )
                costs.uniqueID = json.get("uniqueID").toString()
                costs.deleted = json.getBoolean("deleted")
                //costs.id = json.getLong(("id"))
                costs.mqttsend = true
                database.costsDao().add(costs)
            }
            .send()
    }

    fun subscribeServerIncomeMQTT() {
        client.subscribeWith()
            .topicFilter("monthlycosts/serverincome")
            .qos(MqttQos.EXACTLY_ONCE)
            .callback { publish: Mqtt5Publish ->
                val res = StandardCharsets.UTF_8.decode(publish.payload.get()).toString()
                val json = JSONObject(res)
                val sdate = json.get("recordDateTime").toString()
                val localDateTime = LocalDateTime.parse(sdate)
                var income = Income(localDateTime, json.getDouble("income"))
                income.uniqueID = json.get("uniqueID").toString()
                income.deleted = json.getBoolean("deleted")
                //income.id = json.getLong("id")
                income.mqttsend = true
                database.incomeDao().add(income)
            }
            .send()
    }

    fun getNumberOfMonthsToShow(): Int {
        return database.costsDao().numberOfMonthsToShow
    }

    fun getAllCostsSum(): Double {
        return database.costsDao().getAllCostsSum()
    }

    fun getAllIncomeSum(): Double {
        return database.incomeDao().getAllIncomeSum()
    }

    fun saveCosts(type: String, costs: Costs) {
        syncCostsIncomeMQTT()
        costs.mqttsend = saveCostsToMQTT(costs)

        if (type == "NEW") {
            database.costsDao().add(costs)
        } else if (type == "DELETE") {
            database.costsDao().update(costs)
        }
    }

    fun saveCosts(type: String, value: String, bezeichnung: String) {
        val costs: Costs
        val amount = value.toDouble()
        val comment = bezeichnung
        if (value != "") {
            costs = Costs(type, LocalDateTime.now(), amount, comment)
            this.saveCosts("NEW", costs)
        }
    }

    fun undo() {
        val costs = database.costsDao().getLast(Year.now().value, MonthDay.now().month.value)
        if (costs == null) {
            return // kein Eintrag zum Löschen
        }
        costs.deleted = true
        saveCosts("DELETE", costs)
    }

    fun getMonthlySumsPerType(year: Int, month: Int): List<SumsPerType> {
        return database.costsDao().getSumsPerTypeForAGivenMonth(year, month)
    }

    fun getNumberOfStoredCosts(): Long {
        return database.costsDao().count
    }

    fun deleteCostsByUniqueID(uniqueID: String?) {
        val costs = database.costsDao().getByUniqueID(uniqueID)
        costs.deleted = true
        saveCosts("DELETE", costs)
    }

    // INCOME
    fun saveIncome(amount: Double) {
        val localDate = LocalDateTime.of(LocalDateTime.now().year, LocalDateTime.now().month, 1, 0, 0, 0)
        val monthIncome =
            database.incomeDao().getMonthlyIncome(LocalDateTime.now().year, LocalDateTime.now().monthValue)
        if (monthIncome == null) { // neu
            val income = Income(localDate, amount)
            income.mqttsend = saveIncomeToMQTT(income)
            database.incomeDao().add(income)
        } else { // income update
            // altes auf dem server löschen
            monthIncome.deleted = true
            monthIncome.mqttsend = saveIncomeToMQTT(monthIncome)
            database.incomeDao().update(monthIncome)

            // neues schreiben
            val income = Income(localDate, amount)
            income.mqttsend = saveIncomeToMQTT(income)
            database.incomeDao().add(income)
            val i = 0
        }
    }

    fun saveIncomeToMQTT(income: Income): Boolean {
        val json = JSONObject()
        json.put("manmod", manmod)
        json.put("id", income.id)
        json.put("income", income.income)
        json.put("recordDateTime", income.incomeDateTime)
        json.put("uniqueID", income.uniqueID)
        json.put("deleted", income.deleted)
        val s = json.toString()
        if (clientconnected) {
            val publishResultFuture: CompletableFuture<Mqtt5PublishResult> =
                client.publishWith().topic("monthlycosts/clientincome").qos(MqttQos.EXACTLY_ONCE)
                    .payload(s.toByteArray()).send()
            return true
        }
        return false
    }

    fun saveAllIncomeToServerMQTT() {
        val allIncome = database.incomeDao().getAll
        for (income in allIncome) {
            saveIncomeToMQTT(income)
        }
    }

    fun deleteAllIncome() {
        database.incomeDao().deleteAll()
    }

    fun getMonthlySumOfCosts(year: Int, month: Int): List<SumsPerType> {
        return database.costsDao().getMonthlySumOfCosts(year, month)
    }

    fun getMonthlyIncome(year: Int, month: Int): Income? {
        return database.incomeDao().getMonthlyIncome(year, month)
    }

    fun getNumberOfStoresIncomes(): Long {
        return database.incomeDao().count
    }
}