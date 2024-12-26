package com.example.bookhive.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.bookhive.Constants
import com.example.bookhive.databinding.ActivityPdfViewBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.shashank.sony.fancytoastlib.FancyToast


class PdfViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPdfViewBinding
    var bookId = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bookId = intent.getStringExtra("bookId")!!
        loadBook()
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadBook() {

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val pdfUrl = snapshot.child("uri").value
                    loadBookFromUri("$pdfUrl")

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

    }

    private fun loadBookFromUri(pdfUri: String) {

        val reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUri)
        reference.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener { bytes ->
                binding.pdfView.fromBytes(bytes).swipeHorizontal(false)
                    .onPageChange { page, pageCount ->
                        val currentPage = page + 1
                        binding.subTitleTv.text = "$currentPage/$pageCount"
                    }
                    .onError { t ->
                        FancyToast.makeText(
                            this,
                            "onError ${t.message}",
                            FancyToast.LENGTH_SHORT,
                            FancyToast.ERROR,
                            true
                        ).show()
                    }
                    .onPageError { page, t ->
                        FancyToast.makeText(
                            this,
                            "Page error ${t.message}",
                            FancyToast.LENGTH_SHORT,
                            FancyToast.ERROR,
                            true
                        ).show()
                    }
                    .load()
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                FancyToast.makeText(
                    this,
                    "not load error ${e.message}",
                    FancyToast.LENGTH_SHORT,
                    FancyToast.ERROR,
                    true
                ).show()
                binding.progressBar.visibility = View.GONE
            }

    }
}





































