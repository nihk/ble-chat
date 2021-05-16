package nick.template.di

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.SharedPreferences
import android.location.LocationManager
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import ble.AndroidLocationStates
import nick.template.data.CurrentTime
import ble.LocationStates
import nick.template.data.SystemCurrentTime
import nick.template.ui.conversation.BluetoothConversationRepository
import ble.BluetoothIdentifiers
import ble.CharacteristicParser
import nick.template.ui.conversation.ConversationRepository
import ble.DefaultServiceDataConfig
import ble.ServiceDataConfig
import ble.advertising.AdvertisingConfig
import ble.advertising.AndroidBluetoothAdvertiser
import ble.advertising.BluetoothAdvertiser
import ble.advertising.DefaultAdvertisingConfig
import ble.connecting.AndroidBluetoothConnector
import ble.connecting.BluetoothConnector
import ble.scanning.AndroidBluetoothScanner
import ble.scanning.DefaultScanningConfig
import ble.scanning.BluetoothScanner
import nick.template.data.DefaultDeviceCacheThreshold
import ble.scanning.DefaultScanningTimeout
import nick.template.data.DeviceCacheThreshold
import ble.scanning.FirstResultBluetoothScanner
import ble.scanning.OneShotBluetoothScanner
import ble.scanning.ScanningConfig
import ble.scanning.ScanningTimeout
import ble.serving.AndroidBluetoothServer
import ble.serving.BluetoothServer
import ble.usability.AndroidBluetoothPermissions
import ble.usability.AndroidBluetoothStates
import ble.usability.BluetoothPermissions
import ble.usability.BluetoothStates
import ble.usability.BluetoothUsability
import ble.usability.DefaultBluetoothUsability
import nick.template.bluetooth.AppBluetoothIdentifiers
import nick.template.bluetooth.IdentifiableCharacteristicParser
import nick.template.data.local.AppDatabase
import nick.template.data.local.DeviceAndMessagesDao
import nick.template.ui.chatlist.BroadcastingServerRepository
import nick.template.ui.chatlist.ChatListRepository
import nick.template.ui.chatlist.ScanningChatListRepository
import nick.template.ui.chatlist.ServerRepository

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
        fun bluetoothGattService(identifiers: BluetoothIdentifiers): BluetoothGattService {
            val messageCharacteristic = BluetoothGattCharacteristic(
                identifiers.message,
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE
            )
            return BluetoothGattService(
                identifiers.service,
                BluetoothGattService.SERVICE_TYPE_PRIMARY
            ).apply {
                addCharacteristic(messageCharacteristic)
            }
        }

        @Provides
        fun locationManager(application: Application): LocationManager {
            return application.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }

        @Singleton
        @Provides
        fun appDatabase(application: Application): AppDatabase {
            return Room.databaseBuilder(
                application,
                AppDatabase::class.java,
                "app_database.db"
            ).build()
        }

        @Provides
        fun chatListItemDao(appDatabase: AppDatabase): DeviceAndMessagesDao {
            return appDatabase.chatListItemDao()
        }

        @Provides
        fun sharedPrefs(application: Application): SharedPreferences {
            return application.getSharedPreferences(
                "${application.packageName}_preferences",
                Context.MODE_PRIVATE
            )
        }
    }

    @Binds
    abstract fun chatListRepository(repository: ScanningChatListRepository): ChatListRepository

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
    abstract fun scanningConfig(scanningConfig: DefaultScanningConfig): ScanningConfig

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
    abstract fun bluetoothUuids(bluetoothUuids: AppBluetoothIdentifiers): BluetoothIdentifiers

    @Binds
    abstract fun bluetoothAdvertiser(bluetoothAdvertiser: AndroidBluetoothAdvertiser): BluetoothAdvertiser

    @Binds
    abstract fun advertisingConfig(advertisingConfig: DefaultAdvertisingConfig): AdvertisingConfig

    @Binds
    abstract fun serverRepository(serverRepository: BroadcastingServerRepository): ServerRepository

    @Binds
    abstract fun serviceDataConfig(serviceDataConfig: DefaultServiceDataConfig): ServiceDataConfig

    @Binds
    abstract fun characteristicParser(characteristicParser: IdentifiableCharacteristicParser): CharacteristicParser
}
