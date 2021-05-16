package ble.usability.ui

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

// Known (ancient) issue: dialog gets recreated on config change.
// See: https://stackoverflow.com/q/4873564/2997980
class TurnOnBluetooth : ActivityResultContract<Unit, Boolean>() {
    override fun createIntent(context: Context, input: Unit?): Intent {
        return Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        return resultCode == Activity.RESULT_OK
    }
}
