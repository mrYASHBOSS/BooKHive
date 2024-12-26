package com.example.bookhive

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.bookhive.Adapter.AdapterPdfUser
import com.example.bookhive.Models.ModelPdf
import com.example.bookhive.databinding.FragmentBooksUserBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BooksUserFragment : Fragment {

    private lateinit var binding: FragmentBooksUserBinding

    public companion object {
        //receive data from activity to load books
        public fun newInstance(
            categoryId: String,
            category: String,
            uid: String
        ): BooksUserFragment {
            val fragment = BooksUserFragment()
            //put data to bundle intent
            val args = Bundle()
            args.putString("categoryId", categoryId)
            args.putString("category", category)
            args.putString("uid", uid)
            fragment.arguments = args
            return fragment
        }
    }

    private var categoryId = ""
    private var category = ""
    private var uid = ""
    private  var pdfArrayList: ArrayList<ModelPdf>?=null
    private  var adapterPdfUser: AdapterPdfUser?=null

    constructor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = arguments
        if (args != null) {
            category = args.getString("category")!!
            categoryId = args.getString("categoryId")!!
            uid = args.getString("uid")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBooksUserBinding.inflate(LayoutInflater.from(context), container, false)

        if (category == "All") {
            loadAllBooks()
        } else if (category == "Most Viewed") {
            loadMostViewedBooks("viewCount")
        } else {
            loadCategorized()
        }

        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    adapterPdfUser?.filter?.filter(s)
                } catch (_: Exception) {
                }
            }
            override fun afterTextChanged(s: Editable?) {
            }
        })

        return binding.root
    }

    private fun loadAllBooks() {

        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList?.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(ModelPdf::class.java)
                    if (model != null) {
                        pdfArrayList?.add(model)
                    }
                }
                adapterPdfUser= pdfArrayList?.let { context?.let { it1 -> AdapterPdfUser(it1, it) } }
                binding.bookRV.adapter = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    private fun loadMostViewedBooks(orderBy: String) {

        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        //load only 10 most books
        ref.orderByChild(orderBy).limitToLast(10).addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList?.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(ModelPdf::class.java)
                    if (model != null) {
                        pdfArrayList?.add(model)
                    }
                }
                adapterPdfUser= context?.let { AdapterPdfUser(it, pdfArrayList!!) }
                binding.bookRV.adapter = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun loadCategorized() {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId).addValueEventListener(object : ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList?.clear()
                for (ds in snapshot.children)
                {
                    val model = ds.getValue(ModelPdf::class.java)
                    pdfArrayList?.add(model!!)
                }
                adapterPdfUser = context?.let { AdapterPdfUser(it, pdfArrayList!!) }
                binding.bookRV.adapter = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

}