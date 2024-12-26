package com.example.bookhive.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.bookhive.Adapter.AdapterPdfFav
import com.example.bookhive.MyApplication
import com.example.bookhive.R
import com.example.bookhive.databinding.ActivityPdfDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.shashank.sony.fancytoastlib.FancyToast


class PdfDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPdfDetailsBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var adapterPdfFav: AdapterPdfFav
    private var bookId = ""
    private var bookTitle = ""
    private var bookUri = ""
    private var isInMyFav = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        if (firebaseAuth.currentUser != null) {
            checkIsFav()
        }

        bookId = intent.getStringExtra("bookId")!!
        binding.favoriteBtn.text="Add Favourite"
        MyApplication.incrementBookViewCount(bookId)

        loadBookDetails()

        binding.backBtn.setOnClickListener { onBackPressed() }

        binding.readBookBtn.setOnClickListener {
            val intent = Intent(this, PdfViewActivity::class.java)
            intent.putExtra("bookId", bookId)
            startActivity(intent)
        }

        binding.favoriteBtn.setOnClickListener {

            if (firebaseAuth.currentUser == null) {
                FancyToast.makeText(
                    this,
                    "You're not logged in",
                    FancyToast.LENGTH_SHORT,
                    FancyToast.INFO,
                    true
                ).show()
            } else {
                if (isInMyFav) {
                    binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0,
                        R.drawable.ic_favorite,
                        0,
                        0
                    )
                    MyApplication.removeFromFav(this, bookId)

                } else {
                    addToFav()
                    Log.e("asdfsdf","addToFavCalled")
                }
            }

        }

    }

    private fun loadBookDetails() {

        Log.e("asdfsdf","loadBookDetailsCalled")

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val downloadsCount = "${snapshot.child("downloadsCount").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    bookTitle = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    bookUri = "${snapshot.child("uri").value}"
                    val viewCount = "${snapshot.child("viewsCount").value}"
                    //format Data
                    val date = MyApplication.formatTimeStamp(timestamp.toLong())
                    //load pdf category
                    MyApplication.loadCategory(categoryId, binding.categoryTv)
                    //pdf preview
                    MyApplication.loadPdfFromUrlSinglePage(
                        "$bookUri",
                        "$bookTitle",
                        binding.pdfView,
                        binding.progressBar,
                        binding.pagesTv,
                    )
                    //pdf size
                    MyApplication.loadPdfSize("$bookUri", "$bookTitle", binding.sizeTv)
                    //set Data
                    binding.titleTv.text = bookTitle
                    binding.descrptionTv.text = description
                    binding.viewTv.text = viewCount
//                    binding.downloadTv.text = downloadsCount
                    binding.dateTv.text = date

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun checkIsFav() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isInMyFav = snapshot.exists()
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        Log.e("asdfsdf","checkIsFav")

    }

    private fun addToFav() {
        val timestamp = System.currentTimeMillis()
        val hashMap = HashMap<String, Any>()
        hashMap["bookId"] = bookId
        hashMap["timestamp"] = timestamp

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .setValue(hashMap)
            .addOnSuccessListener {
                FancyToast.makeText(
                    this,
                    "Added To Favorites",
                    FancyToast.LENGTH_SHORT,
                    FancyToast.SUCCESS,
                    true
                ).show()

                binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    0,
                    R.drawable.ic_filled_favorite,
                    0,
                    0
                )
                binding.favoriteBtn.text = "Remove Favorite"

            }
            .addOnFailureListener { e ->
                FancyToast.makeText(
                    this,
                    "Not Added due to ${e.message}",
                    FancyToast.LENGTH_SHORT,
                    FancyToast.ERROR,
                    true
                ).show()
            }
    }

}















































