package org.example.project

actual object TestConfig {
    actual fun isTestMode(): Boolean {
        return try {
            androidContext?.applicationInfo?.flags?.and(android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0 ||
            System.getProperty("maestro.test") == "true"
        } catch (e: Exception) {
            true
        }
    }
}
