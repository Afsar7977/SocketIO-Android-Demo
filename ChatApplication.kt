package com.afsar.githubrepo.StudentActivity

import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import java.net.URISyntaxException

class ChatApplication {
    private var mSocket: Socket? = null
    val socket: Socket?
        get() = mSocket

    init {
        mSocket = try {
            IO.socket("https://your_socket_url/")
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }
    }
}
