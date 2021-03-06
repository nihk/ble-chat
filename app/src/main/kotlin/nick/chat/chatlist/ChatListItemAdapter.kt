package nick.chat.chatlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import nick.chat.databinding.ChatListItemBinding

class ChatListItemAdapter(
    private val converseWith: (ChatListItem) -> Unit
) : ListAdapter<ChatListItem, ChatListItemViewHolder>(ChatListItemDiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListItemViewHolder {
        return LayoutInflater.from(parent.context)
            .let { inflater -> ChatListItemBinding.inflate(inflater, parent, false) }
            .let { binding -> ChatListItemViewHolder(binding) }
    }

    override fun onBindViewHolder(holder: ChatListItemViewHolder, position: Int) {
        holder.bind(getItem(position), converseWith)
    }
}

object ChatListItemDiffCallback : DiffUtil.ItemCallback<ChatListItem>() {
    override fun areItemsTheSame(oldItem: ChatListItem, newItem: ChatListItem): Boolean {
        return oldItem.address == newItem.address
    }

    override fun areContentsTheSame(oldItem: ChatListItem, newItem: ChatListItem): Boolean {
        return oldItem == newItem
    }
}

class ChatListItemViewHolder(private val binding: ChatListItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(item: ChatListItem, chatWith: (ChatListItem) -> Unit) {
        binding.name.text = item.name.toString()
        binding.address.text = item.address
        binding.latest.text = item.latestMessage
        binding.latest.isVisible = item.latestMessage != null
        binding.message.setOnClickListener {
            chatWith(item)
        }
    }
}
