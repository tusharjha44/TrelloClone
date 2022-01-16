

package com.example.projemanag.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.projemanag.R
import com.example.projemanag.databinding.ActivityMyProfileBinding
import com.example.projemanag.firebase.FireStoreClass
import com.example.projemanag.models.User
import com.example.projemanag.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class MyProfileActivity : BaseActivity() {

    private lateinit var binding: ActivityMyProfileBinding

    companion object{
        private const val READ_STORAGE_PERMISSIONS_CODE = 1
        private const val PICK_IMAGE_REQUEST_CODE = 2
    }

    private var mSelectedImageFileUri: Uri? = null
    private lateinit var mUserDetails: User
    private var mProfileImageURL : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()

        FireStoreClass().loadUserData(this)

        binding.ivProfileUserImage.setOnClickListener {
            if(ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED){
                showImageChooser()
            }
            else{
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    READ_STORAGE_PERMISSIONS_CODE
                )
            }
        }

        binding.btnUpdate.setOnClickListener {
            if(mSelectedImageFileUri!=null){
                uploadUserImage()
            }else{
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK
            && requestCode == PICK_IMAGE_REQUEST_CODE
            && data!!.data != null){

            mSelectedImageFileUri = data.data

            try {
                Glide
                    .with(this)
                    .load(mSelectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(binding.ivProfileUserImage)
            }catch (e: IOException){
                e.printStackTrace()
            }

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == READ_STORAGE_PERMISSIONS_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                showImageChooser()
            }
            else{
                Toast.makeText(
                    this,
                    "Oops, you just denied Storage permissions",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showImageChooser(){
        val galleryIntent =
            Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    private fun setupActionBar() {

        setSupportActionBar(binding.toolbarMyProfileActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString(R.string.my_profile)
        }

        binding.toolbarMyProfileActivity.setNavigationOnClickListener { onBackPressed() }
    }

    fun setUserDataInUI(user : User){

        mUserDetails = user

        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(binding.ivProfileUserImage)

        binding.etName.setText(user.name)
        binding.etEmail.setText(user.email)

        if(user.mobile != 0L){
            binding.etMobile.setText(user.mobile.toString())
        }

    }

    private fun updateUserProfileData(){
        val userHashMap = HashMap<String,Any>()

        if(mProfileImageURL.isNotEmpty() && mProfileImageURL != mUserDetails.image){
            userHashMap[Constants.IMAGE] = mProfileImageURL
        }

        if(binding.etName.text.toString() != mUserDetails.name){
            userHashMap[Constants.NAME] = binding.etName.text.toString()
        }
        if(binding.etMobile.text.toString() != mUserDetails.name){
            userHashMap[Constants.MOBILE] = binding.etMobile.text.toString().toLong()
        }

        FireStoreClass().updateUserProfileData(this,userHashMap)

    }

    private fun uploadUserImage(){
        showProgressDialog(resources.getString(R.string.please_wait))

        if(mSelectedImageFileUri != null){
            val sRef: StorageReference =
                FirebaseStorage.getInstance().reference.child(
                    "USER_IMAGE" + System.currentTimeMillis()
                            + "." + getFileExtension(mSelectedImageFileUri)
                )

            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                    taskSnapShot->
                Log.i(
                    "Firebase Image Uri",
                    taskSnapShot.metadata!!.reference!!.downloadUrl.toString()
                )

                taskSnapShot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                        uri->
                    Log.i("Downloaded Image URL",uri.toString())
                    mProfileImageURL = uri.toString()
                    updateUserProfileData()

                }
            }.addOnFailureListener{
                Toast.makeText(
                    this@MyProfileActivity,
                    it.message,
                    Toast.LENGTH_LONG
                ).show()

                hideProgressDialog()

            }

        }
    }


    private fun getFileExtension(uri:Uri?): String?{
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(contentResolver.getType(uri!!))
    }

    fun profileUpdateSuccess(){
        hideProgressDialog()
        finish()
    }
}
