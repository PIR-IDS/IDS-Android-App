package fr.pirids.idsapp.controller.api


import org.jsoup.Connection
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import java.sql.Timestamp
import java.time.LocalDate
import java.util.*


class IzlyApi : Api {

    var timeOfeachAction: LinkedList<Long> = LinkedList<Long>()

    override fun getTransactionList(number: String, password: String): LinkedList<Long> {

        val connexionTest = Jsoup.connect("https://mon-espace.izly.fr/Home/Logon").execute()

        timeOfeachAction =  LinkedList<Long>()
        val cookie_1 = connexionTest.cookie("__RequestVerificationToken")
        val cookie_2 = connexionTest.cookie("ApplicationGatewayAffinity")
        val cookie_3 = connexionTest.cookie("ApplicationGatewayAffinityCORS")
        val cookie_4 = connexionTest.cookie("ASP.NET_SessionId")
        val document_test = connexionTest.parse()
        var requested_token = document_test.select("input[name=\"__RequestVerificationToken\"]").toString()
        requested_token = requested_token.substringAfter("value=").replace("\"", "").replace(">","")


        val connexionFirst2 = Jsoup.connect("https://mon-espace.izly.fr/Home/Logon")
            .data("__RequestVerificationToken",requested_token.toString() )
            .data("username", number)
            .data("password", password)
            .data("ReturnUrl", "/History/")
            //.data("ReturnUrl", "/")
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
            .header("Accept-Encoding", "gzip, deflate, br")
            .header("Accept-Language", "en-US,en;q=0.5")
            .header("Connection", "keep-alive")
            .header("Content-Type","application/x-www-form-urlencoded")
            .header("Cookie","ApplicationGatewayAffinityCORS=" + cookie_3.toString() + "; " +
                    "ApplicationGatewayAffinity=" + cookie_2.toString() + "; "+
                    "ASP.NET_SessionId=" + cookie_4.toString() + "; "+
                    "__RequestVerificationToken" + cookie_1.toString() )
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
            .cookie("__RequestVerificationToken", cookie_1.toString())
            .cookie("ApplicationGatewayAffinity", cookie_2.toString())
            .cookie("ApplicationGatewayAffinityCORS", cookie_3.toString())
            .cookie("ASP.NET_SessionId", cookie_4.toString())
            .method(Connection.Method.POST)

        val connexionFirst = connexionFirst2.execute()
        var document = connexionFirst.parse()
        val cookiesFirst = connexionFirst.cookie(".ASPXAUTH")
        val cookiesSecond = cookie_4
        Thread.sleep(1_000)


        //FIXME: se deconnecter apres avoir realiser la connexion
        var succes: Boolean = false
        while (!succes) {
            try {
                val connexionSecond = Jsoup.connect("https://mon-espace.izly.fr/History?page=1")
                    .data("username", number)
                    .data("password", password)
                    .cookie(".ASPXAUTH", cookiesFirst.toString())
                    .cookie("ASP.NET_SessionId", cookiesSecond.toString())
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
                document = connexionSecond.parse()
                succes = !succes
                Thread.sleep(1_000)
            } catch (e: HttpStatusException) {
                println(" Http 500 erreur !")
            }
        }
        println("succes")
        succes = false
        var nbrAction = 0
        var index = 1
        while (!succes) {

            val history = document.select(".list-group.list-group-flush li:nth-child($index)")
            if (history.toString() == "") {
                succes = !succes
            } else {
                if (history.toString().contains("You have made a payment to")) {
                    var date = history.select(".operation-date").toString().substringAfter('>')
                        .substringBefore('<')
                    if (date.length < 25) {                        //todo: traiter le cas ou c'est today et yesterday
                        var day = date.substringBefore(' ')
                        date = date.substringAfter(" ").replace(" ", "").substringAfter("at")
                        val hour = date.substringBefore('h')
                        val minute = date.substringAfter('h')
                        val today: LocalDate = LocalDate.now()
                        val yesterday = today.minusDays(1)
                        if (day.equals("today")){
                            date = today.toString()+ " $hour:$minute:00"
                            val time = Timestamp.valueOf(date)
                            timeOfeachAction.add(time.time)
                        }
                        if (day.equals("yesterday")){
                            date = yesterday.toString()+ " $hour:$minute:00"
                            val time = Timestamp.valueOf(date)
                            timeOfeachAction.add(time.time)
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
                        timeOfeachAction.add(time.time)
                    }
                    nbrAction += 1
                }
            }
            index += 1
        }

        return timeOfeachAction
    }
}


/*
   val connexion = Jsoup.connect("")
       .method(Connection.Method.POST)
       .execute()
   val document = connexion.parse()
   val element = document.select(".nav-item.nav-link.mb-4.py-0")
   println(document.toString())*/


/*
        val webClient = WebClient()
        val myPage: HtmlPage = webClient.getPage("https://www.google.fr/")

        // convert page to generated HTML and convert to document

        // convert page to generated HTML and convert to document
        var doc = Jsoup.parse(myPage.asXml())
        println(doc.toString())*/







