package tele.crypt.retrofit

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import tele.crypt.exceptions.ConnectionException
import tele.crypt.exceptions.ParseRetrofitException
import java.io.IOException

open class BaseRetrofitSource(
    config : RetrofitConfig
) {
    val retrofit = config.retrofit
    val moshi = config.moshi

    suspend fun <T> wrapRetrofitException(code: suspend () -> T): T {
        return try {
            code()
        } catch (ex: Exception) {
            when (ex) {
                is JsonDataException,
                is JsonEncodingException -> throw ParseRetrofitException(ex)
                is IOException -> throw ConnectionException(ex)
                else -> {
                    println("Undefined exception: ${ex.message}")
                    throw Exception(ex)
                }
            }
        }
    }

}