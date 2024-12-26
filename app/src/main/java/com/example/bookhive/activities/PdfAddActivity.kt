package com.example.bookhive.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.bookhive.Models.ModelCategory
import com.example.bookhive.databinding.ActivityPdfAddBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.shashank.sony.fancytoastlib.FancyToast


class PdfAddActivity : AppCompatActivity() {

    //setup view binding activity_pdf_add
    private lateinit var binding: ActivityPdfAddBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    //arraylist to hold pdf categories
    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    //uri of picked pdf
    private var pdfUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        loadPDFCategories()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.categoryTv.setOnClickListener {
            categoryPickDialog()
        }

        binding.attachedFileBtn.setOnClickListener {
            pdfPickIntent()
        }

        binding.submitBtn.setOnClickListener {
            //STEP 1) = ValidateData
            //STEP 2) = Upload pdf to firebase storage
            //STEP 3) = Get Url of upload pdf
            //STEP 4) = Upload Pdf info to fire
            validateData()
        }

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

    }

    private var title = ""
    private var description = ""
    private var category = ""

    private fun validateData() {

        //STEP 1) = ValidateData
        //get data
        title = binding.titleET.text.toString().trim()
        description = binding.descriptionET.text.toString().trim()
        category = binding.categoryTv.text.toString().trim()

        if (title.isEmpty()) {
            FancyToast.makeText(
                this, "Enter Title....", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true
            ).show()
        } else if (description.isEmpty()) {
            FancyToast.makeText(
                this, "Enter Description....", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true
            ).show()
        } else if (category.isEmpty()) {
            FancyToast.makeText(
                this, "Select Category....", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true
            ).show()
        } else if (pdfUri == null) {
            FancyToast.makeText(
                this, "Pick Pdf....", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true
            ).show()
        } else {
            //data validate, being upload to firebase storage
            uploadData()
        }

    }

    private fun uploadData() {
        //STEP 2) = Upload pdf to firebase storage
        progressDialog.setMessage("Uploading Pdf")
        progressDialog.show()
        val timestamp = System.currentTimeMillis()
        val filePathAndName = "Book/${timestamp}"
        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(pdfUri!!).addOnSuccessListener { taskSnapshot ->

            //STEP 3) = Get Url of upload pdf
            val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
            while (!uriTask.isSuccessful);
            val uploadedPdfUrl = "${uriTask.result}"

            uploadedPdfUrl(uploadedPdfUrl, timestamp)

        }.addOnFailureListener { e ->
            progressDialog.dismiss()
            FancyToast.makeText(
                this,
                "Failed To Upload PDF Deu To ${e.message}",
                FancyToast.LENGTH_SHORT,
                FancyToast.ERROR,
                true
            ).show()
        }
    }

    private fun uploadedPdfUrl(uploadedPdfUrl: String, timestamp: Long) {
        //STEP 4) = Upload Pdf info to fire
        progressDialog.setMessage("Uploading pdf info....")
        val uid = firebaseAuth.uid
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["uid"] = "$uid"
        hashMap["id"] = "$timestamp"
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"
        hashMap["uri"] = "$uploadedPdfUrl"
        hashMap["timestamp"] = timestamp
        hashMap["viewsCount"] = 0
        hashMap["downloadsCount"] = 0


        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child("$timestamp").setValue(hashMap).addOnSuccessListener {

            progressDialog.dismiss()
            FancyToast.makeText(
                this, "Book Uploaded...", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true
            ).show()
            pdfUri = null

        }.addOnFailureListener { e ->
            progressDialog.dismiss()
            FancyToast.makeText(
                this,
                "Failed To upload pdf Data Deu To ${e.message}",
                FancyToast.LENGTH_SHORT,
                FancyToast.ERROR,
                true
            ).show()
        }

    }

    private fun loadPDFCategories() {
        categoryArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before starting adding data into it
                categoryArrayList.clear()
                for (ds in snapshot.children) {
                    //get Data from model
                    val model = ds.getValue(ModelCategory::class.java)
                    //add to arrayList
                    categoryArrayList.add(model!!)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    private fun categoryPickDialog() {
        val categoryArray = arrayOfNulls<String>(categoryArrayList.size)
        for (i in categoryArrayList.indices) {
            categoryArray[i] = categoryArrayList[i].category
        }
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Category").setItems(categoryArray) { dialog, which ->
            selectedCategoryId = categoryArrayList[which].id
            selectedCategoryTitle = categoryArrayList[which].category
            binding.categoryTv.text = selectedCategoryTitle
        }.show()
    }

    private fun pdfPickIntent() {
        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        pdfActivityResultLauncher.launch(intent)
    }

    val pdfActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback<ActivityResult> { result ->
                if (result.resultCode == RESULT_OK) {
                    pdfUri = result.data!!.data
                } else {
                    FancyToast.makeText(
                        this,
                        "PDF Picked Cancelled",
                        FancyToast.LENGTH_SHORT,
                        FancyToast.ERROR,
                        true
                    ).show()
                }
            })
}









































