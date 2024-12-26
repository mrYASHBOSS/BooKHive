package com.example.bookhive.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bookhive.Models.ModelPdf
import com.example.bookhive.MyApplication
import com.example.bookhive.activities.PdfDetailsActivity
import com.example.bookhive.databinding.RowPdfFavBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterPdfFav : RecyclerView.Adapter<AdapterPdfFav.HolderPdfFav> {

    //context
    private var context: Context

    //arraylist to hold pdfs
    private var booksArrayList: ArrayList<ModelPdf>

    //viewBinding
    private lateinit var binding: RowPdfFavBinding


    //Constructor
    constructor(context: Context, booksArrayList: ArrayList<ModelPdf>) {
        this.context = context
        this.booksArrayList = booksArrayList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfFav {
        //binding/inflate layout row_pdf_admin.xml
        binding = RowPdfFavBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderPdfFav(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPdfFav, position: Int) {

        //get data
        val model = booksArrayList[position]

        //load further details like book, pdf from url, pdf size
        loadBookDetails(model, holder)

        //handle click, start pdf list admin activity, also pas pdf id, title
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfDetailsActivity::class.java)
            intent.putExtra("bookId", model.id)
            context.startActivity(intent)
        }
        holder.removeFavBtn.setOnClickListener {
            MyApplication.removeFromFav(context, model.id)

        }
    }

    private fun loadBookDetails(model: ModelPdf, holder: HolderPdfFav) {
        val bookId = model.id
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get book info
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    //val downloadsCount = "${snapshot.child("downloadsCount").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val title = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val uri = "${snapshot.child("uri").value}"
                    val viewsCount = "${snapshot.child("viewsCount").value}"
                    //set data to model
                    model.isFavorite = true
                    model.title =title
                    model.description = description
                    model.categoryId = categoryId
                    model.timestamp = timestamp.toLong()
                    model.uid = uid
                    model.uri = uri
                    model.viewCount = viewsCount.toLong()
                    //format date
                    val date = MyApplication.formatTimeStamp(timestamp.toLong())
                    MyApplication.loadCategory(categoryId, holder.categoryTv)
                    MyApplication.loadPdfFromUrlSinglePage(
                        uri,
                        title,
                        holder.pdfView,
                        holder.progressBar,
                        null,
                    )
                    MyApplication.loadPdfSize(uri, title, holder.sizeTv)
                    holder.titleTv.text = title
                    holder.descriptionTv.text = description
                    holder.dateTv.text = date
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })


    }


    override fun getItemCount(): Int {
        return booksArrayList.size
    }


    //ViewHolder class to hold/init Ui view for row_pdf_admin.xml
    inner class HolderPdfFav(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //init ui view
        val pdfView = binding.pdfView
        val progressBar = binding.progressBar
        val titleTv = binding.titleTv
        val descriptionTv = binding.descriptionTv
        var categoryTv = binding.categoryTv
        val sizeTv = binding.sizeTv
        val dateTv = binding.dateTv
        val removeFavBtn = binding.removeFavBtn
    }


}