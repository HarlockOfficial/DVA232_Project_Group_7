package se.mdh.student.dva232.projectgroup7
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.text.Editable
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class Settings : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        title = "Settings"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        findViewById<Button>(R.id.button_send).setOnClickListener{
            val text = findViewById<EditText>(R.id.edit_feedback).text.toString()
            GlobalScope.launch {
                CommunicationLayer.addFeedback(text)
                findViewById<EditText>(R.id.edit_feedback).setText("")
                runOnUiThread {
                    Toast.makeText(this@Settings, R.string.sent_feedback, Toast.LENGTH_SHORT).show()
                }
            }
        }
        findViewById<Button>(R.id.button_clear).setOnClickListener{
            findViewById<EditText>(R.id.edit_feedback).setText("")

        }
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, SettingsFragment())
            .commit()
    }

    class SettingsFragment : PreferenceFragmentCompat() {


        private var mService: MusicService? = null
        private var mBound: Boolean = false

        private val connection = object : ServiceConnection {

            override fun onServiceConnected(className: ComponentName, service: IBinder) {
                val binder = service as MusicService.ServiceBinder
                mService = binder.getService()
                mBound = true
            }

            override fun onServiceDisconnected(arg0: ComponentName) {
                mBound = false
            }
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)

            getActivity()?.bindService(Intent(getActivity(), MusicService::class.java), connection, BIND_AUTO_CREATE)

            val myPref = preferenceScreen.findPreference<SwitchPreferenceCompat>("switch_preference_music")
            myPref?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->

                val isChecked = newValue as? Boolean ?: false
                if (isChecked) {
                    requireActivity().startService(Intent(activity, MusicService::class.java))
                } else {
                    requireActivity().stopService(Intent(activity, MusicService::class.java))
                    mService?.pauseMusic()
                }
                true
            }


        }

        override fun onPause() {
            super.onPause()
            mService?.pauseMusic()
        }

        override fun onResume() {
            super.onResume()

            if(PreferenceManager.getDefaultSharedPreferences(requireActivity()).getBoolean("switch_preference_music", true)) {
                requireActivity().startService(Intent(activity, MusicService::class.java))
            }
        }

    }
}