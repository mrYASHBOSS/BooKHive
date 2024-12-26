package com.example.bookhive.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.example.bookhive.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class SplashActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        firebaseAuth = FirebaseAuth.getInstance()

        Handler().postDelayed({
            checkUser()
        }, 2000)

    }

    //get current user, if logged in or not
    private fun checkUser() {

        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {

            //user logged in,goto main screen
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()

        } else {

            //user logged in,check user type,same as done in login
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseUser.uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userType = snapshot.child("userType").value
                    if (userType == "user") {
                        //its simple user
                        startActivity(
                            Intent(
                                this@SplashActivity, DashboardUserActivity::class.java
                            )
                        )
                        finish()
                    } else if (userType == "admin") {
                        //Admin
                        startActivity(
                            Intent(
                                this@SplashActivity, DashboardAdminActivity::class.java
                            )
                        )
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        }
    }
}
//For First Time Only
//        Handler().postDelayed({
//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
//            finish()
//        },1000)
