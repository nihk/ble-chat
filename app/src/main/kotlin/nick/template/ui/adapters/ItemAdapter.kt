package nick.template.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import nick.template.databinding.ItemBinding

class ItemAdapter : ListAdapter<Any, ItemViewHolder>(ItemDiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return LayoutInflater.from(parent.context)
            .let { inflater -> ItemBinding.inflate(inflater, parent, false) }
            .let { binding -> ItemViewHolder(binding) }
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

object ItemDiffCallback : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        TODO("Not yet implemented")
    }

    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        TODO("Not yet implemented")
    }
}

class ItemViewHolder(private val binding: ItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(any: Any) {

    }
}