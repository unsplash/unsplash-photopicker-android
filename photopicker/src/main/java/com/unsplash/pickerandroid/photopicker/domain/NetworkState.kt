package com.unsplash.pickerandroid.photopicker.domain

/*
This has been copied from the Android paging library sample
 */

enum class Status {
    LOADING,
    SUCCESS,
    EMPTY,
    FAILURE
}

@Suppress("DataClassPrivateConstructor")
data class NetworkState private constructor(
    val status: Status,
    val isEmpty: Boolean? = false,
    val msg: String? = null
) {
    companion object {
        val SUCCESS = NetworkState(Status.SUCCESS, isEmpty = false)
        val EMPTY = NetworkState(Status.EMPTY, isEmpty = true)
        val LOADING = NetworkState(Status.LOADING)
        fun error(msg: String?) = NetworkState(Status.FAILURE, msg = msg)
    }
}
