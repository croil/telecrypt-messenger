package tele.crypt.retrofit.entities


import com.squareup.moshi.Json
import tele.crypt.chat.Message

data class ResponseMessageBody(
    @field:Json(name = "id") val id: Int,
    @field:Json(name = "from") val from: String,
    @field:Json(name = "data") val data: Content,
    @field:Json(name = "time") val time: String
) {
    fun toMessage(): Message {
        return Message(
            message = data.Text?.text ?: "",
            link = data.Image?.link ?: "",
            sender = from,
            receiver = "night",
            dateTime = time,
            id = id,
        )
    }
}

data class Content(
    @field:Json(name = "Text") val Text: Text?,
    @field:Json(name = "Image") val Image: Image?
)

data class Image(
    @field:Json(name = "link") val link: String
)

data class Text(
    @field:Json(name = "text") val text: String
)
