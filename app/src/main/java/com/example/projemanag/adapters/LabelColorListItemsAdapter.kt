package com.example.projemanag.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.projemanag.databinding.ItemLabelColorBinding

open class LabelColorListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<String>,
    private val mSelectedColor: String): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemLabelColorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = list[position]
        if(holder is MyViewHolder){
            holder.binding.viewMain.setBackgroundColor(Color.parseColor(item))
            if(item == mSelectedColor){
                holder.binding.ivSelectedColor.visibility = View.VISIBLE
            }else{
                holder.binding.ivSelectedColor.visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                if(onItemClickListener != null){
                    onItemClickListener!!.onClick(position,item)
                }
            }

        }
    }

    override fun getItemCount(): Int {
        return list.size
    }


    /**
     * An interface for onclick items.
     */
    interface OnItemClickListener {
        fun onClick(
            position: Int,
            color: String
        )
    }

    private class MyViewHolder(var binding: ItemLabelColorBinding): RecyclerView.ViewHolder(binding.root)
}