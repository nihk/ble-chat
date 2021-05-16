package ble.scanning

import java.util.UUID

data class Scan(
    val address: String,
    val name: String?,
    val services: Map<UUID, ByteArray?>
)
