package tele.crypt.telecrypt

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import tele.crypt.recycler.User
import tele.crypt.telecrypt.databinding.ActivityLoginBinding


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var username: TextInputEditText
    private lateinit var password: TextInputEditText
    private lateinit var errorMessage: TextView
    private lateinit var progressBar: ProgressBar
    val db = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        username = binding.username
        password = binding.password
        errorMessage = binding.errorField
        progressBar = binding.loginProgressBar
    }

    private fun toast(ex: String) {
        Toast.makeText(this, ex, Toast.LENGTH_LONG).show()
    }

    override fun onStart() {
        super.onStart()
        checkSession()
    }

    fun login(view: View) {
        val sessionManager = SessionManager(this@LoginActivity)
        val contextUsername = username.text.toString()
        val contextPassword = password.text.toString()
        if (db) {
            if (checkFields()) {
                activateProgressBar(View.VISIBLE)
                Firebase.firestore.collection(Constants.USER_LIST)
                    .whereEqualTo("username", contextUsername)
                    .whereEqualTo("password", contextPassword)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.documents.size > 0) {
                            activateProgressBar(View.INVISIBLE)
                            sessionManager.saveSession(document.documents[0].id)
                            toMainActivity(document.documents[0].id)
                        } else {
                            activateProgressBar(View.INVISIBLE)
                            toast("Incorrect login or password")
                        }
                    }
                    .addOnFailureListener {
                        activateProgressBar(View.INVISIBLE)
                        toast("No users with such login")
                    }
            }
        } else {
            if (checkFields()) {
                activateProgressBar(View.INVISIBLE)
                sessionManager.saveSession("night")
                toMainActivity("night")
            }
        }


    }

    private fun activateProgressBar(base: Int) {
        progressBar.visibility = base
    }


    private fun checkSession() {
        val sessionManager = SessionManager(this@LoginActivity)
        val userId: String? = sessionManager.getSession()
        if (userId != null) {
            toMainActivity(userId)
        }
    }


    private fun checkFields(): Boolean {
        val strUsername = username.text.toString()
        val strPassword = password.text.toString()
        if (strUsername.isNotEmpty() && strPassword.isNotEmpty()) {
            errorMessage.text = ""
            return true
        }
        if (strUsername.isEmpty()) {
            errorMessage.text = "Input the login"
        } else if (strPassword.isEmpty()) {
            errorMessage.text = "Input the password"
        } else {
            errorMessage.text = "Input login and password"
        }
        return false
    }


//    private fun addChat(list: ArrayList<User>) {
//        val buffer = BufferedReader(InputStreamReader(openFileInput(Constants.CHAT_FILE_NAME)))
//        buffer.forEachLine {
//            val current = it.split(" ")
//            if (current.size > 1 && current[0][0] == '@') list.add(
//                User(
//                    current[0],
//                    current[1]
//                )
//            )
//        }
//        Singleton.setList(list)
//        buffer.close()
//    }


    private fun toMainActivity(userID: String) {
        val intent = Intent(this@LoginActivity, ChannelActivity::class.java)
        intent.putExtra(Constants.MAIN_USER_ID, userID)
        intent.putExtra(Constants.USER_DATA, User("1@channel", "1@ch", "Двач", ""))
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}