
package com.example.projemanag.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.projemanag.activities.*
import com.example.projemanag.models.Board
import com.example.projemanag.models.User
import com.example.projemanag.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FireStoreClass {

    private val mFireStore = Firebase.firestore

    fun registeredUser(activity: SignUpActivity,userInfo: User){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }
            .addOnFailureListener{ e->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error writing document",
                    e
                )
            }
    }

    fun createBoard(activity: CreateBoardActivity,board: Board){
        mFireStore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {

                Log.e(activity.javaClass.simpleName,"Board Created Successfully")
                Toast.makeText(activity,"Board creted successfully",Toast.LENGTH_SHORT).show()
                activity.boardCreatedSuccessfully()
            }
            .addOnFailureListener{ e->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error creating board",
                    e
                )
            }
    }

    fun updateUserProfileData(activity: MyProfileActivity,userHshMap: HashMap<String,Any>){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .update(userHshMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName,"Profile Dta Updated")
                Toast.makeText(activity,"Profile Updated Successfully",Toast.LENGTH_SHORT).show()
                activity.profileUpdateSuccess()

            }.addOnFailureListener{
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName
                    ,"Error while creating board",it)
                Toast.makeText(activity
                    ,"Error when Updating the Profile..",Toast.LENGTH_SHORT).show()

            }
    }

    fun loadUserData(activity: Activity,readBoardsList: Boolean = false) {

        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                Log.e(
                    activity.javaClass.simpleName, document.toString()
                )

                // Here we have received the document snapshot which is converted into the User Data model object.
                val loggedInUser = document.toObject(User::class.java)!!

                when(activity){
                    is SignInActivity->{
                        activity.signInSuccess(loggedInUser)
                    }
                    is MainActivity->{
                        activity.updateNavigationUserDetails(loggedInUser,readBoardsList)
                    }
                    is MyProfileActivity ->{
                        activity.setUserDataInUI(loggedInUser)
                    }
                }

            }
            .addOnFailureListener { e ->

                when(activity){
                    is SignInActivity->{
                        activity.hideProgressDialog()
                    }
                    is MainActivity->{
                        activity.hideProgressDialog()
                    }
                }
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while getting loggedIn user details",
                    e
                )
            }
    }


    fun getBoardsList(activity: MainActivity){
        mFireStore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.ASSIGNED_TO,getCurrentUserId())
            .get()
            .addOnSuccessListener {
                document->
                Log.i(activity.javaClass.simpleName,document.documents.toString())
                val boardList: ArrayList<Board> = ArrayList()
                for(i in document.documents){
                    val board = i.toObject(Board::class.java)!!
                    board.documentID = i.id
                    boardList.add(board)
                }

                activity.populateBoardsListToUI(boardList)
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Error while creating the board",it)
            }
    }

    fun addUpdateTaskList(activity: Activity, board: Board) {

        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentID)
            .update(taskListHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "TaskList updated successfully.")
                if(activity is TaskListActivity)
                    activity.addUpdateTaskListSuccess()
                else if(activity is CardDetailsActivity)
                    activity.addUpdateTaskListSuccess()
            }
            .addOnFailureListener { e ->
                if(activity is TaskListActivity)
                    activity.hideProgressDialog()
                else if(activity is CardDetailsActivity)
                    activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
            }
    }

    fun getCurrentUserId(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser

        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }

        return currentUserID
    }

    fun getBoardDetails(activity: TaskListActivity,documentID: String) {
        mFireStore.collection(Constants.BOARDS)
            .document(documentID)
            .get()
            .addOnSuccessListener {
                    document->
                Log.i(activity.javaClass.simpleName,document.toString())

                val board = document.toObject(Board::class.java)!!
                board.documentID = document.id
                activity.boardDetails(board)
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Error while creating the board",it)
            }
    }

    fun getAssignedMembersListDetails(activity: Activity,assignedTo: ArrayList<String>){
        mFireStore.collection(Constants.USERS)
            .whereIn(Constants.ID,assignedTo)
            .get()
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName,it.documents.toString())

                val usersList: ArrayList<User> = ArrayList()

                for(i in it.documents){
                    val user = i.toObject(User::class.java)
                    if (user != null) {
                        usersList.add(user)
                    }
                }
                if(activity is MembersActivity)
                    activity.setUpMembersList(usersList)
                else if(activity is TaskListActivity)
                    activity.boardMembersDetailsList(usersList)
            }
            .addOnFailureListener {
                if(activity is MembersActivity)
                    activity.hideProgressDialog()
                else if(activity is TaskListActivity)
                    activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Error while creating Board",it)
            }
    }

    fun getMemberDetails(activity: MembersActivity, email: String) {
        mFireStore.collection(Constants.USERS)
            .whereEqualTo(Constants.EMAIL,email)
            .get()
            .addOnSuccessListener { document->

                if(document.documents.size > 0){
                    val user = document.documents[0].toObject(User::class.java)!!
                    activity.memberDetails(user)

                }else{
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("No such Member Found")
                }
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,
                "Error while getting user Details",it)
            }
    }

    fun assignMemberToBoard(activity: MembersActivity,board: Board,user: User){

        val assignedToHshMap = HashMap<String,Any>()
        assignedToHshMap[Constants.ASSIGNED_TO] = board.assignedTo

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentID)
            .update(assignedToHshMap)
            .addOnSuccessListener {
                activity.memberAssignSuccess(user)
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,
                    "Error while getting user Details",it)
            }

    }

}