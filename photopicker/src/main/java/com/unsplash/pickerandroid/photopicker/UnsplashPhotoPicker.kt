package com.unsplash.pickerandroid.photopicker

/**
 * Configuration singleton object.
 */
object UnsplashPhotoPicker {

    private lateinit var accessKey: String

    private lateinit var secretKey: String

    fun init(accessKey: String, secretKey: String) {
        this.accessKey = accessKey
        this.secretKey = secretKey
    }

    fun getAccessKey(): String {
        return accessKey
    }

    fun getSecretKey(): String {
        return secretKey
    }
}
