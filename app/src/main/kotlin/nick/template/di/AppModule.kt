package nick.template.di

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import nick.template.data.CurrentTime
import nick.template.data.SystemCurrentTime
import nick.template.data.bluetooth.AndroidBluetoothConnector
import nick.template.data.bluetooth.AndroidBluetoothPermissions
import nick.template.data.bluetooth.AndroidBluetoothScanner
import nick.template.data.bluetooth.AndroidBluetoothStates
import nick.template.data.bluetooth.BatchedScanningConfig
import nick.template.data.bluetooth.BluetoothConnector
import nick.template.data.bluetooth.BluetoothPermissions
import nick.template.data.bluetooth.BluetoothRepository
import nick.template.data.bluetooth.BluetoothScanner
import nick.template.data.bluetooth.BluetoothStates
import nick.template.data.bluetooth.DefaultBluetoothRepository
import nick.template.data.bluetooth.ScanningConfig
import nick.template.data.local.AppDatabase
import nick.template.data.local.DeviceDao

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

        @Provides
        fun appDatabase(application: Application): AppDatabase {
            return Room.databaseBuilder(
                application,
                AppDatabase::class.java,
                "app_database.db"
            ).build()
        }

        @Provides
        fun deviceDao(appDatabase: AppDatabase): DeviceDao {
            return appDatabase.deviceDao()
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

    @Binds
    abstract fun currentTime(currentTime: SystemCurrentTime): CurrentTime
}
