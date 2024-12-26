package com.example.bookhive.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import com.example.bookhive.Adapter.AdapterCategory
import com.example.bookhive.Models.ModelCategory
import com.example.bookhive.databinding.ActivityDashboardAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class DashboardAdminActivity : AppCompatActivity() {
    private var binding: ActivityDashboardAdminBinding? = null
    private var firebaseAuth: FirebaseAuth? = null
    private var categoryArrayList: ArrayList<ModelCategory>? = null
    private var adapterCategory: AdapterCategory? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()
        localCategories()

        //search
        binding?.searchEt?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //called as and when user type anything
                try {
                    adapterCategory?.filter?.filter(s)
                } catch (_: Exception) {

                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })

        //handle click,logout
        binding?.logoutBtn?.setOnClickListener {
            firebaseAuth?.signOut()
            checkUser()
        }

        //handle click,start add category page
        binding?.addCategoryBtn?.setOnClickListener {
            startActivity(Intent(this, CategoryAddActivity::class.java))
        }

        //handle click,start add pdf page
        binding?.addPdfFab?.setOnClickListener {
            startActivity(Intent(this, PdfAddActivity::class.java))
        }

        binding?.profileBtn?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun localCategories() {
        //init arrayList
        categoryArrayList = ArrayList()

        //get all categories from database.. firebase DB . Categories
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before starting adding data into it
                categoryArrayList?.clear()
                for (ds in snapshot.children) {
                    //get Data from model
                    val model = ds.getValue(ModelCategory::class.java)
                    //add to arrayList
                    categoryArrayList?.add(model!!)
                }
                adapterCategory = AdapterCategory(this@DashboardAdminActivity, categoryArrayList!!)
                binding?.categoriesRV?.adapter = adapterCategory
                //adapterCategory?.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun checkUser() {

        val firebaseUser = firebaseAuth?.currentUser
        if (firebaseUser == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            val email = firebaseUser.email
            binding?.subTitleTv?.text = email
        }
    }
}