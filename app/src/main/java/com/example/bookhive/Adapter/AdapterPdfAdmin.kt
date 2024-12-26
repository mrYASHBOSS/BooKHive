package com.example.bookhive.Adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.bookhive.filters.FilterPdfAdmin
import com.example.bookhive.Models.ModelPdf
import com.example.bookhive.MyApplication
import com.example.bookhive.activities.PdfDetailsActivity
import com.example.bookhive.databinding.RowPdfAdminBinding

class AdapterPdfAdmin : RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin>, Filterable {

    //context
    private var context: Context

    //arraylist to hold pdfs
    var pdfArrayList: ArrayList<ModelPdf>
    private val filterList: ArrayList<ModelPdf>

    //viewBinding
    private lateinit var binding: RowPdfAdminBinding

    private var filter: FilterPdfAdmin? = null

    //Constructor
    constructor(context: Context, pdfArrayList: ArrayList<ModelPdf>) {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfAdmin {
        //binding/inflate layout row_pdf_admin.xml
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderPdfAdmin(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPdfAdmin, position: Int) {

        //get data
        val model = pdfArrayList[position]
        val pdfId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
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
            null,
        )


        //load pdf size
        MyApplication.loadPdfSize(pdfUrl, title, holder.sizeTv)

        //Delete
        holder.moreBtn.setOnClickListener {
            moreOptionDialog(model, holder)
        }


        //handle item click,open PdfDetailActivity
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfDetailsActivity::class.java)
            intent.putExtra("bookId", pdfId)//used for load book
            context.startActivity(intent)
        }

    }

    private fun moreOptionDialog(model: ModelPdf, holder: HolderPdfAdmin) {
        val bookId = model.id
        val bookUri = model.uri
        val bookTitle = model.title

        val option = arrayOf("Delete")

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Are You Sure to delete ${bookTitle}?")
            .setItems(option) { dialog, position ->
                if (position == 0) {
                    //Delete
                    MyApplication.deleteBook(context, bookId, bookUri, bookTitle)
                }
            }.show()
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterPdfAdmin(filterList, this)
        }
        return filter as FilterPdfAdmin
    }

    //ViewHolder class to hold/init Ui view for row_pdf_admin.xml
    inner class HolderPdfAdmin(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //init ui view
        val pdfView = binding.pdfView
        val progressBar = binding.progressBar
        val titleTv = binding.titleTv
        val descriptionTv = binding.descriptionTv
        var categoryTv = binding.categoryTv
        val sizeTv = binding.sizeTv
        val dateTv = binding.dateTv
        val moreBtn = binding.moreBtn
        val pdfView1 = binding.pdfView1
    }
}
