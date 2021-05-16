package ble

internal fun <T> requireBle(t: T?): T {
    return requireNotNull(t) {
        "Either Bluetooth wasn't turned on or relevant permissions weren't actively granted!"
    }
}
