package com.example.bookhive.activities

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.example.bookhive.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth
import com.shashank.sony.fancytoastlib.FancyToast

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Firebase Var
        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //handle click, not have account
        binding.submitBtn.setOnClickListener {
            validateData()
        }

    }

    private var email = ""
    private fun validateData() {
        email = binding.emailEt.text.toString().trim()
        if (email.isEmpty()) {
            FancyToast.makeText(
                this, "Enter Email...", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true
            ).show()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            //invalid Email
            FancyToast.makeText(
                this, "Invalid Email...", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true
            ).show()
        } else {
            recoverPassword()
        }
    }

    private fun recoverPassword() {
        //show progress
        progressDialog.setMessage("Sending password reset instructions to $email")
        progressDialog.show()
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                //sent
                progressDialog.dismiss()
                FancyToast.makeText(
                    this,
                    "Instructions sent to \n$email",
                    FancyToast.LENGTH_SHORT,
                    FancyToast.INFO,
                    true
                ).show()
            }
            .addOnFailureListener { e ->
                //failed
                progressDialog.dismiss()
                FancyToast.makeText(
                    this,
                    "Failed to send due to ${e.message}",
                    FancyToast.LENGTH_SHORT,
                    FancyToast.ERROR,
                    true
                ).show()
            }
    }
}






















