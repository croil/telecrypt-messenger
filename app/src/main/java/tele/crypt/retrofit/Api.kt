package tele.crypt.retrofit

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import tele.crypt.retrofit.entities.ImageMessageBody
import tele.crypt.retrofit.entities.ResponseMessageBody
import tele.crypt.retrofit.entities.TextMessageBody


interface Api {


    @Headers(
        "Content-Type: application/json; charset=UTF-8"
    )
    @POST("1ch")
    suspend fun textRequest(
        @Body textMessageBody: TextMessageBody
    ): Response<Int>


    @POST("1ch")
    suspend fun imageRequest(
       @Body part : MultipartBody
    ): Response<Int>

    @GET("1ch")
    suspend fun request(
        @Query("lastKnownId") lastKnownId: Int,
        @Query("reverse") reverse: Boolean,
        @Query("limit") limit : Int = 20
    ): Response<List<ResponseMessageBody>>
}
