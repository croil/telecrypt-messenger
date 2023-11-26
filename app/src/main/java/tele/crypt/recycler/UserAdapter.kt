package tele.crypt.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import tele.crypt.telecrypt.databinding.RecycleViewBinding

class UserAdapter(
    private val users : List<User>,
    private val onClick: (User) -> Unit
) : RecyclerView.Adapter<UserViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val holder = UserViewHolder(RecycleViewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ))
        holder.name.setOnClickListener {
            onClick(users[holder.adapterPosition])
        }
        return holder
    }



    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val item = users[position]
        holder.bind(item)
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = users.size
}