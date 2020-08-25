package com.amuze.learnfromhome.StudentActivity

import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import java.net.URISyntaxException

class ChatApplication {
    private var mSocket: Socket? = null
    val socket: Socket?
        get() = mSocket

    init {
        mSocket = try {
            IO.socket("https://chatapp-ejs.herokuapp.com/")
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }
    }
}