package com.example.bookhive.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.bookhive.filters.FilterCategory
import com.example.bookhive.Models.ModelCategory
import com.example.bookhive.activities.PdfListAdminActivity
import com.example.bookhive.databinding.RowCategoryBinding
import com.google.firebase.database.FirebaseDatabase
import com.shashank.sony.fancytoastlib.FancyToast

class AdapterCategory : RecyclerView.Adapter<AdapterCategory.HolderCategory>, Filterable {

    private val context: Context
    public var categoryArrayList: ArrayList<ModelCategory>
    private var filterList: ArrayList<ModelCategory>
    private var filter: FilterCategory? = null
    //private var position: Int = 0


    private lateinit var binding: RowCategoryBinding

    constructor(context: Context, categoryArrayList: ArrayList<ModelCategory>) {
        this.context = context
        this.categoryArrayList = categoryArrayList
        this.filterList = categoryArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategory {
        //inflate/bind row_category.xml
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderCategory(binding.root)
    }

    override fun onBindViewHolder(holder: HolderCategory, position: Int) {
        //get Data , set data ,handle click etc
        val model = categoryArrayList[position]
        val id = model.id
        val category = model.category
        val uid = model.uid
        val timestamp = model.timestamp

        //set data
        holder.categoryTv.text = category
        //handle delete click
        holder.deleteBtn.setOnClickListener {
            //confirm before delete
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Delete").setMessage("Are you sure want to delete this category ?")
                .setPositiveButton("Confirm") { a, d ->
                    FancyToast.makeText(
                        context, "Deleting...", FancyToast.LENGTH_SHORT, FancyToast.INFO, true
                    ).show()
                    deleteCategory(model, holder)
                }.setNegativeButton("Cancel") { a, d ->
                    a.dismiss()
                }.show()
        }
        //handle click, start pdf list admin activity, also pas pdf id, title
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfListAdminActivity::class.java)
            intent.putExtra("categoryId", id)
            intent.putExtra("category", category)
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return categoryArrayList.size
    }


    private fun deleteCategory(model: ModelCategory, holder: HolderCategory) {
        //get id of category to delete
        val id = model.id
        //firebase DB > Category > CategoryId
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child(id)
            .removeValue()
            .addOnSuccessListener {
                FancyToast.makeText(context, "Deleted...", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show()
//                categoryArrayList.removeAt(position)
//                notifyItemRemoved(position)
//                notifyItemRangeChanged(position, categoryArrayList.size)
            }.addOnFailureListener { e ->
                FancyToast.makeText(context, "Unable to delete due to ${e.message}...", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show()
            }
    }

    //ViewHolder class to hold/init Ui view for row_category.xml
    inner class HolderCategory(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //init ui view
        var categoryTv: TextView = binding.categoryTv
        var deleteBtn: ImageButton = binding.deleteBtn
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterCategory(filterList, this)
        }
        return filter as FilterCategory
    }


}