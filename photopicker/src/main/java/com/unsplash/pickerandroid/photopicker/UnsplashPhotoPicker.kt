package com.unsplash.pickerandroid.photopicker

/**
 * Configuration singleton object.
 */
object UnsplashPhotoPicker {

    private lateinit var accessKey: String

    private lateinit var secretKey: String

    private const val DEFAULT_PAGE_SIZE = 20

    private var pageSize: Int = DEFAULT_PAGE_SIZE

    fun init(accessKey: String, secretKey: String, pageSize: Int = DEFAULT_PAGE_SIZE) {
        this.accessKey = accessKey
        this.secretKey = secretKey
        this.pageSize = pageSize
    }

    fun getAccessKey(): String {
        return accessKey
    }

    fun getSecretKey(): String {
        return secretKey
    }

    fun getPageSize(): Int {
        return pageSize
    }
}
