package tele.crypt.telecrypt

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tele.crypt.recycler.User
import tele.crypt.recycler.UserAdapter
import tele.crypt.services.SessionService
import tele.crypt.telecrypt.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var list = arrayListOf<User>()
    private lateinit var myRecyclerView: RecyclerView
    private lateinit var binding: ActivityMainBinding
    private lateinit var progressBar: ProgressBar
    private var service: SessionService? = null
    private lateinit var mainUserId: String

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, bind: IBinder?) {
            val binder = (bind as SessionService.SessionBinder).service
            this@MainActivity.service = binder
            if (!binder.isRunning) {
                activateProgressBar(View.VISIBLE)
                binder.updateUserList(object : SessionService.Callback {
                    override fun onCallback(list: ArrayList<User>) {
                        activateProgressBar(View.INVISIBLE)
                        this@MainActivity.list = list
                        createList()
                    }
                })
                binder.isRunning = true
            } else {
                this@MainActivity.list = binder.getUsers()
                createList()
            }
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressBar = binding.recyclerProgressBar
        mainUserId = intent.getStringExtra(Constants.MAIN_USER_ID).toString()
        val serviceIntent = Intent(this, SessionService::class.java)
        serviceIntent.putExtra(
            Constants.MAIN_USER_ID,
            mainUserId
        )
        startService(serviceIntent)
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE)


        binding.popupMenu.setOnClickListener {
            val popupMenu = PopupMenu(this, it)
            popupMenu.menuInflater.inflate(R.menu.menu_item, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.add_user -> startActivity(Intent(this, AddContactActivity::class.java))
                    R.id.delete_session -> {
                        Toast.makeText(this, getString(R.string.session_deleted), Toast.LENGTH_LONG)
                            .show()
                        logout()
                    }
                }
                true
            }
            popupMenu.show()
        }
    }


    private fun activateProgressBar(base: Int) {
        progressBar.visibility = base
    }

    private fun createList() {
        myRecyclerView = binding.recyclerView
        myRecyclerView.adapter?.notifyItemInserted(list.size - 1)
        myRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = UserAdapter(list) { user ->
                run {
                    val chatIntent = Intent(this@MainActivity, ChannelActivity::class.java)
                    chatIntent.putExtra(Constants.USER_DATA, user)
                    chatIntent.putExtra(Constants.MAIN_USER_ID, mainUserId)
                    startActivity(chatIntent)
                    finish()
                }
            }
        }
    }

    private fun loseItAll(session: SessionManager) {
        session.getEditor().clear().apply()
    }


    private fun logout() {
        val sessionManager = SessionManager(this@MainActivity)
        loseItAll(sessionManager)
        sessionManager.removeSession()
        toLoginActivity()
    }


    private fun toLoginActivity() {
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }


    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }
}