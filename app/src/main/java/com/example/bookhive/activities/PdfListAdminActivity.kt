package com.example.bookhive.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import com.example.bookhive.Adapter.AdapterPdfAdmin
import com.example.bookhive.Models.ModelPdf
import com.example.bookhive.databinding.ActivityPdfListAdminBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class PdfListAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfListAdminBinding

    //category id, title
    private var categoryId = ""
    private var category = ""

    //array list to hold books
    private lateinit var pdfArrayList: ArrayList<ModelPdf>

    //adapter
    private lateinit var adapterPdfAdmin: AdapterPdfAdmin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfListAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //get from intent, that we passed from adapter
        val intent = intent
        categoryId = intent.getStringExtra("categoryId")!!
        category = intent.getStringExtra("category")!!

        //set pdf category
        binding.subTitleTv.text = category

        //load pdf/books
        loadPDFList()

        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    adapterPdfAdmin.filter!!.filter(s)
                } catch (e: Exception) {

                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
        //back Button
        binding.backBtn.setOnClickListener { onBackPressed() }

    }

    private fun loadPDFList() {
        //init arraylist
        pdfArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    pdfArrayList.clear()
                    for (ds in snapshot.children) {
                        val model = ds.getValue(ModelPdf::class.java)
                        if (model != null) {
                            pdfArrayList.add(model)
                        }
                    }
                    adapterPdfAdmin = AdapterPdfAdmin(this@PdfListAdminActivity, pdfArrayList)
                    binding.bookRV.adapter = adapterPdfAdmin
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

}