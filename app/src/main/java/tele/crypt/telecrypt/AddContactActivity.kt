package tele.crypt.telecrypt


import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import tele.crypt.services.SessionService
import tele.crypt.telecrypt.databinding.ActivityAddContactBinding


class AddContactActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddContactBinding
    private lateinit var addContactButton: Button
    private lateinit var backButton: Button
    private lateinit var name: EditText
    private lateinit var id: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var image: ImageView
    private var currentBitmap: Bitmap? = null
    private var service: SessionService? = null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, bind: IBinder?) {
            val binder = (bind as SessionService.SessionBinder).service
            this@AddContactActivity.service = binder
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddContactBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val serviceIntent = Intent(this, SessionService::class.java)
        startService(serviceIntent)
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE)
        addContactButton = binding.addNewContact
        backButton = binding.back
        name = binding.username
        id = binding.userId
        progressBar = binding.addContactProgressBar
        image = binding.userImage
        image.setOnClickListener {
            if (checkPermission()) openStorage()
        }
        addContactButton.setOnClickListener {
            val strName = name.text.toString()
            val login = id.text.toString()
            if (strName.isNotEmpty()) {
                activateProgressBar(View.VISIBLE)
                userExist(login, object : AddContactCallback {
                    override fun onCallback(exist: Boolean, id: String) {
                        activateProgressBar(View.INVISIBLE)
                        if (exist) {
                            val intent = Intent(
                                this@AddContactActivity,
                                GenerateKeyActivity::class.java
                            )
                            intent.putExtra(Constants.USER_ID, id)
                            intent.putExtra(Constants.USER_LOGIN, login)
                            intent.putExtra(Constants.USER_NAME, strName)
                            println("$id $login $strName")
                            try {
                                openFileOutput("$id.txt", Context.MODE_PRIVATE).use {
                                    it.write(Utilities.compress(currentBitmap).toByteArray())
                                    it.close()
                                }
                            } catch (e: Exception) {
                                println("Tut vsyo ploho $e\n")
                            }

                            startActivity(intent)
                        } else {
                            Toast.makeText(
                                this@AddContactActivity,
                                "No user with such login or this user is already in your friend list",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                })

            } else Toast.makeText(this, "Field aren\'t filled", Toast.LENGTH_LONG).show()
        }
        backButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }


    private fun activateProgressBar(base: Int) {
        progressBar.visibility = base
    }

    private fun userExist(login: String, c: AddContactCallback) {
        Firebase.firestore.collection(Constants.USER_LIST)
            .whereEqualTo("username", login)
            .get()
            .addOnSuccessListener {
                if (it != null && it.documents.size > 0) {
                    var inList = false
                    for (item in service?.getUsers()!!) {
                        if (item.username == it.documents[0].get("username")) {
                            c.onCallback(false, "")
                            inList = true
                            break
                        }
                    }
                    if (!inList) c.onCallback(true, it.documents[0].id)
                } else c.onCallback(false, "")
            }
    }

    private fun checkPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this@AddContactActivity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@AddContactActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 123
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            123 -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        this,
                        "Permission denied...",
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
            val data: Intent? = it.data
            if (data != null) {
                val uri = data.data
                if (uri != null) {
                    val bitmap = if (Build.VERSION.SDK_INT < 28) {
                        MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                    } else {
                        val source = ImageDecoder.createSource(this.contentResolver, uri)
                        ImageDecoder.decodeBitmap(source)

                    }
                    currentBitmap = Utilities.cropBitmap(bitmap)
                    image.setImageBitmap(currentBitmap)

                }

            }
        }


    private fun openStorage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        resultLauncher.launch(intent)
    }

    interface AddContactCallback {
        fun onCallback(exist: Boolean, id: String)
    }

}