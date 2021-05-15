package nick.template.ui.chatlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import nick.template.data.local.Device
import nick.template.databinding.DeviceItemBinding

class ChatListItemAdapter(
    private val converseWith: (ChatListItem) -> Unit
) : ListAdapter<ChatListItem, ChatListItemViewHolder>(ChatListItemDiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListItemViewHolder {
        return LayoutInflater.from(parent.context)
            .let { inflater -> DeviceItemBinding.inflate(inflater, parent, false) }
            .let { binding -> ChatListItemViewHolder(binding) }
    }

    override fun onBindViewHolder(holder: ChatListItemViewHolder, position: Int) {
        holder.bind(getItem(position), converseWith)
    }
}

object ChatListItemDiffCallback : DiffUtil.ItemCallback<ChatListItem>() {
    override fun areItemsTheSame(oldItem: ChatListItem, newItem: ChatListItem): Boolean {
        return oldItem.device.address == newItem.device.address
    }
    override fun areContentsTheSame(oldItem: ChatListItem, newItem: ChatListItem): Boolean {
        return oldItem == newItem
    }
}

class ChatListItemViewHolder(private val binding: DeviceItemBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: ChatListItem, chatWith: (ChatListItem) -> Unit) {
        val device = item.device
        binding.name.text = device.name.toString()
        binding.address.text = device.address
        binding.message.setOnClickListener {
            chatWith(item)
        }
    }
}
