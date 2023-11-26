package tele.crypt.recycler


import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import tele.crypt.telecrypt.Utilities
import tele.crypt.telecrypt.databinding.RecycleViewBinding

class UserViewHolder(
    private val binding: RecycleViewBinding,
) : RecyclerView.ViewHolder(binding.root) {
    private val image: ImageView = binding.userImage
    val name: TextView = binding.username
    val lastMessage: TextView = binding.last
    val amount: TextView = binding.messageAmount
    val lastMessageData: TextView = binding.lastMessageData

    fun bind(user: User) {
        name.text = user.name
        if (user.image.isNotEmpty()) {
            val bitmap = Utilities.uncompress(user.image)
            if (bitmap != null) image.setImageBitmap(Utilities.cropBitmap(bitmap))
        }

    }
}

