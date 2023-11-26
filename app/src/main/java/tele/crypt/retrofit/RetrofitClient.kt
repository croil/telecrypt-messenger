package tele.crypt.retrofit

import okhttp3.Headers.Companion.headersOf
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import tele.crypt.retrofit.entities.ImageMessageBody
import tele.crypt.retrofit.entities.ResponseMessageBody
import tele.crypt.retrofit.entities.TextMessageBody
import tele.crypt.telecrypt.Constants
import java.io.File
import java.io.FileNotFoundException
import java.net.URLConnection


class RetrofitClient(
    config: RetrofitConfig
) : BaseRetrofitSource(config), ServerSource {
    private val api: Api = retrofit.create(Api::class.java)

    private fun <T> responseCheck(response: Response<T>): T? {
        return if (response.isSuccessful) {
            response.body()
        } else {
            println("Couldn't make a request with response code ${response.code()}")
            null
        }
    }


    override suspend fun textRequest(sender: String, text: String): Int = wrapRetrofitException {
        val response = api.textRequest(
            TextMessageBody(
                sender, TextMessageBody.Data(
                    TextMessageBody.Text(text)
                )
            )
        )
        return@wrapRetrofitException responseCheck(response) ?: -1
    }

    override suspend fun imageRequest(sender: String, link: String): Int = wrapRetrofitException {
        val file = File(link)
        if (!file.exists()) {
            println("File not found")
            throw FileNotFoundException()
        }
        val adapter = moshi.adapter(ImageMessageBody::class.java)
        val contentType = URLConnection.guessContentTypeFromName(link).toMediaType()
        val body = MultipartBody.Builder()
            .setType("multipart/form-data".toMediaType())
            .addFormDataPart(Constants.JSON_NAME, adapter.toJson(ImageMessageBody(sender)))
            .addPart(headersOf(Constants.PICTURE_NAME, link), file.asRequestBody(contentType))
            .build()


        return@wrapRetrofitException responseCheck(api.imageRequest(body)) ?: -1
    }

    override suspend fun request(lastKnownId: Int, reverse: Boolean, limit : Int): List<ResponseMessageBody> =
        wrapRetrofitException {
            return@wrapRetrofitException responseCheck(api.request(lastKnownId, reverse, limit))
                ?: emptyList()
        }
}