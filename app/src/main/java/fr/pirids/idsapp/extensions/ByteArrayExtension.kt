package fr.pirids.idsapp.extensions

@OptIn(ExperimentalUnsignedTypes::class)
fun ByteArray.toHexString() : String = asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }