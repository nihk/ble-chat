package nick.template.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import nick.template.data.local.Device
import nick.template.databinding.DeviceItemBinding
import nick.template.ui.OpenChatCallback

class DeviceAdapter(
    private val openChatCallback: OpenChatCallback
) : ListAdapter<Device, DeviceViewHolder>(DeviceDiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        return LayoutInflater.from(parent.context)
            .let { inflater -> DeviceItemBinding.inflate(inflater, parent, false) }
            .let { binding -> DeviceViewHolder(binding) }
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(getItem(position), openChatCallback)
    }
}

object DeviceDiffCallback : DiffUtil.ItemCallback<Device>() {
    override fun areItemsTheSame(oldItem: Device, newItem: Device): Boolean {
        return oldItem.address == newItem.address
    }

    override fun areContentsTheSame(oldItem: Device, newItem: Device): Boolean {
        return oldItem == newItem
    }
}

class DeviceViewHolder(private val binding: DeviceItemBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(device: Device, openChatCallback: OpenChatCallback) {
        binding.name.text = device.name.toString()
        binding.address.text = device.address
        binding.message.setOnClickListener {
            openChatCallback.with(device)
        }
    }
}
