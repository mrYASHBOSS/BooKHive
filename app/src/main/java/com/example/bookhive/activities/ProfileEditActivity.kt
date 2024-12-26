package com.example.bookhive.activities

import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.PopupMenu
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.bookhive.R
import com.example.bookhive.databinding.ActivityProfileEditBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.shashank.sony.fancytoastlib.FancyToast

class ProfileEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileEditBinding
    private var firebaseAuth: FirebaseAuth? = null
    private var imageUri: Uri? = null
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)


        firebaseAuth = FirebaseAuth.getInstance()
        loadUserInfo()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.profileIv.setOnClickListener {
            showImageAttachMenu()

        }

        binding.updateBtn.setOnClickListener {
            validateData()
        }

    }

    private var name = ""
    private fun validateData() {
        name = binding.nameEt.text.toString().trim()
        if (name.isEmpty()) {
            FancyToast.makeText(
                this, "Enter name", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true
            ).show()
        } else {
            if (imageUri == null) {
                updateProfile("")
            } else {
                uploadImage()
            }
        }
    }

    private fun uploadImage() {
        progressDialog.setMessage("Uploading profile image")
        progressDialog.show()
        //image path and name, use uid to replace previous
        val filePathAndName = "ProfileImages/" + firebaseAuth?.uid
        //storage reference
        val reference = FirebaseStorage.getInstance().getReference(filePathAndName)
        reference.putFile(imageUri!!).addOnSuccessListener { taskSnapshot ->
            //image uploaded, get url of uploaded image
            val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
            while (!uriTask.isSuccessful);
            val uploadedImageUrl = "${uriTask.result}"
            updateProfile(uploadedImageUrl)
        }.addOnFailureListener { e ->
            //failed to upload image
            progressDialog.dismiss()
            FancyToast.makeText(
                this,
                "Failed to upload image due to ${e.message}",
                FancyToast.LENGTH_SHORT,
                FancyToast.ERROR,
                true
            ).show()
        }
    }

    private fun updateProfile(uploadedImageUrl: String) {
        progressDialog.setMessage("Updating profile...")
        //setup info to update to db
        val hashmap: HashMap<String, Any> = HashMap()
        hashmap["name"] = "$name"
        if (imageUri != null) {
            hashmap["profileImage"] = uploadedImageUrl
        }
        //update to db
        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child(firebaseAuth?.uid!!)
            .updateChildren(hashmap)
            .addOnSuccessListener {
                //profile updated
                progressDialog.dismiss()
                FancyToast.makeText(
                    this,
                    "Profile updated",
                    FancyToast.LENGTH_SHORT,
                    FancyToast.SUCCESS,
                    true
                ).show()
            }
            .addOnFailureListener { e ->
                //failed to upload image
                progressDialog.dismiss()
                FancyToast.makeText(
                    this,
                    "Failed to update profile due to ${e.message}",
                    FancyToast.LENGTH_SHORT,
                    FancyToast.ERROR,
                    true
                ).show()
            }
    }

    private fun loadUserInfo() {
        //db reference to load user info
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth?.uid!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //get user info
                val name = "${snapshot.child("name").value}"
                val profileImage = "${snapshot.child("profileImage").value}"
                val timestamp = "${snapshot.child("timestamp").value}"
                //set data
                binding.nameEt.setText(name)

                //set image
                try {
                    Glide.with(this@ProfileEditActivity).load(profileImage)
                        .placeholder(R.drawable.ic_person).into(binding.profileIv)
                } catch (e: Exception) {
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun showImageAttachMenu() {/*Show popup menu with options Camera, Gallery to pick image*/
//setup popup menu
        val popupMenu = PopupMenu(this, binding.profileIv)
        popupMenu.menu.add(Menu.NONE, 0, 0, "Camera")
        popupMenu.menu.add(Menu.NONE, 1, 1, "Gallery")
        popupMenu.show()
//handle popup menu item click
        popupMenu.setOnMenuItemClickListener { item ->
            //get id of clicked item
            val id = item.itemId
            if (id == 0) {
                //Camera clicked
                pickImageCamera()
            } else if (id == 1) {
                //Gallery licked
                pickImageGallery()
            }
            true
        }
    }

    private fun pickImageCamera() {

        val value = ContentValues()
        value.put(MediaStore.Images.Media.TITLE, "Temp_Title")
        value.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Description")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, value)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)
    }

    private fun pickImageGallery() {

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)

    }

    //used to handle result of camera intent (new way in replacement of start activity for results)
    private val cameraActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback<ActivityResult> { result ->
                //get uri of image
                if (result.resultCode == RESULT_OK) {
                    val data = result.data
                    //imageUri = data!!.data no need we already have image in imageUri in camera case
                    //set to imageview
                    binding.profileIv.setImageURI(imageUri)
                } else {
                    //cancelled
                    FancyToast.makeText(
                        this, "Cancelled", FancyToast.LENGTH_SHORT, FancyToast.INFO, true
                    ).show()
                }
            })

    private val galleryActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback<ActivityResult> { result ->
                if (result.resultCode == RESULT_OK) {
                    val data = result.data
                    imageUri = data!!.data
                    binding.profileIv.setImageURI(imageUri)
                } else {
                    FancyToast.makeText(
                        this, "Cancelled", FancyToast.LENGTH_SHORT, FancyToast.INFO, true
                    ).show()
                }
            })
}