package nick.template.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface BluetoothRepository {
    fun scanningResults(): Flow<BluetoothScanner.Result>
}

class AndroidBluetoothRepository @Inject constructor(
    private val bluetoothScanner: BluetoothScanner
) : BluetoothRepository {
    override fun scanningResults(): Flow<BluetoothScanner.Result> = bluetoothScanner.results()
}
