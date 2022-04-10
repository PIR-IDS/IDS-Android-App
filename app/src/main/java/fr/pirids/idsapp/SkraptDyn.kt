package fr.pirids.idsapp


import fr.pirids.idsapp.utils.Constants
import org.jsoup.Connection
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import java.sql.Timestamp
import java.util.*


class Javanese {


    fun main() {
        /* Jsoup.connect("https://mon-espace.izly.fr").get().run {
             //2. Parses and scrapes the HTML response
             select(".container").forEachIndexed { index, element ->
                 val titleAnchor = element.select("section")
                 val title = titleAnchor.text() //3. Dumping Search Index, Title and URL on the stdout.
                 print("yo\n\n\n")
                 println("$index. $title ")
                 print("yo\n\n\n")
             }
             print("yo3\n\n\n")
         }*/
        val timeOfeachAction =  LinkedList<Long>()
        val connexionFirst = Jsoup.connect("https://mon-espace.izly.fr/Home/Logon")
            .data("username", Constants.NUM.toString())
            .data("password", Constants.MDP.toString())
            .data("ReturnUrl", "/History/")
            .method(Connection.Method.POST)
            .execute()
        println("test1")
        val cookiesFirst = connexionFirst.cookie(".ASPXAUTH")
        val cookiesSecond = connexionFirst.cookie("ASP.NET_SessionId")
        var document = connexionFirst.parse()
        println(cookiesFirst.toString())
        println(cookiesSecond.toString())

        Thread.sleep(1_000)


        //FIXME: se deconnecter apres avoir realiser la connexion
        var succes: Boolean = false
        while (!succes) {
            try {
                val connexionSecond = Jsoup.connect("https://mon-espace.izly.fr/History?page=1")
                    .data("username", Constants.NUM.toString())
                    .data("password", Constants.MDP.toString())
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
        succes = false
        var nbrAction = 0
        var index = 1
        while (!succes) {


            val history = document.select(".list-group.list-group-flush li:nth-child($index)")
            if (history.toString() == "") {
                println("done")
                succes = !succes
            } else {
                if (history.toString().contains("You have made a payment to")) {
                    var date = history.select(".operation-date").toString().substringAfter('>')
                        .substringBefore('<')
                    if (date.length < 25) {
                        //todo: traiter le cas ou c'est today et yesterday
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
                    //val date = Timestamp.valueOf("2014-08-18 8:19:15.0");
                    //println(date.time)
                }
            }
            index += 1
        }

    println(timeOfeachAction)
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







