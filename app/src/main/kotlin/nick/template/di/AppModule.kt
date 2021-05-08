package nick.template.di

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.content.Context
import android.location.LocationManager
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import nick.template.data.AndroidLocationStates
import nick.template.data.CurrentTime
import nick.template.data.LocationStates
import nick.template.data.SystemCurrentTime
import nick.template.data.bluetooth.AdvertiseConfig
import nick.template.data.bluetooth.AndroidBluetoothAdvertiser
import nick.template.data.bluetooth.AndroidBluetoothConnector
import nick.template.data.bluetooth.AndroidBluetoothPermissions
import nick.template.data.bluetooth.AndroidBluetoothScanner
import nick.template.data.bluetooth.AndroidBluetoothStates
import nick.template.data.bluetooth.DefaultBluetoothUsability
import nick.template.data.bluetooth.BatchedScanningConfig
import nick.template.data.bluetooth.BluetoothConnector
import nick.template.data.bluetooth.BluetoothConversationRepository
import nick.template.data.bluetooth.BluetoothPermissions
import nick.template.data.bluetooth.ScanningRepository
import nick.template.data.bluetooth.BluetoothScanner
import nick.template.data.bluetooth.BluetoothServer
import nick.template.data.bluetooth.BluetoothStates
import nick.template.data.bluetooth.BluetoothUsability
import nick.template.data.bluetooth.ConversationRepository
import nick.template.data.bluetooth.AndroidBluetoothServer
import nick.template.data.bluetooth.BluetoothAdvertiser
import nick.template.data.bluetooth.BluetoothUuids
import nick.template.data.bluetooth.ChatBluetoothUuids
import nick.template.data.bluetooth.DefaultAdvertiseConfig
import nick.template.data.bluetooth.DefaultScanningRepository
import nick.template.data.bluetooth.DefaultDeviceCacheThreshold
import nick.template.data.bluetooth.DefaultScanningTimeout
import nick.template.data.bluetooth.DeviceCacheThreshold
import nick.template.data.bluetooth.FirstResultBluetoothScanner
import nick.template.data.bluetooth.OneShotBluetoothScanner
import nick.template.data.bluetooth.ScanningConfig
import nick.template.data.bluetooth.ScanningTimeout
import nick.template.data.local.AppDatabase
import nick.template.data.local.DeviceDao
import nick.template.data.local.MessageDao

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
            return bluetoothManager.adapter
        }

        @Provides
        fun bluetoothGattService(uuids: BluetoothUuids): BluetoothGattService {
            val messageCharacteristic = BluetoothGattCharacteristic(
                uuids.message,
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE
            )
            val confirmCharacteristic = BluetoothGattCharacteristic(
                uuids.confirmConnection,
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE
            )
            return BluetoothGattService(
                uuids.service,
                BluetoothGattService.SERVICE_TYPE_PRIMARY
            ).apply {
                addCharacteristic(messageCharacteristic)
                addCharacteristic(confirmCharacteristic)
            }
        }

        @Provides
        fun locationManager(application: Application): LocationManager {
            return application.getSystemService(Context.LOCATION_SERVICE) as LocationManager
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

        @Provides
        fun messageDao(appDatabase: AppDatabase): MessageDao {
            return appDatabase.messageDao()
        }
    }

    @Binds
    abstract fun scanningRepository(repository: DefaultScanningRepository): ScanningRepository

    @Binds
    abstract fun conversationRepository(repository: BluetoothConversationRepository): ConversationRepository

    @Binds
    abstract fun bluetoothStates(bluetoothStates: AndroidBluetoothStates): BluetoothStates

    @Binds
    abstract fun bluetoothScanner(bluetoothScanner: AndroidBluetoothScanner): BluetoothScanner

    @Binds
    abstract fun oneShotBluetoothScanner(oneShotBluetoothScanner: FirstResultBluetoothScanner): OneShotBluetoothScanner

    @Binds
    abstract fun bluetoothPermissions(bluetoothPermissions: AndroidBluetoothPermissions): BluetoothPermissions

    @Binds
    abstract fun scanningConfig(scanningConfig: BatchedScanningConfig): ScanningConfig

    @Binds
    abstract fun scanningTimeout(scanningTimeout: DefaultScanningTimeout): ScanningTimeout

    @Binds
    abstract fun bluetoothConnector(bluetoothConnector: AndroidBluetoothConnector): BluetoothConnector

    @Binds
    abstract fun currentTime(currentTime: SystemCurrentTime): CurrentTime

    @Binds
    abstract fun bluetoothUsability(bluetoothUsability: DefaultBluetoothUsability): BluetoothUsability

    @Binds
    abstract fun locationStates(locationStates: AndroidLocationStates): LocationStates

    @Binds
    abstract fun deviceCacheThreshold(deviceCacheThreshold: DefaultDeviceCacheThreshold): DeviceCacheThreshold

    @Binds
    abstract fun bluetoothServer(bluetoothServer: AndroidBluetoothServer): BluetoothServer

    @Binds
    abstract fun bluetoothUuids(bluetoothUuids: ChatBluetoothUuids): BluetoothUuids

    @Binds
    abstract fun bluetoothAdvertiser(bluetoothAdvertiser: AndroidBluetoothAdvertiser): BluetoothAdvertiser

    @Binds
    abstract fun advertiseConfig(advertiseConfig: DefaultAdvertiseConfig): AdvertiseConfig
}
