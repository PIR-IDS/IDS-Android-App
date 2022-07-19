package fr.pirids.idsapp.controller.api

import android.util.Log
import fr.pirids.idsapp.data.api.auth.ApiAuth
import fr.pirids.idsapp.data.api.auth.IzlyAuth
import fr.pirids.idsapp.data.api.data.IzlyData
import fr.pirids.idsapp.data.items.ServiceId
import kotlinx.coroutines.*
import org.jsoup.Connection
import org.jsoup.Connection.Response
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.HttpURLConnection
import java.sql.Timestamp
import java.time.LocalDate

class IzlyApi(credentials: ApiAuth, override val serviceId: ServiceId = ServiceId.IZLY) : ApiInterface {
    private val maxRetries = 50
    private lateinit var credentials: IzlyAuth
    private var cookieSessionId: String? = null
    private var cookieASPXAUTH: String? = null

    private val logonURL = "https://mon-espace.izly.fr/Home/Logon"
    private val historyURL = "https://mon-espace.izly.fr/History?page=1"

    init {
        authenticate(credentials)
    }

    override fun authenticate(credentials: ApiAuth) {
        (credentials as? IzlyAuth)?.let { this.credentials = it } ?: throw IllegalArgumentException("Credentials must be IzlyAuth")

        val firstRequestConnection = try { Jsoup.connect(logonURL).execute() } catch (e: HttpStatusException) { Log.e("IzlyApi", "Error while fetching URL: ", e) ; null }

        val cookieRequestVerificationToken = firstRequestConnection?.cookie("__RequestVerificationToken")
        val cookieApplicationGatewayAffinity = firstRequestConnection?.cookie("ApplicationGatewayAffinity")
        val cookieApplicationGatewayAffinityCORS = firstRequestConnection?.cookie("ApplicationGatewayAffinityCORS")
        cookieSessionId = firstRequestConnection?.cookie("ASP.NET_SessionId")
        val documentData = firstRequestConnection?.parse()

        // We need to get the __RequestVerificationToken from the page before we can logon
        // so we scrape it from the HTML
        val requestedToken = documentData
            ?.select("input[name=\"__RequestVerificationToken\"]")
            ?.toString()
            ?.substringAfter("value=")
            ?.replace("\"", "")
            ?.replace(">","")
            ?: ""

        // We need the .ASPXAUTH cookie in order to get the History page
        // so we connect to the logon page with the __RequestVerificationToken and get the cookie
        val loginConnection = Jsoup.connect(logonURL)
            .data("__RequestVerificationToken", requestedToken)
            .data("username", this.credentials.id)
            .data("password", this.credentials.password)
            .data("ReturnUrl", "/History/")

            // We use the headers generated from a random navigator in order to emulate a real browser
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
            .header("Accept-Encoding", "gzip, deflate, br")
            .header("Accept-Language", "en-US,en;q=0.5")
            .header("Connection", "keep-alive")
            .header("Content-Type","application/x-www-form-urlencoded")
            .header(
                "Cookie",
                "ApplicationGatewayAffinityCORS=" + (cookieApplicationGatewayAffinityCORS?.toString() ?: "") + "; " +
                        "ApplicationGatewayAffinity=" + (cookieApplicationGatewayAffinity?.toString() ?: "") + "; "+
                        "ASP.NET_SessionId=" + (cookieSessionId ?: "") + "; "+
                        "__RequestVerificationToken" + (cookieRequestVerificationToken?.toString() ?: "")
            )
            .header("Host", "mon-espace.izly.fr")
            .header("Origin", "https://mon-espace.izly.fr")
            .header("Referer", "https://mon-espace.izly.fr/Home/Logon?ReturnUrl=%2f")
            .header("Sec-Fetch-Dest","document")
            .header("Sec-Fetch-Mode", "navigate")
            .header("Sec-Fetch-Site", "same-site")
            .header("Sec-Fetch-User", "?1")
            .header("Upgrade-Insecure-Requests", "1")
            .header(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.75 Safari/537.36"
            )

            .cookie("__RequestVerificationToken", cookieRequestVerificationToken?.toString() ?: "")
            .cookie("ApplicationGatewayAffinity", cookieApplicationGatewayAffinity?.toString() ?: "")
            .cookie("ApplicationGatewayAffinityCORS", cookieApplicationGatewayAffinityCORS?.toString() ?: "")
            .cookie("ASP.NET_SessionId", cookieSessionId ?: "")

            .method(Connection.Method.POST)

        val firstLoginConnection = try { loginConnection.execute() } catch (e: HttpStatusException) { Log.e("IzlyApi", "Error while logging in: ", e) ; null }
        cookieASPXAUTH = firstLoginConnection?.cookie(".ASPXAUTH")
    }

    private suspend fun insistToGetHistoryConnection() : Response {
        var success = false
        var cptErr = 0
        lateinit var historyResponse: Response

        while (!success && cptErr < maxRetries) {
            cptErr++
            try {
                historyResponse = getHistoryConnection()
                success = true
                delay(1_000)
            } catch (e: HttpStatusException) {
                Log.d("IZLY", e.toString())
                if(cptErr >= maxRetries) {
                    throw e
                }
            }
        }

        return historyResponse
    }

    override suspend fun checkConnection(): Boolean =
        try { insistToGetHistoryConnection().statusCode() == HttpURLConnection.HTTP_OK } // HTTP 200, we don't want the 302 status which is obtained when we are not logged in
        catch(e: HttpStatusException) { e.message?.let { Log.d("IzlyApi", it) } ; false }
        catch(e: Exception) { e.message?.let { Log.d("IzlyApi", it) } ; false }

    //TODO: improve this function
    override suspend fun getData(): IzlyData {
        val timeOfEachAction: MutableList<Long> = mutableListOf()
        val historyData: Document = insistToGetHistoryConnection().parse()

        var success = false
        var nbrAction = 0
        var index = 1
        var cptErr = 0
        while (!success && cptErr < maxRetries) {
            cptErr++
            val history = historyData.select(".list-group.list-group-flush li:nth-child($index)")
            if (history.toString() == "") {
                success = true
            } else {
                if (history.toString().contains("You have made a payment to")) {
                    var date = history?.select(".operation-date").toString().substringAfter('>').substringBefore('<')
                    if (date.length < 25) {
                        //TODO: traiter le cas ou c'est today et yesterday
                        val day = date.substringBefore(' ')
                        date = date.substringAfter(" ").replace(" ", "").substringAfter("at")
                        val hour = date.substringBefore('h')
                        val minute = date.substringAfter('h')
                        val today: LocalDate = LocalDate.now()
                        val yesterday = today.minusDays(1)
                        if (day == "today"){
                            date = "$today $hour:$minute:00"
                            val time = Timestamp.valueOf(date)
                            timeOfEachAction.add(time.time)
                        }
                        if (day == "yesterday"){
                            date = "$yesterday $hour:$minute:00"
                            val time = Timestamp.valueOf(date)
                            timeOfEachAction.add(time.time)
                        }
                    } else {
                        date = date.substringAfter(',').substringAfter(' ')
                        var month = date.substringBefore(" ")
                        when (month) {
                            "January" -> month = "1"
                            "February" -> month = "2"
                            "March" -> month = "3"
                            "April" -> month = "4"
                            "May" -> month = "5"
                            "June" -> month = "6"
                            "July" -> month = "7"
                            "August" -> month = "8"
                            "September" -> month = "9"
                            "October" -> month = "10"
                            "November" -> month = "11"
                            "December" -> month = "12"
                        }
                        date = date.substringAfter(" ").replace(" ", "")
                        val day = date.substringBefore(',')
                        date = date.substringAfter(',')
                        val year = date.substringBefore("at")
                        date = date.substringAfter("at")
                        val hour = date.substringBefore('h')
                        val minute = date.substringAfter('h')
                        date = "$year-$month-$day $hour:$minute:00"
                        val time = Timestamp.valueOf(date)
                        timeOfEachAction.add(time.time)
                    }
                    nbrAction++
                }
            }
            index++
        }

        return IzlyData(transactionList = timeOfEachAction)
    }

    private fun getHistoryConnection() = Jsoup.connect(historyURL)
        .data("username", credentials.id)
        .data("password", credentials.password)
        .cookie(".ASPXAUTH", cookieASPXAUTH ?: "")
        .cookie("ASP.NET_SessionId", cookieSessionId ?: "")
        .cookie("_culture", "en-US")
        .header("Accept", "text/html, */*; q=0.01")
        .header(
            "Accept-Language",
            "en-US,fr;q=0.9,en;q=0.8,es;q=0.7,de;q=0.6,fr-FR;q=0.5"
        )
        .header("Cache-Control", "no-cache")
        .header("Connection", "keep-alive")
        .header("Pragma", "no-cache")
        .header("Referer", "https://mon-espace.izly.fr/History")
        .header("Sec-Fetch-Dest", "empty")
        .header("Sec-Fetch-Mode", "cors")
        .header("Sec-Fetch-Site", "same-origin")
        .header(
            "User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.75 Safari/537.36"
        )
        .header("X-Requested-With", "XMLHttpRequest")
        .header(
            "sec-ch-ua",
            "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"100\", \"Google Chrome\";v=\"100\""
        )
        .header("sec-ch-ua-mobile", "?0")
        .header("sec-ch-ua-platform", "\"Windows\"")
        .method(Connection.Method.GET)
        .execute()
}