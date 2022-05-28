package fr.pirids.idsapp.controller.api

import java.util.*

interface Api {

    // TODO fetch and init/connect functions in interface

    // TODO rename and remove parameteres to be more generic
    fun getTransactionList(number: String, password: String) : LinkedList<Long>
}