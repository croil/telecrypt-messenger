package tele.crypt.telecrypt

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.provider.MediaStore
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import tele.crypt.chat.ChatAdapter
import tele.crypt.database.MessageService
import tele.crypt.database.UserListener
import tele.crypt.recycler.User
import tele.crypt.telecrypt.databinding.ActivityChannelBinding

class ChannelActivity : AppCompatActivity() {
    private var service: MessageService? = null
    private var attachImage: Bitmap? = null
    private var attachPath = ""
    private lateinit var binding: ActivityChannelBinding
    private lateinit var messageField: TextInputEditText
    private lateinit var user: User
    private lateinit var back: AppCompatImageView
    private lateinit var send: AppCompatImageView
    private lateinit var attach: AppCompatImageView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var path: String
    private lateinit var recyclerView: RecyclerView



    private val msConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, bind: IBinder?) {
            val binder = (bind as MessageService.SessionBinder).service
            this@ChannelActivity.service = binder
            service?.listen(userListener)
            if (!service?.isRunning!!) {
                onServiceStart(binder)
            }
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
        }
    }

    fun onServiceStart(service: MessageService) {
        showGIF(true)
        service.isRunning = true
        service.upload()
    }

    private fun showGIF(type: Boolean) {
        if (type) {
            Glide.with(this).load(R.drawable.cat_gif).into(binding.happyLoader)
            binding.happyLoader.visibility = View.VISIBLE
        } else {
            binding.happyLoader.visibility = View.INVISIBLE
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChannelBinding.inflate(layoutInflater)
        setContentView(binding.root)
        path = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString()
        viewDidLoad()
        bindMessages()
        val serviceIntent = Intent(this, MessageService::class.java)
        serviceIntent.putExtra(Constants.PATH, path)
        startService(serviceIntent)
        bindService(serviceIntent, msConnection, BIND_AUTO_CREATE)

        binding.yourMessage.setOnEditorActionListener { _, i, _ ->
            if (i == EditorInfo.IME_ACTION_DONE) {
                // TODO: #7 Not supported yet
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
        back.setOnClickListener {
            logout()
            finish()
//            val mainIntent = Intent(this@ChannelActivity, LoginActivity::class.java)
//            mainIntent.putExtra(
//                Constants.MAIN_USER_ID, intent.getStringExtra(Constants.MAIN_USER_ID)
//            )
//            startActivity(mainIntent)
//            finish()
        }

        send.setOnClickListener {
            lifecycleScope.launch {
                send()
            }
        }
        attach.setOnClickListener {
            if (checkPermission()) {
                openStorage()
            }
        }

    }

    fun openImage(view: View) {
        val description =
            view.findViewById<ImageView>(R.id.receive_image_message).contentDescription
        toImageActivity(description.toString())
    }

    fun openMyImage(view: View) {
        val description = view.findViewById<ImageView>(R.id.send_image_message).contentDescription
        toImageActivity(description.toString())
    }

    private fun toImageActivity(description: String) {
        if (description.isNotEmpty()) {
            val imageActivity = Intent(this, ImageActivity::class.java)
            imageActivity.putExtra(
                Constants.MAIN_USER_ID,
                intent.getStringExtra(Constants.MAIN_USER_ID).toString()
            )
            imageActivity.putExtra(Constants.PATH, path)
            imageActivity.putExtra(Constants.IMAGE_LINK, description)
            startActivity(imageActivity)
        }
    }



    private suspend fun send() {
        service?.send(messageField.text.toString(), attachPath)
        messageField.setText("")
        attach.setImageResource(R.drawable.ic_attach)
        attach.rotation = 45F
        attachPath = ""
    }


    private fun bindMessages() {
        //TODO #2 Создавать адаптер только один раз, оставить только байнд к ресайклеру и адаптеру
        chatAdapter = ChatAdapter(intent.getStringExtra(Constants.MAIN_USER_ID).toString()) {
            service?.load(40)
        }
        recyclerView = binding.chatRecyclerView
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChannelActivity)
            (layoutManager as LinearLayoutManager).stackFromEnd = true
            adapter = chatAdapter
        }
    }

    private val userListener: UserListener = { list ->
        chatAdapter.messages = list
        showGIF(false)
//        if (list.size > 0 && change) {
//            recyclerView.smoothScrollToPosition(
//                list.size - 1
//            )
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        service?.unListen(userListener)
    }

    private fun loseItAll(session: SessionManager) {
        session.getEditor().clear().apply()
    }

    private fun logout() {
        val sessionManager = SessionManager(this@ChannelActivity)
        loseItAll(sessionManager)
        sessionManager.removeSession()
        toLoginActivity()
    }

    private fun toLoginActivity() {
        val intent = Intent(this@ChannelActivity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun chooseBetweenChats(id1: String, id2: String) =
        if (id1 > id2) "$id1:$id2" else "$id2:$id1"

    private fun viewDidLoad() {
        messageField = binding.yourMessage
        back = binding.backChatButton
        send = binding.sendMessage
        attach = binding.fileAttach
        attach.rotation = 45.0F
        user = intent.getSerializableExtra(Constants.USER_DATA) as User
        binding.chatUsername.text = user.name
        val userImage = binding.userImage
        var bitmap: Bitmap? = null
        if (user.image.isNotEmpty()) {
            bitmap = Utilities.uncompress(user.image)?.let { Utilities.cropBitmap(it) }
        }
        if (bitmap != null) {
            userImage.setImageBitmap(bitmap)
        } else {
            userImage.setImageResource(R.drawable.ic_avatar)
            userImage.setColorFilter(Utilities.getRandomColor())
        }
    }

    private fun checkPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this@ChannelActivity, Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@ChannelActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 123
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            123 -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        this,
                        getString(R.string.permission_denied),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    openStorage()
                }
            }

        }
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val data: Intent? = it.data
                if (data != null) {
                    val uri = data.data
                    if (uri != null) {
                        val cursor = this.contentResolver.query(uri, null, null, null, null, null)
                        cursor?.moveToFirst()
                        val bitmap = if (Build.VERSION.SDK_INT < 28) {
                            MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                        } else {
                            val source = ImageDecoder.createSource(this.contentResolver, uri)
                            ImageDecoder.decodeBitmap(source)
                        }
                        attach.setImageBitmap(Utilities.cropBitmap(bitmap))
                        attach.rotation = 0F
                        attachImage = bitmap
                        attachPath = cursor?.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                            ?.let { it1 -> cursor.getString(it1) }.toString()
                    }

                }
            }
        }

    private fun openStorage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        resultLauncher.launch(intent)
    }

}