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
import nick.template.data.bluetooth.advertising.AdvertisingConfig
import nick.template.data.bluetooth.advertising.AdvertisingRepository
import nick.template.data.bluetooth.advertising.AndroidBluetoothAdvertiser
import nick.template.data.bluetooth.connecting.AndroidBluetoothConnector
import nick.template.data.bluetooth.usability.AndroidBluetoothPermissions
import nick.template.data.bluetooth.scanning.AndroidBluetoothScanner
import nick.template.data.bluetooth.usability.AndroidBluetoothStates
import nick.template.data.bluetooth.usability.DefaultBluetoothUsability
import nick.template.data.bluetooth.scanning.AppScanningConfig
import nick.template.data.bluetooth.connecting.BluetoothConnector
import nick.template.data.bluetooth.BluetoothConversationRepository
import nick.template.data.bluetooth.usability.BluetoothPermissions
import nick.template.data.bluetooth.scanning.BluetoothScanner
import nick.template.data.bluetooth.serving.BluetoothServer
import nick.template.data.bluetooth.usability.BluetoothStates
import nick.template.data.bluetooth.usability.BluetoothUsability
import nick.template.data.bluetooth.ConversationRepository
import nick.template.data.bluetooth.serving.AndroidBluetoothServer
import nick.template.data.bluetooth.advertising.BluetoothAdvertiser
import nick.template.data.bluetooth.BluetoothUuids
import nick.template.data.bluetooth.AppBluetoothUuids
import nick.template.data.bluetooth.advertising.DefaultAdvertisingConfig
import nick.template.data.bluetooth.advertising.DefaultAdvertisingRepository
import nick.template.data.bluetooth.scanning.DefaultDeviceCacheThreshold
import nick.template.data.bluetooth.scanning.DefaultScanningTimeout
import nick.template.data.bluetooth.scanning.DeviceCacheThreshold
import nick.template.data.bluetooth.scanning.FirstResultBluetoothScanner
import nick.template.data.bluetooth.scanning.OneShotBluetoothScanner
import nick.template.data.bluetooth.scanning.ScanningConfig
import nick.template.data.bluetooth.scanning.ScanningTimeout
import nick.template.data.local.AppDatabase
import nick.template.data.local.DeviceAndMessagesDao
import nick.template.data.local.DeviceDao
import nick.template.data.local.MessageDao
import nick.template.ui.chatlist.BluetoothChatListRepository
import nick.template.ui.chatlist.ChatListRepository

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

        @Provides
        fun chatListItemDao(appDatabase: AppDatabase): DeviceAndMessagesDao {
            return appDatabase.chatListItemDao()
        }
    }

    @Binds
    abstract fun chatListRepository(repository: BluetoothChatListRepository): ChatListRepository

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
    abstract fun scanningConfig(scanningConfig: AppScanningConfig): ScanningConfig

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
    abstract fun bluetoothUuids(bluetoothUuids: AppBluetoothUuids): BluetoothUuids

    @Binds
    abstract fun bluetoothAdvertiser(bluetoothAdvertiser: AndroidBluetoothAdvertiser): BluetoothAdvertiser

    @Binds
    abstract fun advertisingConfig(advertisingConfig: DefaultAdvertisingConfig): AdvertisingConfig

    @Binds
    abstract fun advertisingRepository(advertisingRepository: DefaultAdvertisingRepository): AdvertisingRepository
}
