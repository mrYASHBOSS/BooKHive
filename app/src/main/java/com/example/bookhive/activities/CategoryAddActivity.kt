package com.example.bookhive.activities

import android.app.ProgressDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bookhive.databinding.ActivityCategoryAddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.shashank.sony.fancytoastlib.FancyToast

class CategoryAddActivity : AppCompatActivity() {
    private  var binding: ActivityCategoryAddBinding? = null
    private  var firebaseAuth: FirebaseAuth? = null
    private  var progressDialog: ProgressDialog? = null
    private  var ref: DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryAddBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        ref = FirebaseDatabase.getInstance().getReference("Categories")

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog?.setTitle("Please Wait...")
        progressDialog?.setCanceledOnTouchOutside(false)

        binding?.backBtn?.setOnClickListener {
            onBackPressed()
        }

        binding?.submitBtn?.setOnClickListener {
            validateData()
        }
    }

    private var category = ""

    private fun validateData() {

        category = binding?.categoryEt?.text.toString().trim()

        if (category.isEmpty()) {
            FancyToast.makeText(
                this,
                "Enter Category",
                FancyToast.LENGTH_SHORT,
                FancyToast.ERROR,
                true
            ).show()
        } else {
            addCategoryFirebase()
        }
    }

    private fun addCategoryFirebase() {

        progressDialog?.dismiss()

        val timestamp = System.currentTimeMillis()

        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$timestamp"
        hashMap["category"] = category
        hashMap["timestamp"] = timestamp
        hashMap["uid"] = "${firebaseAuth?.uid}"

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child("$timestamp").setValue(hashMap).addOnSuccessListener {
            progressDialog?.dismiss()
            FancyToast.makeText(
                this,
                "Added Successfully...",
                FancyToast.LENGTH_SHORT,
                FancyToast.SUCCESS,
                true
            ).show()
            //hide keyboard on button click
            finish()
        }.addOnFailureListener { e ->
            progressDialog?.dismiss()
            FancyToast.makeText(
                this,
                "Failed To add Deu To ${e.message}",
                FancyToast.LENGTH_SHORT,
                FancyToast.ERROR,
                true
            ).show()
        }
    }

}























