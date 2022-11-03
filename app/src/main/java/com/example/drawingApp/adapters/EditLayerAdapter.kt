package com.example.drawingApp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.drawingApp.databinding.EditLayerDialogBinding

class EditLayerAdapter(
    private val data: List<LayerViewModel>,
    /**
     * called when the edit image is clicked in each view of the recycler view
     * determines what will be performed when clicked
     * currently allows the editing of the layer at that position of data
     */
    private val onEditClick: (LayerViewModel) -> Unit,
    /**
     * called when the delete image is clicked in each view of the recycler view
     * determines what will be performed when clicked
     * currently deletes the layer at that position of data
     */
    private val onDeleteClick: (LayerViewModel) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rowBinding =
            EditLayerDialogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LayerViewHolder(
            rowBinding,
            onEditClick,
            onDeleteClick,
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

}

class LayerViewModel(val text: String)

abstract class ViewHolder(rowBinding: EditLayerDialogBinding) :
    RecyclerView.ViewHolder(rowBinding.root) {
    abstract fun bind(model: LayerViewModel)
}

class LayerViewHolder(
    val binding: EditLayerDialogBinding,
    val onEdit: (LayerViewModel) -> Unit,
    val onDelete: (LayerViewModel) -> Unit,
) : ViewHolder(binding) {

    override fun bind(model: LayerViewModel) {
        binding.editLayerName.text = model.text
        binding.layerEditBtn.setOnClickListener {
            onEdit(model)
        }
        binding.layerDeleteBtn.setOnClickListener {
            onDelete(model)
        }
    }
}