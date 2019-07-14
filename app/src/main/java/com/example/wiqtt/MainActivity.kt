package com.example.wiqtt

import android.content.Intent
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
import io.requery.android.sqlite.DatabaseSource
import io.requery.kotlin.eq
import io.requery.sql.KotlinEntityDataStore
import javax.sql.DataSource

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    IMqttConnectListener {

    private val TAG = "WiQTT"

    private val BROKER_REQ_CODE = 0
    private val MESSAGE_REQ_CODE = 1

    private val mClientId = "wiqtt-client"

    private lateinit var mMqttClient: MqttClient

    private var mCurrentBroker: Broker? = null

    private lateinit var mMessageListAdapter: MessageListAdapter

    private lateinit var mDataStore: KotlinEntityDataStore<Any>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val messageAddButton: FloatingActionButton = findViewById(R.id.message_add)

        messageAddButton.setOnClickListener {
            startActivityForResult(Intent(applicationContext, MessageCreationActivity::class.java), MESSAGE_REQ_CODE)
        }

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)

        val source = DatabaseSource(applicationContext, Models.DEFAULT, 1)
        mDataStore = KotlinEntityDataStore(source.configuration)

        mMessageListAdapter = MessageListAdapter(mutableListOf(), ::onMessageClicked)

        val viewManager = LinearLayoutManager(this)
        val messageRecyclerView: RecyclerView = findViewById(R.id.message_recycler_view)

        messageRecyclerView.apply {
            adapter = mMessageListAdapter
            layoutManager = viewManager
        }

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

        reloadBrokers()
    }

    private fun reloadBrokers() {
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navViewMenu = navView.menu
        val brokersSubmenu = navViewMenu.findItem(R.id.nav_broker_menu).subMenu
        brokersSubmenu.clear()

        // TODO: Investigate how to use mDataStore.invoke { ... }
        val brokers = mDataStore.select(Broker::class).get()

        brokers.forEach { broker ->
            brokersSubmenu.add(R.id.nav_broker_group, broker.id, Menu.NONE, broker.name)
        }

    }

    private fun reloadMessages() {
        mDataStore.invoke {
            val result = select(Message::class) where (Message::broker eq mCurrentBroker)
            val messages = result.get().toList()
            mMessageListAdapter.reloadMessages(messages)
        }
    }


    private fun showToast(text: String) {
        val toast = Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT)
        toast.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            BROKER_REQ_CODE -> {
                if (resultCode == 0) {
                    data ?: return
                    val extras = data.extras ?: return

                    val broker = extras.getParcelable<Broker>(resources.getString(R.string.broker_intent_extra))
                    broker?.let {
                        mDataStore.insert(broker)
                        reloadBrokers()
                    }
                }
            }
            MESSAGE_REQ_CODE -> {
                data ?: return
                val extras = data.extras ?: return

                val message = extras.getParcelable<Message>(resources.getString(R.string.message_intent_extra))
                message?.let {
                    message.broker = mCurrentBroker ?: return
                    mDataStore.insert(message)
                    reloadMessages()
                }
            }
        }

    }

    override fun onConnectSuccess(broker: Broker) {
        showToast("Successfully connected to ${broker.name}.")
        val brokerNameTextView: TextView = findViewById(R.id.nav_broker_name)
        brokerNameTextView.text = broker.name

        val brokerIpTextView: TextView = findViewById(R.id.nav_broker_uri)
        brokerIpTextView.text = brokerAsUri(broker)

        reloadMessages()
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


    private fun onMessageClicked(message: Message, isShortClick: Boolean) {
        if (isShortClick) {
            mMqttClient.publishMessage(message)
        } else {
            ShortcutHelper.createMessageShortcut(applicationContext, message)
        }

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
                startActivityForResult(Intent(applicationContext, BrokerCreationActivity::class.java), BROKER_REQ_CODE)
            }
            else -> {
                mDataStore.invoke {
                    val result = select(Broker::class) where (Broker::id eq itemId) limit 5
                    val broker = result.get().first()
                    mMqttClient.connect(broker)
                    mCurrentBroker = broker
                }
            }
        }
        if (closeDrawer) {
            val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        return true
    }
}
