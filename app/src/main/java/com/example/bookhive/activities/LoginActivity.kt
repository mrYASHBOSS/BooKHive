package com.example.bookhive.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.example.bookhive.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.shashank.sony.fancytoastlib.FancyToast


class LoginActivity : AppCompatActivity() {


    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Firebase Var
        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        //handle click, not have account
        binding.noAccountTv.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }

        binding.loginBtn.setOnClickListener {
            /*Steps
            * 1) Input Data
            * 2) Validate Data
            * 3) Login - Firebase Auth
            * 4) Check User type - Firebase Auth
            *       if user - Move to user dashboard
            *       if Admin - Move to Admin dashboard*/
            validateData()
        }

        binding.forgoTV.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private var email = ""
    private var password = ""

    private fun validateData() {

        email = binding.emailET.text.toString().trim()
        password = binding.passwordET.text.toString().trim()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            //invalid Email
            FancyToast.makeText(
                this, "Invalid Email...", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true
            ).show()

        } else if (password.isEmpty()) {
            FancyToast.makeText(
                this, "Enter Password...", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true
            ).show()
        } else {
            loginUser()
        }
    }

    private fun loginUser() {

        progressDialog.setMessage("Logging In....")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
            checkUser()

        }.addOnFailureListener { e ->
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

    private fun checkUser() {

        progressDialog.setMessage("Checking User...")

        val firebaseUser = firebaseAuth.currentUser!!
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseUser.uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                progressDialog.dismiss()
                val userType = snapshot.child("userType").value
                if (userType == "user") {
                    //its simple user
                    startActivity(Intent(this@LoginActivity, DashboardUserActivity::class.java))
                    finish()
                } else if (userType == "admin") {
                    //Admin
                    startActivity(Intent(this@LoginActivity, DashboardAdminActivity::class.java))
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}