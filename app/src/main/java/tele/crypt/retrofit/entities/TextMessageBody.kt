package tele.crypt.retrofit.entities

import com.squareup.moshi.Json


data class TextMessageBody(
    @Json(name = "from") val from: String,
    @Json(name = "data") val data: Data
) {
    data class Data(
        @Json(name = "Text") val text : Text
    )

    data class Text(
        @Json(name = "text") val text : String
    )
}

