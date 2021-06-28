package nick.chat.conversation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import nick.chat.databinding.ConversationItemBinding

class ConversationItemAdapter : ListAdapter<ConversationItem, ConversationItemViewHolder>(ConversationItemDiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationItemViewHolder {
        return LayoutInflater.from(parent.context)
            .let { inflater -> ConversationItemBinding.inflate(inflater, parent, false) }
            .let { binding -> ConversationItemViewHolder(binding) }
    }

    override fun onBindViewHolder(holder: ConversationItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

object ConversationItemDiffCallback : DiffUtil.ItemCallback<ConversationItem>() {
    override fun areItemsTheSame(oldItem: ConversationItem, newItem: ConversationItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ConversationItem, newItem: ConversationItem): Boolean {
        return oldItem == newItem
    }
}

class ConversationItemViewHolder(
    private val binding: ConversationItemBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: ConversationItem) {
        binding.message.text = item.message
        val bias = if (item.isMe) 1.0f else 0.0f
        ConstraintSet().apply {
            clone(binding.root)
            setHorizontalBias(binding.message.id, bias)
            applyTo(binding.root)
        }
    }
}
