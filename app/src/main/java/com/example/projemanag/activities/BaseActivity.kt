package com.example.projemanag.activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.projemanag.R
import com.example.projemanag.databinding.ActivityBaseBinding
import com.example.projemanag.databinding.DialogProgressBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

open class BaseActivity : AppCompatActivity() {

    private var binding: ActivityBaseBinding? = null
    private var doubleBackToExitPressedOne = false
    private lateinit var mProgressDialog: Dialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
    }

    fun showProgressDialog(text:String){
        mProgressDialog = Dialog(this)
        val mBinding: DialogProgressBinding = DialogProgressBinding.inflate(layoutInflater)
        mProgressDialog.setContentView(mBinding.root)

        mBinding.tvProgressText.text = text

        mProgressDialog.show()

    }

    fun hideProgressDialog(){
        mProgressDialog.dismiss()
    }

fun getCurrentUserID() : String{
    return FirebaseAuth.getInstance().currentUser!!.uid
}

    fun doubleBckToExit(){
        if(doubleBackToExitPressedOne){
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOne = true
        Toast.makeText(
            this,
            resources.getString(R.string.please_click_back_again_to_exit),
            Toast.LENGTH_SHORT
        ).show()

        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOne=false
        }, 3000)
    }

    fun showErrorSnackBar(message: String){
        val snackBar = Snackbar.make(
            findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_SHORT)
        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(ContextCompat.getColor(this,R.color.snackbar_error_color))
        snackBar.show()
    }
}