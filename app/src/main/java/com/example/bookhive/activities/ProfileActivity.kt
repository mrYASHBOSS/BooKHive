package com.example.bookhive.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.bookhive.Adapter.AdapterPdfFav
import com.example.bookhive.Models.ModelPdf
import com.example.bookhive.MyApplication
import com.example.bookhive.R
import com.example.bookhive.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private var firebaseAuth: FirebaseAuth? = null
    private lateinit var booksArrayList: ArrayList<ModelPdf>
    private lateinit var adapterPdfFav: AdapterPdfFav

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        loadUserInfo()
        loadFavBook()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        binding.profileEditBtn.setOnClickListener {
            startActivity(Intent(this, ProfileEditActivity::class.java))
        }
    }

    private fun loadUserInfo() {
        //db reference to load user info
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth?.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get user info
                    val email = "${snapshot.child("email").value}"
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val userType = "${snapshot.child("userType").value}"
                    val formattedDate = MyApplication.formatTimeStamp(timestamp.toLong())
                    //set data
                    binding.nameTv.text = name
                    binding.emailTv.text = email
                    binding.MemberTv.text = formattedDate
                    binding.accountTypeTv.text = userType
                    //set image
                    try {
                        Glide.with(this@ProfileActivity)
                            .load(profileImage)
                            .placeholder(R.drawable.ic_person)
                            .into(binding.profileIv)
                    } catch (e: Exception) {
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun loadFavBook() {

        //init arraylist
        booksArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth?.uid!!).child("Favorites")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //clear arraylist, before starting adding data
                    booksArrayList.clear()
                    for (ds in snapshot.children) {
                        //get only id of the books, rest of the info we have loaded in adapter class
                        val bookId = "${ds.child("bookId").value}"
                        //set to model
                        val modelPdf = ModelPdf()
                        modelPdf.id = bookId
                        //add model to list
                        booksArrayList.add(modelPdf)
                    }
                    //set number of favorite books
                    binding.favoriteBooksCountTv.text = "${booksArrayList.size}"

                    adapterPdfFav = AdapterPdfFav(this@ProfileActivity, booksArrayList)
                    binding.favoritesRV.adapter = adapterPdfFav

                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

    }
}