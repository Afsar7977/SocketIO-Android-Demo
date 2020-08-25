@file:Suppress(
    "HasPlatformType", "SpellCheckingInspection",
    "PackageName", "unused",
    "PrivatePropertyName"
)

package com.afsar.githubrepo.StudentActivity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afsar.githubrepo.HomePage
import com.afsar.githubrepo.Modal.CMessage
import com.afsar.githubrepo.R
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.Socket
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_student_chat.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class StudentChat : AppCompatActivity() {

    private lateinit var chat_back: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var sAdapter: CustomAdapter1
    private var mList: ArrayList<CMessage> = ArrayList()
    private lateinit var mSocket: Socket

    private val onNewMessage = Emitter.Listener { args ->
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val data = args[0] as JSONObject
                    try {
                        Log.d("data", data.toString())
                        addData(data)
                    } catch (e: JSONException) {
                        Log.d("Nerror", e.toString())
                    }
                } catch (e: Exception) {
                    Log.d("eNerror", e.toString())
                }
            }
        }
    }

    private val online = Emitter.Listener {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    mSocket.on("is_online") { args ->
                        val data = args[0] as JSONObject
                        Log.d("online", data.toString())
                    }
                } catch (e: Exception) {
                    Log.d("error", e.toString())
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_chat)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        recyclerView = findViewById(R.id.chat_recycler_view)
        chat_back = findViewById(R.id.chat_back)
        chat_back.setOnClickListener {
            val intent = Intent(applicationContext, HomePage::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
        val randomflag = UUID.randomUUID().toString().subSequence(0, 2).toString()
        random = "Lorem $randomflag"
        try {
            val app = ChatApplication()
            mSocket = app.socket!!
            mSocket.emit("connection", (arrayOf(random, "test")))
            mSocket.on("chat_message", onNewMessage)
            mSocket.on("is_online", online)
            mSocket.connect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        chatsend.setOnClickListener {
            try {
                val txt = chat_edittxt.text.toString().trim()
                val jsonObject = JSONObject()
                jsonObject.put("chat_message", txt)
                jsonObject.put("user_name", random)
                mSocket.emit("chat_message", jsonObject)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        recyclerView.apply {
            val linearLayoutManager =
                LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
            recyclerView.layoutManager = linearLayoutManager
            sAdapter = CustomAdapter1(mList, applicationContext)
            recyclerView.adapter = sAdapter
            sAdapter.notifyDataSetChanged()
        }
    }

    class CustomAdapter1(private val slist: ArrayList<CMessage>, val context: Context) :
        RecyclerView.Adapter<CustomAdapter1.ViewHolder>() {
        private val CHAT_MINE = 0
        private val CHAT_PARTNER = 1

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            var view: View? = null
            when (viewType) {
                0 -> {
                    view =
                        LayoutInflater.from(context)
                            .inflate(R.layout.chat_student_item, parent, false)
                    Log.d("user inflating", "viewType : $viewType")
                }

                1 -> {
                    view = LayoutInflater.from(context)
                        .inflate(R.layout.chat_partner_item, parent, false)
                    Log.d("partner inflating", "viewType : $viewType")
                }
            }
            return ViewHolder(view!!)
        }

        override fun getItemViewType(position: Int): Int {
            return slist[position].viewType
        }

        override fun getItemCount(): Int {
            return slist.size
        }

        @Suppress("UNUSED_VARIABLE", "LocalVariableName")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val data = slist[position]
            val chat_msg = data.chat_message
            val username = data.user_name
            when (data.viewType) {
                CHAT_MINE -> {
                    holder.ctxt.text = chat_msg
                }
                CHAT_PARTNER -> {
                    holder.ctxt.text = chat_msg
                }
            }
        }

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val ctxt = itemView.findViewById<TextView>(R.id.chat__item_txt)
            val cimage = itemView.findViewById<CircleImageView>(R.id.chat_profile)
        }
    }

    private fun addData(jsonObject: JSONObject) {
        try {
            runOnUiThread {
                Log.d("addData", "->:$jsonObject")
                val cMessage: CMessage
                val messageObject: JSONObject = jsonObject.optJSONObject("message")!!
                val cMsg = messageObject.optString("chat_message")
                val userN = messageObject.optString("user_name")
                cMessage = when (userN) {
                    random -> {
                        CMessage(cMsg.toString(), userN.toString(), "0".toInt())
                    }
                    else -> {
                        CMessage(cMsg.toString(), userN.toString(), "1".toInt())
                    }
                }
                mList.add(cMessage)
                sAdapter.notifyDataSetChanged()
                recyclerView.scrollToPosition(mList.size - 1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private lateinit var random: String
    }
}
