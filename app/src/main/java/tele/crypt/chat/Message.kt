package tele.crypt.chat

data class Message(
    val message: String,
    val link : String,
    val sender: String,
    val receiver: String,
    val dateTime: String,
    var id: Int = 0
)