package ble.usability.ui

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContract

class OpenLocationSettings : ActivityResultContract<Unit, Unit>() {
    override fun createIntent(context: Context, input: Unit?): Intent {
        return Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    }

    override fun parseResult(resultCode: Int, intent: Intent?) {
        // Nothing useful here.
    }
}
