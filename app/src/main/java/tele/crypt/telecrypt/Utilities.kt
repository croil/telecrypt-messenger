package tele.crypt.telecrypt

import android.graphics.*
import android.icu.text.SimpleDateFormat
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*


object Utilities {
    fun compress(bitmap: Bitmap?): String {
        return if (bitmap == null) {
            ""
        } else {
            try {
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream)
                Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
            } catch (ex : Exception) {
                ""
            }
        }
    }

    fun uncompress(enc: String): Bitmap? {
        return try {
            val dec = Base64.decode(enc, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(dec, 0, dec.size)
        } catch (ex : Exception) {
            null
        }
    }

    fun cropBitmap(hardBitmap: Bitmap): Bitmap {
        val config = Bitmap.Config.ARGB_8888
        val bitmap = hardBitmap.copy(config, false)
        val width = bitmap.width
        val height = bitmap.height
        val output = if (width > height) {
            Bitmap.createBitmap(height, height, config)
        } else {
            Bitmap.createBitmap(width, width, config)
        }
        val canvas = Canvas(output)
        val color = -0xbdbdbe
        val paint = Paint()
        val rect = Rect(0, 0, width, height)
        val r = if (width > height) {
            (height / 2).toFloat()
        } else {
            (width / 2).toFloat()
        }
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawCircle(r, r, r, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
    }
    fun getRandomColor(): Int {
        val rnd = Random()
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
    }
    //TODO: #2 Normal data
    fun getReadableDate(unix: String): String {
        return try {
            val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
            val netDate = Date(unix.toLong() * 1000)
            sdf.format(netDate)
        } catch (e: Exception) {
            e.toString()
        }
    }

    fun parser(s : String) : String {
        return s.replace('/', '-')
    }
    fun writeFromURL(stream : InputStream, path : String) {
        val output = FileOutputStream(path)
        val length = 1024
        output.use {
            val buffer = ByteArray(length)
            var bytesRead: Int
            while (stream.read(buffer, 0, length).also { bytesRead = it; } >= 0) {
                output.write(buffer, 0, bytesRead)
            }
        }
    }
}