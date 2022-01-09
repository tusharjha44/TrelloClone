package com.example.projemanag.activities

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import com.example.projemanag.R
import com.example.projemanag.databinding.ActivitySignUpBinding
import com.example.projemanag.firebase.FireStoreClass
import com.example.projemanag.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignUpActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth
    private var binding: ActivitySignUpBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        auth = Firebase.auth

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        }else{
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        binding?.btnSignUp?.setOnClickListener{
            registerUser()
        }

        setupActionBar()
    }

    fun userRegisteredSuccess(){
        Toast.makeText(this@SignUpActivity,
            "Successfully Registered",
            Toast.LENGTH_LONG).show()
        hideProgressDialog()
        auth.signOut()
        finish()
    }

    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarSignUpActivity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        binding?.toolbarSignUpActivity?.setNavigationOnClickListener { onBackPressed() }

    }

    private fun registerUser(){
        val name: String = binding?.etName?.text.toString().trim{it<=' '}
        val email: String = binding?.etEmail?.text.toString().trim{it<=' '}
        val password: String = binding?.etPassword?.text.toString().trim{it<=' '}

        if(validateForm(name,email, password)){
            showProgressDialog(resources.getString(R.string.please_wait))
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        val registeredEmail = firebaseUser.email!!
                        val user = User(firebaseUser.uid,name,registeredEmail)
                        FireStoreClass().registeredUser(this,user)
                    } else {
                        Toast.makeText(
                            this,
                            "Registration Failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

    }


    private fun validateForm(name: String,email: String,password: String): Boolean {
        return when{
            TextUtils.isEmpty(name)->{
                showErrorSnackBar("Please enter a name")
                false
            }
            TextUtils.isEmpty(email)->{
                showErrorSnackBar("Please enter a email")
                false
            }
            TextUtils.isEmpty(password)->{
                showErrorSnackBar("Please enter a password")
                false
            }
            else->{
                true
            }

        }
    }

}