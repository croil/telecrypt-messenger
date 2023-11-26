//package tele.crypt.telecrypt
//
//import android.Manifest
//import android.annotation.SuppressLint
//import android.app.Activity
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.graphics.Bitmap
//import android.graphics.ImageDecoder
//import android.os.Build
//import android.os.Bundle
//import android.provider.MediaStore
//import android.view.View
//import android.widget.Toast
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.appcompat.app.AppCompatActivity
//import androidx.appcompat.widget.AppCompatImageView
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.google.android.material.textfield.TextInputEditText
//import com.google.firebase.firestore.DocumentChange
//import com.google.firebase.firestore.EventListener
//import com.google.firebase.firestore.QuerySnapshot
//import com.google.firebase.firestore.ktx.firestore
//import com.google.firebase.ktx.Firebase
//import tele.crypt.chat.ChatAdapter
//import tele.crypt.chat.Message
//import tele.crypt.recycler.User
//import tele.crypt.telecrypt.databinding.ActivityChatBinding
//import java.util.*
//
//class ChatActivity : AppCompatActivity() {
//
//    private var attachImage : Bitmap? = null
//    private lateinit var layoutManager: LinearLayoutManager
//    private lateinit var binding: ActivityChatBinding
//    private lateinit var messageField: TextInputEditText
//    private lateinit var user: User
//    private lateinit var back: AppCompatImageView
//    private lateinit var send: AppCompatImageView
//    private lateinit var attach: AppCompatImageView
//    private lateinit var messages: ArrayList<Message>
//    private lateinit var chatAdapter: ChatAdapter
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityChatBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//        setUser()
//        messageField = binding.yourMessage
//        back = binding.backChatButton
//        send = binding.sendMessage
//        attach = binding.fileAttach
//        attach.rotation = 45.0F
//        bindMessages()
//        back.setOnClickListener {
//            val mainIntent = Intent(this@ChatActivity, MainActivity::class.java)
//            mainIntent.putExtra(
//                Constants.MAIN_USER_ID,
//                intent.getStringExtra(Constants.MAIN_USER_ID)
//            )
//            startActivity(mainIntent)
//            finish()
//        }
//        send.setOnClickListener {
//            send()
//        }
//        attach.setOnClickListener {
//            if (checkPermission()) {
//                openStorage()
//            }
//        }
//        loadMessages()
//    }
//
//    private fun loadMessages() {
//        Firebase.firestore.collection(Constants.USER_CHATS)
//            .document(
//                chooseBetweenChats(
//                    intent.getStringExtra(Constants.MAIN_USER_ID).toString(),
//                    user.id
//                )
//            )
//            .collection(Constants.SUB_CHATS)
//            .addSnapshotListener(eventListener)
//    }
//
//    private fun bindMessages() {
//        messages = arrayListOf()
//        chatAdapter = ChatAdapter(
//            messages,
//            user,
//            intent.getStringExtra(Constants.MAIN_USER_ID).toString()
//        )
//        layoutManager = LinearLayoutManager(this@ChatActivity)
//        layoutManager.stackFromEnd = true
//        binding.chatRecyclerView.layoutManager = layoutManager
//        binding.chatRecyclerView.adapter = chatAdapter
//    }
//
//
//    @SuppressLint("NotifyDataSetChanged")
//    private val eventListener = EventListener<QuerySnapshot> { value, error ->
//        if (error != null)
//            return@EventListener
//        if (value != null) {
//            val count = messages.size
//            for (doc in value.documentChanges) {
//                if (doc.type == DocumentChange.Type.ADDED) {
//                    println(doc.document.getString("message"))
//                    messages.add(
//                        Message(
//                            doc.document.getString("message").toString(),
//                            Utilities.uncompress(doc.document.getString("image").toString()),
//                            doc.document.getString("sender").toString(),
//                            doc.document.getString("receiver").toString(),
//                            doc.document.getString("date").toString()
//                        )
//                    )
//                }
//            }
//            messages.sortWith(Comparator { a: Message, b: Message ->
//                a.dateTime.compareTo(
//                    b.dateTime
//                )
//            })
//            if (count == 0) {
//                chatAdapter.notifyDataSetChanged()
//            } else {
//                chatAdapter.notifyItemInserted(messages.size - 1)
//                binding.chatRecyclerView.scrollToPosition(messages.size - 1)
//            }
//
//            binding.chatRecyclerView.visibility = View.VISIBLE
//        }
//    }
//
//    private fun send() {
//        val id1 = intent.getStringExtra(Constants.MAIN_USER_ID).toString()
//        val id2 = user.id
//        val map = hashMapOf(
//            "message" to messageField.text.toString(),
//            "sender" to id1,
//            "receiver" to id2,
//            "image" to Utilities.compress(attachImage),
//            "date" to Date().toString()
//        )
//        Firebase.firestore
//            .collection(Constants.USER_CHATS)
//            .document(chooseBetweenChats(id1, id2))
//            .collection(Constants.SUB_CHATS)
//            .add(map)
//            .addOnSuccessListener {
//                messageField.setText("")
//                attachImage = null
//                attach = binding.fileAttach
//                attach.rotation = 45.0F
//            }
//    }
//
//    private fun chooseBetweenChats(id1: String, id2: String) =
//        if (id1 > id2) "$id1:$id2" else "$id2:$id1"
//
//    private fun setUser() {
//        user = intent.getSerializableExtra(Constants.USER_DATA) as User
//        binding.chatUsername.text = user.name
//        if (user.image.isNotEmpty()) binding.userImage.setImageBitmap(Utilities.uncompress(user.image)
//            ?.let { Utilities.cropBitmap(it) })
//    }
//
//    private fun checkPermission(): Boolean {
//        if (ContextCompat.checkSelfPermission(
//                this@ChatActivity,
//                Manifest.permission.READ_EXTERNAL_STORAGE
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(
//                this@ChatActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 123
//            )
//            return false
//        }
//        return true
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        when (requestCode) {
//            123 -> {
//                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(
//                        this,
//                        "Permission denied...",
//                        Toast.LENGTH_LONG
//                    ).show()
//                } else {
//                    openStorage()
//                }
//            }
//
//        }
//    }
//
//    private var resultLauncher =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
//            if (it.resultCode == Activity.RESULT_OK) {
//                val data: Intent? = it.data
//                if (data != null) {
//                    val uri = data.data
//                    if (uri != null) {
//                        val bitmap = if (Build.VERSION.SDK_INT < 28) {
//                            MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
//                        } else {
//                            val source = ImageDecoder.createSource(this.contentResolver, uri)
//                            ImageDecoder.decodeBitmap(source)
//                        }
//                        attach.setImageBitmap(Utilities.cropBitmap(bitmap))
//                        attach.rotation = 0F
//                        attachImage = bitmap
//                    }
//
//                }
//            }
//        }
//
//    private fun openStorage() {
//        val intent = Intent(Intent.ACTION_PICK)
//        intent.type = "image/*"
//        resultLauncher.launch(intent)
//    }
//
//
//}