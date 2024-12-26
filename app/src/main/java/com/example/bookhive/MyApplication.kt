package com.example.bookhive

import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.bookhive.Adapter.AdapterPdfFav
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.shashank.sony.fancytoastlib.FancyToast
import java.util.Calendar
import java.util.Locale

class MyApplication : Application() {


    override fun onCreate() {
        super.onCreate()
    }

    companion object {
        //created a static method to convert timestamp to proper date format, so
        fun formatTimeStamp(timestamp: Long): String {
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = timestamp
            //format dd/MM/yyyy
            return DateFormat.format("dd/MM/yyyy", cal).toString()
        }
        //function to get pdf size
        fun loadPdfSize(pdfUrl: String, pdfTitle: String, sizeTv: TextView) {

            //using url we can get file and its metadata from firebase storage
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.metadata.addOnSuccessListener { storageMetaData ->
                val bytes = storageMetaData.sizeBytes.toDouble()
                //convert bytes to KB/MB
                val kb = bytes / 1024
                val mb = kb / 1024
                if (mb > 1) {
                    sizeTv.text = "${String.format("%.2f", mb)} MB"
                } else if (kb >= 1) {
                    sizeTv.text = "${String.format("%.2f", kb)} KB"
                } else {
                    sizeTv.text = "${String.format("%.2f", bytes)} bytes"
                }
            }.addOnFailureListener { e ->
                //failed to get metadata
            }
        }

        fun loadPdfFromUrlSinglePage(
            pdfUrl: String,
            pdfTitle: String,
            pdfView1: PDFView,
            progressBar: ProgressBar,
            pagesTv: TextView?,
        ) {
            //using url we can get file and its metadata from firebase storage
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.getBytes(Constants.MAX_BYTES_PDF).addOnSuccessListener { bytes ->
                //Set To PDFVIEW

                pdfView1.fromBytes(bytes)
                    .pages(0)
                    .spacing(0)
                    .swipeHorizontal(false)
                    .enableSwipe(false).onError { t ->
                        progressBar.visibility = View.INVISIBLE
                    }.onPageError { page, t ->
                        progressBar.visibility = View.INVISIBLE
                    }
                    .onLoad { nbPages ->
                        progressBar.visibility = View.INVISIBLE

                        if (pagesTv != null) {
                            pagesTv.text = "$nbPages"
                        }
                    }
                    .load()
            }.addOnFailureListener { e ->
                //failed to get metadata
            }
        }

        fun loadCategory(categoryId: String, categoryTv: TextView) {
            val ref = FirebaseDatabase.getInstance().getReference("Categories")
            ref.child(categoryId)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val category = "${snapshot.child("category").value}"
                        categoryTv.text = category
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }

        fun deleteBook(context: Context, bookId: String, bookUri: String, bookTitle: String) {
            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Please Wait")
            progressDialog.setMessage("Deleting $bookTitle")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUri)
            storageReference.delete()
                .addOnSuccessListener {
                    val ref = FirebaseDatabase.getInstance().getReference("Books")
                    ref.child(bookId)
                        .removeValue()
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            FancyToast.makeText(
                                context,
                                "Successfully deleted...",
                                FancyToast.LENGTH_SHORT,
                                FancyToast.SUCCESS,
                                true
                            ).show()
                        }
                        .addOnFailureListener { e ->
                            progressDialog.dismiss()
                            FancyToast.makeText(
                                context,
                                "Failed to delete due to ${e.message}",
                                FancyToast.LENGTH_SHORT,
                                FancyToast.ERROR,
                                true
                            ).show()
                        }
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    FancyToast.makeText(
                        context,
                        "Failed to delete due to ${e.message}",
                        FancyToast.LENGTH_SHORT,
                        FancyToast.ERROR,
                        true
                    ).show()
                }
        }

        fun incrementBookViewCount(bookId: String) {
            //1) Get current book views count
            val ref = FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get views count
                        var viewsCount = "${snapshot.child("viewsCount").value}"
                        if (viewsCount == "" || viewsCount == "null") {
                            viewsCount = "0";
                        }
                        //2 Increment views count
                        val newViewsCount = viewsCount.toLong() + 1
                        //setup data to update in db
                        val hashMap = HashMap<String, Any>()
                        hashMap["viewsCount"] = newViewsCount
                        //set to db
                        val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                        dbRef.child(bookId)
                            .updateChildren(hashMap)
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        }

        public fun removeFromFav(context: Context, bookId: String) {

            val firebaseAuth = FirebaseAuth.getInstance()

            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
                .removeValue()
                .addOnSuccessListener {
                    FancyToast.makeText(
                        context,
                        "Removed From Favorites",
                        FancyToast.LENGTH_SHORT,
                        FancyToast.SUCCESS,
                        true
                    ).show()

                }
                .addOnFailureListener { e ->
                    FancyToast.makeText(
                        context,
                        "Not removed due to ${e.message}",
                        FancyToast.LENGTH_SHORT,
                        FancyToast.ERROR,
                        true
                    ).show()
                }
        }

    }
}

/*instead of making new function load PdfPageCount() to just load pages count it would be more good to use some existing function to do that
              i.e. Load PdfFromUrlSingle Page
          * We will add another parameter of type TextView e.g. pagesTv
          * Whenever we call that function
          * 1) if we require page numbers we will pass pagesTv (TextView)
          * 2) If we don't require page number we will pass null
              And in function if pagesTv (TextView) parameter is not null we will set the page number count*/