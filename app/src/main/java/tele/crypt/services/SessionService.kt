package tele.crypt.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.widget.Toast
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import tele.crypt.recycler.User
import tele.crypt.telecrypt.Constants
import tele.crypt.telecrypt.GenerateKeyActivity


class SessionService : Service() {
    private var userList = arrayListOf<User>()
    var isRunning = false
    private var currentUser = ""


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (currentUser.isEmpty()) {
            currentUser = intent.getStringExtra(Constants.MAIN_USER_ID).toString()
        }
        return START_NOT_STICKY
    }


    fun getUsers() = this.userList
    @SuppressWarnings("unchecked")
    fun updateUserList(c: Callback) {
        val collection = Firebase.firestore.collection(Constants.USER_LIST)
        collection.document(currentUser).get().addOnSuccessListener { document ->
            val friends = document.get("friends") as Map<String, Map<String, String>>
            if (friends.isNotEmpty()) {
                friends.forEach {
                    val user = it.value
                    val username = user.getOrDefault("username", "")
                    val name = user.getOrDefault("name", "")
                    val image = openFileInput("${user.getOrDefault("id", "")}.txt").bufferedReader()
                        .use { r -> r.readText() }
                    if (name.isEmpty()) {
                        parseFriend(collection, username, object : UserCallback {
                            override fun onCallback(user: User) {
                                userList.add(user)
                            }
                        })
                    } else {
                        userList.add(
                            User(
                                user.getOrDefault("id", ""), username, name, image
                            )
                        )
                    }
                }
            }
            c.onCallback(userList)
        }.addOnFailureListener {
            toast("Something went wrong")
        }
    }

    private fun parseFriend(collection: CollectionReference, id: String, c: UserCallback) {
        collection.document(id).get().addOnSuccessListener {
            c.onCallback(
                User(
                    it.id,
                    it.getString("username").toString(),
                    it.getString("name").toString(),
                    it.getString("image").toString()
                )
            )
        }
    }


    private fun toast(ex: String) {
        Toast.makeText(this, ex, Toast.LENGTH_LONG).show()
    }

    override fun onBind(p0: Intent?): SessionBinder = SessionBinder()

    inner class SessionBinder : Binder() {
        val service: SessionService
            get() = this@SessionService
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }

    fun addUser(id: String, user: User, c: GenerateKeyActivity.AddUserCallback) {
        userList.add(user)
        val document = Firebase.firestore.collection(Constants.USER_LIST).document(currentUser)
        document.get().addOnSuccessListener {
            val friendMap = it.get("friends") as MutableMap<String, Map<String, String>>
            friendMap[id] = mapOf(
                "id" to user.id,
                "username" to user.username,
                "name" to user.name,
            )
            println(user.image)
            reloadMap(document, friendMap, c)
        }.addOnFailureListener {
            toast("Something went wrong")
        }
    }

    private fun reloadMap(
        document: DocumentReference,
        friendMap: MutableMap<String, Map<String, String>>,
        c: GenerateKeyActivity.AddUserCallback
    ) {
        document.update("friends", friendMap).addOnSuccessListener {
            c.onCallback()
        }.addOnFailureListener {
            toast("Something went wrong")
        }
    }


    interface Callback {
        fun onCallback(list: ArrayList<User>)
    }

    interface UserCallback {
        fun onCallback(user: User)
    }

}
