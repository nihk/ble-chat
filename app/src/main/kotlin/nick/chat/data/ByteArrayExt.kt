package nick.chat.data

fun ByteArray.stringify(): String {
    return String(this, Charsets.ISO_8859_1)
}

fun String.bytify(): ByteArray {
    return toByteArray(Charsets.ISO_8859_1)
}
