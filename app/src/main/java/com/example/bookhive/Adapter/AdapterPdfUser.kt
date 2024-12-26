package com.example.bookhive.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.bookhive.filters.FilterPdfUser
import com.example.bookhive.Models.ModelPdf
import com.example.bookhive.MyApplication
import com.example.bookhive.activities.PdfDetailsActivity
import com.example.bookhive.databinding.RowPdfUserBinding

class AdapterPdfUser : RecyclerView.Adapter<AdapterPdfUser.HolderPdfUser>, Filterable {

    //context
    private var context: Context

    //arraylist to hold pdfs
    var pdfArrayList: ArrayList<ModelPdf>
    private val filterList: ArrayList<ModelPdf>

    //viewBinding
    private lateinit var binding: RowPdfUserBinding

    private var filter: FilterPdfUser? = null

    //Constructor
    constructor(context: Context, pdfArrayList: ArrayList<ModelPdf>) {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfUser {
        //binding/inflate layout row_pdf_admin.xml
        binding = RowPdfUserBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderPdfUser(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPdfUser, position: Int) {

        //get data
        val model = pdfArrayList[position]
        val bookId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val uid = model.uid
        val pdfUrl = model.uri
        val timestamp = model.timestamp


        //convert timestamp to dd/MM/yyyy format
        val formattedDate = MyApplication.formatTimeStamp(timestamp)


        //set data
        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.dateTv.text = formattedDate

        //load further details like category, pdf from url, pdf size
        //load category
        MyApplication.loadCategory(categoryId, holder.categoryTv)


        //we don't need page number here, pas null for page number || load pdf thumbnail
        MyApplication.loadPdfFromUrlSinglePage(
            pdfUrl,
            title,
            holder.pdfView,
            holder.progressBar,
            null
        )

        MyApplication.loadCategory(categoryId, holder.categoryTv)

        //load pdf size
        MyApplication.loadPdfSize(pdfUrl, title, holder.sizeTv)

        //handle item click,open PdfDetailActivity
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfDetailsActivity::class.java)
            intent.putExtra("bookId", bookId)//used for load book
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return pdfArrayList.size
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterPdfUser(filterList, this)
        }
        return filter as FilterPdfUser
    }

    //ViewHolder class to hold/init Ui view for row_pdf_admin.xml
    inner class HolderPdfUser(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //init ui view
        val pdfView = binding.pdfView
        val progressBar = binding.progressBar
        val titleTv = binding.titleTv
        val descriptionTv = binding.descriptionTv
        var categoryTv = binding.categoryTv
        val sizeTv = binding.sizeTv
        val dateTv = binding.dateTv
        //val moreBtn = binding.moreBtn
    }
}