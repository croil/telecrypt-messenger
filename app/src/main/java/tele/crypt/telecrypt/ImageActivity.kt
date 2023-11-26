package tele.crypt.telecrypt

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.icu.number.NumberFormatter.with
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.squareup.picasso.Picasso
import tele.crypt.telecrypt.Utilities.parser
import tele.crypt.telecrypt.databinding.ActivityImageBinding
import java.io.File
import java.io.FileOutputStream


class ImageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityImageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val directory = intent.getStringExtra(Constants.PATH).toString()
        val imagePath = "img/${intent.getStringExtra(Constants.IMAGE_LINK)}"
        val serverPath = "${Constants.URL}/$imagePath"
        val filePath = "$directory/${parser(imagePath)}"
        loadImage(filePath, serverPath)
        binding.backChatButton.setOnClickListener {
            val imageIntent = Intent(this@ImageActivity, ChannelActivity::class.java)
            imageIntent.putExtra(
                Constants.MAIN_USER_ID,
                intent.getStringExtra(Constants.MAIN_USER_ID)
            )
            startActivity(imageIntent)
            finish()
        }
    }

    private enum class FORMATS {
        PNG, JPG, JPEG
    }

    private fun saveImageIntoFile(bitmap: Bitmap, file: File) {
        var format: FORMATS = FORMATS.PNG
        val lowerCaseLink = file.name.lowercase()
        if (lowerCaseLink.endsWith("png")) format = FORMATS.PNG
        if (lowerCaseLink.endsWith("jpg")) format = FORMATS.JPG
        if (lowerCaseLink.endsWith("jpeg")) format = FORMATS.JPEG
        if (lowerCaseLink.endsWith("webp")) format = FORMATS.PNG
        val outputStream = FileOutputStream(file)
        val compressFormat = when (format) {
            FORMATS.PNG -> Bitmap.CompressFormat.PNG
            FORMATS.JPG, FORMATS.JPEG -> Bitmap.CompressFormat.JPEG
        }
        bitmap.compress(compressFormat, 100, outputStream)
    }


    private fun loadImage(filePath: String, serverPath: String) {
        val file = File(filePath)
        try {
            binding.goodImage.setImageBitmap(BitmapFactory.decodeFile(filePath))
        } catch (ex: Exception) {
            println("Error while loading image from $filePath: $ex")
        } finally {
            if (!file.exists()) {
                try {
                    Glide.with(this)
                        .asBitmap()
                        .load(serverPath)
                        .into(object : CustomTarget<Bitmap>(){
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                binding.goodImage.setImageBitmap(resource)
                                saveImageIntoFile(resource, file)
                            }
                            override fun onLoadCleared(placeholder: Drawable?) {}
                        })

                } catch (ex: Exception) {
                    binding.goodImage.setImageResource(R.drawable.ic_not_found)
                }
            }
        }
    }
}



