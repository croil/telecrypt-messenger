package tele.crypt.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import tele.crypt.chat.Message

@Entity(
    tableName = "messages"
)
data class MessageEntity(
    val message: String,
    val link: String,
    val sender: String,
    val receiver: String,
    @ColumnInfo(name = "date_time") val dateTime: String,
    @PrimaryKey val id: Int
) {
    fun toMessage(): Message = Message(
        message = message,
        link = link,
        sender = sender,
        receiver = receiver,
        dateTime = dateTime,
        id = id
    )

    companion object {
        fun createMessage(message: Message): MessageEntity = MessageEntity(
            message = message.message,
            link = message.link,
            sender = message.sender,
            receiver = message.receiver,
            dateTime = message.dateTime,
            id = message.id
        )
    }
}
