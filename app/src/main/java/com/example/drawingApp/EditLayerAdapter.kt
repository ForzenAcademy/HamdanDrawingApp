package com.example.drawingApp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

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
        val inflater = LayoutInflater.from(parent.context)
        return LayerViewHolder(
            inflater.inflate(R.layout.edit_layer_dialog, parent, false),
            onEditClick,
            onDeleteClick
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

}

class LayerViewModel(val text: String)

abstract class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bind(model: LayerViewModel)
}

class LayerViewHolder(
    val view: View,
    val onEdit: (LayerViewModel) -> Unit,
    val onDelete: (LayerViewModel) -> Unit
) : ViewHolder(view) {

    override fun bind(model: LayerViewModel) {
        view.findViewById<TextView>(R.id.editLayerName).text = model.text
        view.findViewById<ImageView>(R.id.layerEditBtn).setOnClickListener {
            onEdit(model)
        }
        view.findViewById<ImageView>(R.id.layerDeleteBtn).setOnClickListener {
            onDelete(model)
        }
    }
}