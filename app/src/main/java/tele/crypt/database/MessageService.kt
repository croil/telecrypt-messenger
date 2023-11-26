package tele.crypt.database


import android.app.Service
import android.content.Intent
import android.os.Binder
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import tele.crypt.chat.Message
import tele.crypt.retrofit.RetrofitClient
import tele.crypt.retrofit.RetrofitConfig
import tele.crypt.telecrypt.Constants


typealias UserListener = (messageList: ArrayList<Message>) -> Unit

class MessageService : Service() {
    private var messageList = arrayListOf<Message>()
    private var delayed = arrayListOf<Message>()
    private val listeners = mutableSetOf<UserListener>()
    private lateinit var path: String
    private lateinit var db: AppDatabase
    private lateinit var messageDao: MessageDao
    private lateinit var scope: CoroutineScope
    private lateinit var retrofitClient: RetrofitClient
    private var uploaded = false
    var isRunning = false
    private val sender = "night"
    companion object {
        private const val START = 1
        private const val END = 2
        private const val INF = Int.MAX_VALUE
    }

    fun listen(c: UserListener) {
        listeners.add(c)
        c.invoke(messageList)
    }

    fun unListen(c: UserListener) {
        listeners.remove(c)
    }


    /**
     * Notify changes, calling invoke method to all listeners.
     * We call ourself and do callback in MainActivity, then it changed
     * adapter condition*/
    private fun notifyChanges() {
        listeners.forEach { it.invoke(messageList) }
    }


    override fun onCreate() {
        super.onCreate()
        scope = CoroutineScope(Dispatchers.IO)
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        retrofitClient = RetrofitClient(
            RetrofitConfig(
                createRetrofit(moshi),
                moshi
            )
        )
    }

    /** Create retrofit with moshi converter factory **/
    private fun createRetrofit(moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        path = intent.getStringExtra(Constants.PATH).toString()
        db = AppDatabase.getInstance(applicationContext)
        messageDao = db.messageDao()
        return START_NOT_STICKY
    }

    /**
     * Contract: Either image or text. The image has priority*/
    suspend fun send(text: String, image: String) {
        if (image.isNotEmpty()) {
            scope.launch {
                imageRequest(image)
            }
        } else if (text.isNotEmpty()) {
            scope.launch {
                textRequest(text)
            }
        }
    }

    private suspend fun imageRequest(link: String) {
        val id = retrofitClient.imageRequest(sender, link)
        val wrapper = retrofitClient.request(id - 1, false, 1)
        val name = if (wrapper.isNotEmpty()) wrapper.last().data.Image?.link else ""
        if (id != -1) {
            if (!indexCheck(id) && name != null && name.isNotEmpty()) {
                addMessage(
                    Message(
                        "", name, sender, "1@channel",
                        (System.currentTimeMillis() / 1000L).toString(), id
                    )
                )
            }
        }
    }

    private suspend fun addMessage(message: Message) = listCopy {
        withContext(Dispatchers.Main) {
            launch(scope.coroutineContext) {
                messageDao.insert(MessageEntity.createMessage(message))
            }
            if (uploaded) {
                messageList.add(message)
                notifyChanges()
            } else {
                delayed.add(message)
            }
        }

    }

    private suspend fun textRequest(text: String) {
        val id = retrofitClient.textRequest(sender, text)
        if (id != -1 && !indexCheck(id)) {
            addMessage(
                Message(
                    text, "", sender, "1@channel",
                    (System.currentTimeMillis() / 1000L).toString(), id
                )
            )
        }
    }

    /**
     * Check last element of list with index*/
    private fun indexCheck(index: Int): Boolean {
        return if (messageList.isNotEmpty()) messageList.last().id == index else false
    }


    private suspend fun refreshAdapter() = listCopy {
        withContext(Dispatchers.Main) {
            for (message in delayed) {
                messageList.add(message)
            }
            delayed = arrayListOf()
            uploaded = true
            notifyChanges()
        }
    }

    /**
     * Upload: Calling while service create: Get data from database and make request to server*/
    fun upload() {
        scope.launch {
            uploaded = false
            messageList = ArrayList(messageDao.getAll().map { it.toMessage() })
            if (messageList.isNotEmpty()) {
                request(INF, END)
            } else {
                request(100, START)
            }
            refreshAdapter()
            uploaded = true
            looper()
        }
    }

    fun load(amount : Int) {
        if (!uploaded) return
        scope.launch {
            uploaded = false
            request(amount, START)
            refreshAdapter()
            uploaded = true
        }
    }


    private suspend fun <T> listCopy(code : suspend () -> T) : T {
        messageList = ArrayList(messageList)
        return code()
    }


    private suspend fun request(amount: Int, type: Int): Int = listCopy {
        var counter = 0
        while (true) {
            val list = if (messageList.isNotEmpty()) {
                if (type == START) retrofitClient.request(messageList[0].id, true)
                else retrofitClient.request(messageList.last().id, false, 100)
            } else {
                if (type == START) retrofitClient.request(10000, true)
                else retrofitClient.request(0, false)
            }
            if (list.isEmpty()) break
            for (message in list.map { it.toMessage() }) {
                if (type == START) {
                    messageDao.insert(MessageEntity.createMessage(message))
                    messageList.add(0, message)
                }
                if (type == END) {
                    if (!indexCheck(message.id)) {
                        messageDao.insert(MessageEntity.createMessage(message))
                        messageList.add(message)
                    }
                }
                counter++
                if (counter >= amount) return@listCopy counter
            }
        }
        return@listCopy counter
    }

    /**
     * Make request each 5 second*/
    //TODO #10 Запускать сразу при старте сервиса, пусть пихает в delayed
    private suspend fun looper() {
        while (true) {
            val response = request(10, END)
            withContext(Dispatchers.Main) {
                if (response > 0) notifyChanges()
            }
            delay(5000)
            println("Monitoring...")
        }
    }


    override fun onBind(p0: Intent?): SessionBinder = SessionBinder()

    inner class SessionBinder : Binder() {
        val service: MessageService
            get() = this@MessageService
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        scope.cancel()
        AppDatabase.closeDatabase()
    }
}