package com.example.smarcifier

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.bluetoothtest.BoboConnection
import com.example.smarcifier.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private var boboConnection: BoboConnection? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.nav_temp, R.id.nav_alarm, R.id.nav_settings), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        /*

        // Set up bluetooth state communication with settings fragment
        //
        // None of this works right now!
        val _foo = findViewById<NavigationView>(R.id.nav_view)
        assert(_foo != null);

        val fragManager: FragmentManager = supportFragmentManager;
        for (frag in fragManager.fragments) {
            Log.d("mylog", "Found fragment with id ${frag.id}");
        }
        val frag = fragManager.findFragmentById(R.id.nav_settings);
        if (frag != null) {
            Log.d("mylog", "Found the settings fragment! (id ${frag.id})");
        }
        //assert(frag != null);

         */
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun notifyBluetoothConnect(newCon: BoboConnection) {
        // Disconnect any existing connection
        boboConnection?.disconnect();

        // Set the new connection
        boboConnection = newCon;
        boboConnection?.setOnTemperatureChange { newTemp: Float ->
            val textView = findViewById<TextView>(R.id.textTemperature);
            textView?.post(Runnable() {
                textView.text = "%.1f".format(newTemp);
            })
        }

        Toast.makeText(
            this,
            "Successfully connected to Bo-Bo device.",
            Toast.LENGTH_LONG
        ).show();
    }
}