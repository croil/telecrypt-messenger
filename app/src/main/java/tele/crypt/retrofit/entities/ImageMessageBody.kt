package tele.crypt.retrofit.entities

import com.squareup.moshi.Json

data class ImageMessageBody(
    @Json(name = "from") val from: String
)

