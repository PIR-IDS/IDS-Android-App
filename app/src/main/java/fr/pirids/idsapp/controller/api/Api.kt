package fr.pirids.idsapp.controller.api

import java.util.*

interface Api {

    fun getTransactionList(number: String, password: String) : LinkedList<Long>
}