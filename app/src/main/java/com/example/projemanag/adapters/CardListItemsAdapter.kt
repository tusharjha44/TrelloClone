package com.example.projemanag.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projemanag.activities.TaskListActivity
import com.example.projemanag.databinding.ItemCardBinding
import com.example.projemanag.models.Card
import com.example.projemanag.models.SelectedMembers

open class CardListItemsAdapter
    (private val context: Context,
     private var list: ArrayList<Card>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val model = list[position]

        if(holder is MyViewHolder){

            if(model.labelColor.isNotEmpty()){
                holder.binding.viewLabelColor.visibility = ViewGroup.VISIBLE
                holder.binding.viewLabelColor.
                        setBackgroundColor(Color.parseColor(model.labelColor))
            }else{
                holder.binding.viewLabelColor.visibility = ViewGroup.GONE
            }
            holder.binding.tvCardName.text = model.name

            if((context as TaskListActivity).mAssignedMemberDetails.size > 0){
                val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()
                for(i in context.mAssignedMemberDetails.indices){
                    for(j in model.assignedTo){
                        if(context.mAssignedMemberDetails[i].id == j){
                            val selectedMembers = SelectedMembers(
                                context.mAssignedMemberDetails[i].id,
                                context.mAssignedMemberDetails[i].image)

                            selectedMembersList.add(selectedMembers)
                        }
                    }
                }

                if(selectedMembersList.size > 0){
                    if(selectedMembersList.size == 1 && selectedMembersList[0].id == model.createdBy){
                        holder.binding.rvCardSelectedMembersList.visibility = View.GONE
                    }else{
                        holder.binding.rvCardSelectedMembersList.visibility = View.VISIBLE
                    }

                    holder.binding.rvCardSelectedMembersList.layoutManager =
                        GridLayoutManager(context,4)

                    val adapter = CardMemberListItemsAdapter(
                        context,selectedMembersList,false)

                    holder.binding.rvCardSelectedMembersList.adapter = adapter

                    adapter.setOnClickListener(
                        object : CardMemberListItemsAdapter.OnClickListener{
                            override fun onClick() {
                                if(onClickListener != null){
                                    onClickListener!!.onClick(position)
                                }
                            }

                        })

                }else{
                    holder.binding.rvCardSelectedMembersList.visibility = View.GONE
                }

            }


            holder.itemView.setOnClickListener {
                if(onClickListener != null){
                    onClickListener!!.onClick(position)
                }
            }

        }
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private class MyViewHolder(var binding: ItemCardBinding): RecyclerView.ViewHolder(binding.root)

}