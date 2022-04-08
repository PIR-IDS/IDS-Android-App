package fr.pirids.idsapp

import android.service.autofill.Validators.not
import android.widget.AutoCompleteTextView
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlPage
import fr.pirids.idsapp.utils.Constants
import org.jsoup.Connection
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import javax.xml.validation.Validator


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

        val connexionFirst = Jsoup.connect("https://mon-espace.izly.fr/Home/Logon")
            .data("username",Constants.NUM.toString())
            .data("password", Constants.MDP.toString())
            .data("ReturnUrl","/History/")
            .method(Connection.Method.POST)
            .execute()
        println("test1")
        val cookiesFirst = connexionFirst.cookie(".ASPXAUTH")
        val cookiesSecond  = connexionFirst.cookie("ASP.NET_SessionId")
        val document = connexionFirst.parse()
        println(cookiesFirst.toString())
        println(cookiesSecond.toString())

        Thread.sleep(1_000)




        //FIXME: se deconnecter apres avoir realiser la connexion
            var  succes: Boolean = false
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
                    val documentSecond = connexionSecond.parse()
                    val history = documentSecond.select("#userHistory")
                    println(documentSecond.toString())
                    succes = !succes
                    Thread.sleep(1_000)
                } catch (e: HttpStatusException) {
                    println(" Http 500 erreur !")
                }
            }


    }
}



     /*
        val connexion = Jsoup.connect("https://mon-espace.izly.fr/Home/Logon?username=0614099728&password=596489&ReturnUrl=/History/")
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







