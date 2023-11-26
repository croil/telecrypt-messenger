package tele.crypt.chat


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import tele.crypt.telecrypt.Constants
import tele.crypt.telecrypt.R
import tele.crypt.telecrypt.Utilities
import tele.crypt.telecrypt.databinding.RecieveMessageItemBinding
import tele.crypt.telecrypt.databinding.SendMessageItemBinding


class MessageDiffCallback(
    private val oldList : List<Message>,
    private val newList : List<Message>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldMessage = oldList[oldItemPosition]
        val newMessage = newList[newItemPosition]
        return oldMessage == newMessage
    }

}


class ChatAdapter(
    private val me: String,
    private val callback: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var messages = emptyList<Message>()
        set(newList) {
            val diffCallback = MessageDiffCallback(field, newList)
            val diffResult = DiffUtil.calculateDiff(diffCallback)
            field = newList
            diffResult.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) SentMessageHolder(
            SendMessageItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        ) else ReceivedMessageHolder(
            RecieveMessageItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position == 10) callback()
        if (getItemViewType(position) == 0) (holder as SentMessageHolder).bind(messages[position])
        else (holder as ReceivedMessageHolder).bind(messages[position])
    }

    override fun getItemCount() = messages.size

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].sender == me) 0 else 1
    }

    inner class SentMessageHolder(
        private val binding: SendMessageItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.sendMessage.text = message.message
            binding.senderName.text = message.sender
            binding.senderName.setTextColor(Utilities.getRandomColor())
            binding.senderDate.text = Utilities.getReadableDate(message.dateTime)
            binding.sendImageMessage.contentDescription = message.link
            if (message.link.isNotBlank()) {
                Picasso.get()
                    .load("${Constants.URL}/thumb/${message.link}")
                    .into(binding.sendImageMessage)
            } else binding.sendImageMessage.setImageBitmap(null)
        }
    }

    inner class ReceivedMessageHolder(
        private val binding: RecieveMessageItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.receiveMessage.text = message.message
            binding.receiverName.text = message.sender
            binding.receiverName.setTextColor(Utilities.getRandomColor())
            binding.receiverDate.text = Utilities.getReadableDate(message.dateTime)
            binding.receiveImageMessage.contentDescription = message.link
            binding.receivedUserImage.setImageResource(R.drawable.ic_avatar)
            binding.receivedUserImage.setColorFilter(Utilities.getRandomColor())
            if (message.link.isNotBlank()) {
                Picasso.get()
                    .load("${Constants.URL}/thumb/${message.link}")
                    .into(binding.receiveImageMessage)
            } else binding.receiveImageMessage.setImageBitmap(null)
        }
    }

}