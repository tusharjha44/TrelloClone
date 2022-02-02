@file:Suppress("DEPRECATION")

package com.example.projemanag.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projemanag.R
import com.example.projemanag.adapters.MemberListItemsAdapter
import com.example.projemanag.databinding.ActivityMembersBinding
import com.example.projemanag.databinding.DialogSearchMemberBinding
import com.example.projemanag.firebase.FireStoreClass
import com.example.projemanag.models.Board
import com.example.projemanag.models.User
import com.example.projemanag.utils.Constants
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

@Suppress("DEPRECATION")
class MembersActivity : BaseActivity() {

    private lateinit var binding: ActivityMembersBinding
    private lateinit var mBoardDetails: Board
    private lateinit var mAssignedMembersList: ArrayList<User>
    private var anyChangesMade: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMembersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(intent.hasExtra(Constants.BOARD_DETAIL)){
            mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!

            showProgressDialog(resources.getString(R.string.please_wait))
            FireStoreClass().getAssignedMembersListDetails(this,mBoardDetails.assignedTo)

        }

        setupActionBar()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_member,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.action_add_member->{
                dialogSearchMember()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun dialogSearchMember() {
        val dialog = Dialog(this)
        val bind : DialogSearchMemberBinding = DialogSearchMemberBinding .inflate(layoutInflater)
        dialog.setContentView(bind.root)
        bind.tvAdd.setOnClickListener {

            val email = bind.etEmailSearchMember.text.toString()

            if (email.isNotEmpty()) {
                dialog.dismiss()
                showProgressDialog(resources.getString(R.string.please_wait))
                FireStoreClass().getMemberDetails(this@MembersActivity, email)
            } else {
                showErrorSnackBar("Please enter members email address.")
            }
        }
        bind.tvCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    fun memberDetails(user: User){
        mBoardDetails.assignedTo.add(user.id)
        FireStoreClass().assignMemberToBoard(this,mBoardDetails,user)
    }

    fun setUpMembersList(list:ArrayList<User>){

        mAssignedMembersList = list
        hideProgressDialog()

        binding.rvMembersList.layoutManager = LinearLayoutManager(this)
        binding.rvMembersList.setHasFixedSize(true)

        val adapter = MemberListItemsAdapter(this,list)
        binding.rvMembersList.adapter = adapter

    }

    private fun setupActionBar() {

        setSupportActionBar(binding.toolbarMembersActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString(R.string.members)
        }

        binding.toolbarMembersActivity.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if(anyChangesMade){
            setResult(Activity.RESULT_OK)
        }
    }

    fun memberAssignSuccess(user: User){
        hideProgressDialog()
        mAssignedMembersList.add(user)

        anyChangesMade = true
        setUpMembersList(mAssignedMembersList)
        SendNotificationToUserAsyncTask(mBoardDetails.name,user.fcmToken).execute()
    }

    @SuppressLint("StaticFieldLeak")
    private inner class SendNotificationToUserAsyncTask(val boardName: String, val token: String)
        : AsyncTask<Any,Void,String>(){

        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog(resources.getString(R.string.please_wait))
        }

        override fun doInBackground(vararg params: Any?): String {
            var result : String
            var connection: HttpsURLConnection? = null
            try {
                val url = URL(Constants.FCM_BASE_URL)
                connection = url.openConnection() as HttpsURLConnection

                /**
                 * A URL connection can be used for input and/or output.  Set the DoOutput
                 * flag to true if you intend to use the URL connection for output,
                 * false if not.  The default is false.
                 */
                connection.doInput = true
                connection.doOutput = true

                /**
                 * Sets whether HTTP redirects should be automatically followed by this instance.
                 * The default value comes from followRedirects, which defaults to true.
                 */
                connection.instanceFollowRedirects = false

                connection.requestMethod = "POST"

                /**
                 * Sets the general request property. If a property with the key already
                 * exists, overwrite its value with the new value.
                 */
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("charset", "utf-8")
                connection.setRequestProperty("Accept", "application/json")

                connection.setRequestProperty(
                    Constants.FCM_AUTHORIZATION,"${Constants.FCM_KEY} = ${Constants.FCM_SERVER_KEY}")

                connection.useCaches = false

                /**
                 * Creates a new data output stream to write data to the specified
                 * underlying output stream. The counter written is set to zero.
                 */
                val wr = DataOutputStream(connection.outputStream)

                // Create JSONObject Request
                val jsonRequest = JSONObject()

                // Create a data object
                val dataObject = JSONObject()
                // Here you can pass the title as per requirement as here we have added some text and board name.
                dataObject.put(Constants.FCM_KEY_TITLE, "Assigned to the Board $boardName")
                // Here you can pass the message as per requirement as here we have added some text and appended the name of the Board Admin.
                dataObject.put(
                    Constants.FCM_KEY_MESSAGE,
                    "You have been assigned to the new board by ${mAssignedMembersList[0].name}"
                )

                // Here add the data object and the user's token in the jsonRequest object.
                jsonRequest.put(Constants.FCM_KEY_DATA, dataObject)
                jsonRequest.put(Constants.FCM_KEY_TO,token )

                /**
                 * Writes out the string to the underlying output stream as a
                 * sequence of bytes. Each character in the string is written out, in
                 * sequence, by discarding its high eight bits. If no exception is
                 * thrown, the counter written is incremented by the
                 * length of s.
                 */
                wr.writeBytes(jsonRequest.toString())
                wr.flush() // Flushes this data output stream.
                wr.close() // Closes this output stream and releases any system resources associated with the stream

                val httpResult: Int =
                    connection.responseCode // Gets the status code from an HTTP response message.

                if (httpResult == HttpURLConnection.HTTP_OK) {

                    /**
                     * Returns an input stream that reads from this open connection.
                     */
                    val inputStream = connection.inputStream

                    /**
                     * Creates a buffering character-input stream that uses a default-sized input buffer.
                     */
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val sb = StringBuilder()
                    var line: String?
                    try {
                        /**
                         * Reads a line of text.  A line is considered to be terminated by any one
                         * of a line feed ('\n'), a carriage return ('\r'), or a carriage return
                         * followed immediately by a linefeed.
                         */
                        while (reader.readLine().also { line = it } != null) {
                            sb.append(line + "\n")
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } finally {
                        try {
                            /**
                             * Closes this input stream and releases any system resources associated
                             * with the stream.
                             */
                            inputStream.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    result = sb.toString()
                } else {
                    /**
                     * Gets the HTTP response message, if any, returned along with the
                     * response code from a server.
                     */
                    result = connection.responseMessage
                }

            } catch (e: SocketTimeoutException) {
                result = "Connection Timeout"
            } catch (e: Exception) {
                result = "Error : " + e.message
            } finally {
                connection?.disconnect()
            }

            // You can notify with your result to onPostExecute.
            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            hideProgressDialog()
            if (result != null) {
                Log.e("JSON Response Result", result)
            }
        }

    }
}