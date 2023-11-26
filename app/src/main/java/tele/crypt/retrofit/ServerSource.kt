package tele.crypt.retrofit

import tele.crypt.retrofit.entities.ResponseMessageBody

interface ServerSource {
    suspend fun textRequest(sender: String, text: String): Int
    suspend fun imageRequest(sender: String, link: String): Int
    suspend fun request(
        lastKnownId: Int,
        reverse: Boolean,
        limit: Int = 20
    ): List<ResponseMessageBody>
}