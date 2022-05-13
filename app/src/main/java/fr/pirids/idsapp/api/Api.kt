package fr.pirids.idsapp.api

import java.util.*

interface Api {

    fun GetTransactionList() : LinkedList<Long>
}