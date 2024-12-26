package com.example.bookhive.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.example.bookhive.databinding.ActivityRegistrationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.shashank.sony.fancytoastlib.FancyToast
import java.util.HashMap

class RegistrationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistrationBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Firebase Var
        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        binding.RegisterBtn.setOnClickListener {
            //Steps
            //1) Input Data
            //2) Validate data
            //3) Create Account - Firebase Auth
            //4) Save User Info - Firebase Realtime Database
            validateData()
        }

    }

    private var name = ""
    private var email = ""
    private var password = ""
    private fun validateData() {

        //1) Input Data
        name = binding.nameET.text.toString().trim()
        email = binding.emailET.text.toString().trim()
        password = binding.passwordET.text.toString().trim()
        val cPassword = binding.cPasswordET.text.toString().trim()

        //2)
        if (name.isEmpty()) {
            //Empty name
            FancyToast.makeText(
                this,
                "Enter Your Name...",
                FancyToast.LENGTH_SHORT,
                FancyToast.ERROR,
                true
            ).show()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            //invalid Email
            FancyToast.makeText(
                this,
                "Invalid Email...",
                FancyToast.LENGTH_SHORT,
                FancyToast.ERROR,
                true
            ).show()
        } else if (password.isEmpty()) {
            FancyToast.makeText(
                this,
                "Enter Password...",
                FancyToast.LENGTH_SHORT,
                FancyToast.ERROR,
                true
            ).show()
        } else if (cPassword.isEmpty()) {
            FancyToast.makeText(
                this,
                "Confirm Password...",
                FancyToast.LENGTH_SHORT,
                FancyToast.ERROR,
                true
            ).show()
        } else if (password != cPassword) {
            FancyToast.makeText(
                this,
                "Password Doesn't Match",
                FancyToast.LENGTH_SHORT,
                FancyToast.ERROR,
                true
            ).show()
        } else {
            createUserAccount()
        }
    }

    private fun createUserAccount() {

        //3) Create password
        //-->Show password
        progressDialog.setMessage("Creating Account")
        progressDialog.show()

        //-->Create User in firebase Auth
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                //Account created but this fun use to store data of there
                updateUserInfo()
            }
            .addOnFailureListener {
                //Failed  Creating Account
                    e ->
                progressDialog.dismiss()
                FancyToast.makeText(
                    this,
                    "Failed To Create Account Deu To ${e.message}",
                    FancyToast.LENGTH_SHORT,
                    FancyToast.ERROR,
                    true
                ).show()
            }
    }

    private fun updateUserInfo() {

        //4)Saved Information
        progressDialog.setMessage("Saving User Info...")

        //Time
        val timestamp = System.currentTimeMillis()

        //get current user id, since we get register user
        val uid = firebaseAuth.uid

        //setup data to add in db
        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["uid"] = uid
        hashMap["email"] = email
        hashMap["name"] = name
        hashMap["profileImage"] = ""
        hashMap["userType"] = "user"
        hashMap["timestamp"] = timestamp

        //Set Data in db
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid!!).setValue(hashMap).addOnSuccessListener {

            progressDialog.dismiss()
            FancyToast.makeText(
                this,
                "Account Created...",
                FancyToast.LENGTH_SHORT,
                FancyToast.SUCCESS,
                true
            ).show()
            startActivity(Intent(this@RegistrationActivity, DashboardUserActivity::class.java))
            finish()
        }.addOnFailureListener { e ->
            progressDialog.dismiss()
            FancyToast.makeText(
                this,
                "Failed To Saving Data Deu To ${e.message}",
                FancyToast.LENGTH_SHORT,
                FancyToast.ERROR,
                true
            ).show()
        }

    }
}