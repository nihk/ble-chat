package nick.template.di

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import nick.template.data.AndroidBluetoothConnector
import nick.template.data.AndroidBluetoothPermissions
import nick.template.data.DefaultBluetoothRepository
import nick.template.data.AndroidBluetoothScanner
import nick.template.data.AndroidBluetoothStates
import nick.template.data.BluetoothPermissions
import nick.template.data.BluetoothRepository
import nick.template.data.BluetoothScanner
import nick.template.data.BluetoothStates
import nick.template.data.BatchedScanningConfig
import nick.template.data.BluetoothConnector
import nick.template.data.ScanningConfig

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    companion object {
        @Provides
        fun bluetoothManager(application: Application): BluetoothManager {
            return application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        }

        @Provides
        fun bluetoothAdapter(bluetoothManager: BluetoothManager): BluetoothAdapter {
            // fixme: use BluetoothAdapter.getDefaultAdapter() instead?
            return bluetoothManager.adapter
        }
    }

    @Binds
    abstract fun bluetoothRepository(repository: DefaultBluetoothRepository): BluetoothRepository

    @Binds
    abstract fun bluetoothStates(bluetoothStates: AndroidBluetoothStates): BluetoothStates

    @Binds
    abstract fun bluetoothScanner(bluetoothScanner: AndroidBluetoothScanner): BluetoothScanner

    @Binds
    abstract fun bluetoothPermissions(bluetoothPermissions: AndroidBluetoothPermissions): BluetoothPermissions

    @Binds
    abstract fun scanningConfig(scanningConfig: BatchedScanningConfig): ScanningConfig

    @Binds
    abstract fun bluetoothConnector(bluetoothConnector: AndroidBluetoothConnector): BluetoothConnector
}
