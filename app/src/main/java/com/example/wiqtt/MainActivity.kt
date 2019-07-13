package com.example.wiqtt

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wiqtt.mqtt.*
import com.example.wiqtt.mqtt.entities.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    IMqttConnectListener {

    private val TAG = "WiQTT"

    private val mClientId = "wiqtt-client"

    private lateinit var mMqttClient: MqttClient

    private val mBrokers = mutableListOf<Broker>()
    private lateinit var mMessageListAdapter: MessageListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            publishMessage()
            Snackbar.make(view, "Message sending", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)

        val message = MessageEntity()
        message.setTopic("cmnd/room_light/power")
        message.setPayload("toggle")

        mMessageListAdapter =
            MessageListAdapter(mutableListOf(message, message, message, message, message, message, message))

        val messageRecyclerView: RecyclerView = findViewById(R.id.message_recycler_view)
        messageRecyclerView.apply {
            adapter = mMessageListAdapter
        }

        loadBrokers()

        mMqttClient = MqttClient(
            applicationContext, mClientId, this,
            object : IMqttPublishListener {
                override fun onPublishSuccess(message: Message) {
                    Log.i(TAG, "Message published")
                }

                override fun onPublishFailed(message: Message, exception: Throwable?) {
                    Log.e(TAG, "Message not published, reason ${exception?.message}")
                }
            })
    }

    private fun showToast(text: String) {
        val toast = Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT)
        toast.show()
    }

    override fun onConnectSuccess(broker: Broker) {
        showToast("Successfully connected to ${broker.name}.")
        val brokerNameTextView: TextView = findViewById(R.id.nav_broker_name)
        brokerNameTextView.text = broker.name

        val brokerIpTextView: TextView = findViewById(R.id.nav_broker_uri)
        brokerIpTextView.text = brokerAsUri(broker)
    }

    override fun onConnectFailed(broker: Broker, exception: Throwable?) {
        showToast("Connection to ${broker.name} failed.")
    }

    override fun onDisconnectSuccess(broker: Broker) {
        showToast("Successfully disconnected from ${broker.name}.")
    }

    override fun onDisconnectFailed(broker: Broker, exception: Throwable?) {
        showToast("Can't disconnect from ${broker.name}.")
    }

    fun loadBrokers() {
        val broker = BrokerEntity()
        broker.setName("Home")
        broker.setHost("192.168.1.1")
        broker.setPort(1883)
        broker.setProtocol(Protocol.TCP)

        mBrokers.add(broker)

        val brokers = mBrokers
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navViewMenu = navView.menu
        val brokersSubmenu = navViewMenu.findItem(R.id.nav_broker_menu).subMenu

        brokers.forEachIndexed { index, broker ->
            brokersSubmenu.add(R.id.nav_broker_group, index, Menu.NONE, broker.name)
        }
    }

    fun publishMessage() {
        val message = MessageEntity()
        message.setTopic("cmnd/room_light/power")
        message.setPayload("toggle")
        mMqttClient.publishMessage(message)
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        var closeDrawer = false
        when (val itemId = item.itemId) {
            R.id.nav_add_broker -> {
                closeDrawer = true
            }
            else -> {
                val broker = mBrokers.get(itemId)

                Log.i(TAG, "Connecting to broker ${broker.name}")
                mMqttClient.connect(broker)
            }
        }
        if (closeDrawer) {
            val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        return true
    }
}
