package org.example.project

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

class MockWebServerService : Service() {
    private var mockWebServer: MockWebServer? = null
    private val binder = LocalBinder()
    private var listener: OnServerReadyListener? = null
    private var isServerReady = false

    interface OnServerReadyListener {
        fun onServerReady()
    }

    inner class LocalBinder : Binder() {
        fun getService(): MockWebServerService = this@MockWebServerService
    }

    fun setServerReadyListener(listener: OnServerReadyListener) {
        this.listener = listener
        if (isServerReady) {
            listener.onServerReady()
        }
    }

    companion object {
        private const val TAG = "MockWebServerService"
        private const val PORT = 8080
        private val MOCK_POKEMON_RESPONSE = """
        {
          "pokedex_id": "25",
          "category": "Souris",
          "name": { "fr": "Pikachu", "en": "Pikachu", "jp": "ピカチュウ" },
          "sprites": {
            "regular": "https://raw.githubusercontent.com/Yarkis01/TyraDex/images/sprites/regular/25.png",
            "shiny": "https://raw.githubusercontent.com/Yarkis01/TyraDex/images/sprites/shiny/25.png",
            "gmax": null
          }
        }
        """.trimIndent()
    }

    override fun onCreate() {
        super.onCreate()
        startMockWebServer()
    }

    private fun startMockWebServer() {
        Thread {
            try {
                mockWebServer = MockWebServer().apply {
                    dispatcher = object : okhttp3.mockwebserver.Dispatcher() {
                        override fun dispatch(request: okhttp3.mockwebserver.RecordedRequest): MockResponse {
                            Log.d(TAG, "Requête reçue: ${request.path}")
                            return if (request.path?.contains("/api/v1/pokemon/") == true) {
                                MockResponse()
                                    .setResponseCode(200)
                                    .setBody(MOCK_POKEMON_RESPONSE)
                                    .setHeader("Content-Type", "application/json")
                            } else {
                                MockResponse().setResponseCode(404)
                            }
                        }
                    }
                    start(PORT)
                }
                Thread.sleep(100)
                isServerReady = true
                val hostName = mockWebServer?.hostName ?: "localhost"
                val port = mockWebServer?.port ?: PORT
                Log.d(TAG, "MockWebServer démarré sur $hostName:$port")
                listener?.onServerReady()
            } catch (e: Exception) {
                Log.e(TAG, "Erreur lors du démarrage de MockWebServer", e)
                isServerReady = false
            }
        }.start()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        Thread {
            try {
                mockWebServer?.shutdown()
                Log.d(TAG, "MockWebServer arrêté")
            } catch (e: Exception) {
                Log.e(TAG, "Erreur lors de l'arrêt de MockWebServer", e)
            }
        }.start()
    }
}
