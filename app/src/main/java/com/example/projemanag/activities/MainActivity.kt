package com.example.projemanag.activities

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.example.projemanag.R
import com.example.projemanag.databinding.ActivityMainBinding
import com.example.projemanag.firebase.FireStoreClass
import com.example.projemanag.models.User
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : BaseActivity(),NavigationView.OnNavigationItemSelectedListener {

    private var binding:ActivityMainBinding? = null

    companion object {
        const val MY_PROFILE_REQUEST_CODE: Int = 11
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        }else{
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        setupActionBar()

        binding?.navView?.setNavigationItemSelectedListener(this)

        FireStoreClass().loadUserData(this)

    }

    private fun setupActionBar(){
        setSupportActionBar(binding?.appBarMainLayout?.toolbarMainActivity)
        binding?.appBarMainLayout?.toolbarMainActivity?.setNavigationIcon(R.drawable.ic_action_navigation_menu)

        binding?.appBarMainLayout?.toolbarMainActivity?.setNavigationOnClickListener {
           toggleDrawer()
        }
    }

    private fun toggleDrawer(){
        if (binding?.drawerLayout?.isDrawerOpen(GravityCompat.START) == true){
            binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        }else{
            binding?.drawerLayout?.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        if (binding?.drawerLayout?.isDrawerOpen(GravityCompat.START) == true){
            binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        }else{
            doubleBckToExit()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK
            && requestCode == MY_PROFILE_REQUEST_CODE
        ) {
            // Get the user updated details.
            FireStoreClass().loadUserData(this@MainActivity)
        } else {
            Log.e("Cancelled", "Cancelled")
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_my_profile -> {
                startActivityForResult(Intent(this@MainActivity
                    ,MyProfileActivity::class.java),
                    MY_PROFILE_REQUEST_CODE)
            }

            R.id.nav_sign_out -> {
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        return true
    }

    fun updateNavigationUserDetails(user: User){
        val headerView = binding?.navView?.getHeaderView(0)
        val headerBinding = headerView!!.findViewById<ImageView>(R.id.iv_profile_user_image)

        Glide
            .with(this@MainActivity)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(headerBinding)

        headerView.findViewById<TextView>(R.id.tv_username).text = user.name
    }

}