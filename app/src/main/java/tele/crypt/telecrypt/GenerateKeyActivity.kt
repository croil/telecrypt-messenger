package tele.crypt.telecrypt

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import tele.crypt.RSA.RSA
import tele.crypt.recycler.User
import tele.crypt.services.SessionService

class GenerateKeyActivity : AppCompatActivity() {
    private var wasRunning = false
    private var service: SessionService? = null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, bind: IBinder?) {
            val binder = (bind as SessionService.SessionBinder).service
            this@GenerateKeyActivity.service = binder
            if (!wasRunning) {
                val shift = RSA()
                makeAll(shift)
                wasRunning = true
            }
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate_key)
        if (savedInstanceState != null) {
            wasRunning = savedInstanceState.getBoolean(Constants.PROCESS_RUNNING)
        }

        val serviceIntent = Intent(this, SessionService::class.java)
        startService(serviceIntent)
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE)
    }

    private fun makeAll(shift: RSA) {
        service?.addUser(intent.getStringExtra(Constants.USER_ID).toString(),
            User(
                intent.getStringExtra(Constants.USER_ID).toString(),
                intent.getStringExtra(Constants.USER_LOGIN).toString(),
                intent.getStringExtra(Constants.USER_NAME).toString(),
                openFileInput("${intent.getStringExtra(Constants.USER_ID)}.txt").bufferedReader()
                    .use { r -> r.readText() }
            ), object : AddUserCallback {
                override fun onCallback() {
                    startActivity(
                        Intent(
                            this@GenerateKeyActivity,
                            MainActivity::class.java
                        )
                    )
                }
            })


//        val mainExecutor: Executor = ContextCompat.getMainExecutor(this)
//        val backgroundExecutor = Executors.newSingleThreadScheduledExecutor()
//        backgroundExecutor.execute {
//
//            mainExecutor.execute {
//
//            }
//        }
    }

    interface AddUserCallback {
        fun onCallback()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(Constants.PROCESS_RUNNING, wasRunning)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }


}