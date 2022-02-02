package com.example.projemanag.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projemanag.R
import com.example.projemanag.adapters.BoardsItemAdapter
import com.example.projemanag.databinding.ActivityMainBinding
import com.example.projemanag.firebase.FireStoreClass
import com.example.projemanag.models.Board
import com.example.projemanag.models.User
import com.example.projemanag.utils.Constants
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : BaseActivity(),NavigationView.OnNavigationItemSelectedListener {

    private var binding:ActivityMainBinding? = null

    private lateinit var mUserName: String
    private lateinit var mSharedPreferences: SharedPreferences

    companion object {
        const val MY_PROFILE_REQUEST_CODE: Int = 11
        const val CREATE_BOARD_REQUEST_CODE: Int = 12
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

        mSharedPreferences = this.getSharedPreferences(Constants.PROGEMANAG_PREFERENCES,Context.MODE_PRIVATE)
        val tokenUpdated = mSharedPreferences
            .getBoolean(Constants.FCM_TOKEN_UPDATED,false)

        if(tokenUpdated){
            showProgressDialog(resources.getString(R.string.please_wait))
            FireStoreClass().loadUserData(this,true)
        }else{
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener(this@MainActivity) {
                    updateFcmToken(it)
                }
        }

        FireStoreClass().loadUserData(this,true)

        binding?.appBarMainLayout?.fabCreateBoard?.setOnClickListener {
            val intent = Intent(this,
                CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME,mUserName)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }

    }

    private fun setupActionBar(){
        setSupportActionBar(binding?.appBarMainLayout?.toolbarMainActivity)
        binding?.appBarMainLayout?.toolbarMainActivity?.setNavigationIcon(R.drawable.ic_action_navigation_menu)

        binding?.appBarMainLayout?.toolbarMainActivity?.setNavigationOnClickListener {
           toggleDrawer()
        }
    }

    @SuppressLint("CutPasteId")
    fun populateBoardsListToUI(boardsList: ArrayList<Board>) {
        hideProgressDialog()

//        val contentMainBinding: ContentMainBinding = ContentMainBinding.inflate(layoutInflater)

        if (boardsList.size > 0) {

            // Toggling the views: Board List + No Boards Available
//             contentMainBinding.rvBoardsList.visibility = View.VISIBLE
//             contentMainBinding.tvNoBoardsAvailable.visibility = View.GONE

            // We are choosing a Linear Layout
//             contentMainBinding.rvBoardsList.layoutManager = LinearLayoutManager(this@MainActivity)
//             contentMainBinding.rvBoardsList.setHasFixedSize(true)

            // Create an instance of BoardItemsAdapter and pass the boardList to it.
//             val adapter = BoardsItemAdapter(this@MainActivity, boardsList)

            // Attach the adapter to the recyclerView.
//             contentMainBinding.rvBoardsList.adapter = adapter

            findViewById<RecyclerView>(R.id.rv_boards_list).visibility = View.VISIBLE
            findViewById<TextView>(R.id.tv_no_boards_available).visibility = View.GONE

            findViewById<RecyclerView>(R.id.rv_boards_list).layoutManager =
                LinearLayoutManager(this@MainActivity)
            findViewById<RecyclerView>(R.id.rv_boards_list).setHasFixedSize(true)

            val adapter = BoardsItemAdapter(this@MainActivity, boardsList)
            findViewById<RecyclerView>(R.id.rv_boards_list).adapter = adapter

            adapter.setOnClickListener(object : BoardsItemAdapter.OnClickListener{
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity,TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID,model.documentID)
                    startActivity(intent)
                }

            })

        } else {

//            // Toggling the views: Board List + No Boards Available
//             contentMainBinding.rvBoardsList.visibility = View.GONE
//             contentMainBinding.tvNoBoardsAvailable.visibility = View.VISIBLE

            findViewById<RecyclerView>(R.id.rv_boards_list).visibility = View.GONE
            findViewById<TextView>(R.id.tv_no_boards_available).visibility = View.VISIBLE

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
        } else if (resultCode == Activity.RESULT_OK && requestCode == CREATE_BOARD_REQUEST_CODE) {
            FireStoreClass().getBoardsList(this@MainActivity)
        }
        else {
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

                mSharedPreferences.edit().clear().apply()

                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        return true
    }

    fun updateNavigationUserDetails(user: User, readBoardsList: Boolean){
        hideProgressDialog()
        mUserName = user.name

        val headerView = binding?.navView?.getHeaderView(0)
        val headerBinding = headerView!!.findViewById<ImageView>(R.id.iv_profile_user_image)

        Glide
            .with(this@MainActivity)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(headerBinding)

        headerView.findViewById<TextView>(R.id.tv_username).text = user.name

        if(readBoardsList){
            showProgressDialog(resources.getString(R.string.please_wait))
            FireStoreClass().getBoardsList(this)
        }

    }

    fun tokenUpdateSuccess() {
        hideProgressDialog()
        val editor: SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)

        editor.apply()

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().loadUserData(this,true)

    }

    private fun updateFcmToken(token: String){
        val userHashMap = HashMap<String, Any>()
        userHashMap[Constants.FCM_TOKEN] = token

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().updateUserProfileData(this,userHashMap)
    }


}