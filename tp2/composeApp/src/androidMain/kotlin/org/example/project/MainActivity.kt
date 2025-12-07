package org.example.project

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity(), MockWebServerService.OnServerReadyListener {
    private var mockWebServerService: MockWebServerService? = null
    private var serviceConnection: ServiceConnection? = null
    private var isServerReady by mutableStateOf(false)

    override fun onServerReady() {
        runOnUiThread {
            isServerReady = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        androidContext = this

        if (TestConfig.isTestMode()) {
            startMockWebServerService()
        } else {
            isServerReady = true
        }

        setContent {
            if (isServerReady) {
                App()
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Démarrage du serveur...")
                }
            }
        }
    }

    private fun startMockWebServerService() {
        val intent = Intent(this, MockWebServerService::class.java)
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as MockWebServerService.LocalBinder
                mockWebServerService = binder.getService()
                mockWebServerService?.setServerReadyListener(this@MainActivity)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                mockWebServerService = null
            }
        }
        startService(intent)
        bindService(intent, serviceConnection!!, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceConnection?.let {
            unbindService(it)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}