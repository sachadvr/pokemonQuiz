package org.example.project

actual object TestConfig {
    actual fun isTestMode(): Boolean {
        return false
    }
}
