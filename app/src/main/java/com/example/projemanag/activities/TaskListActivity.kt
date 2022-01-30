package com.example.projemanag.activities

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projemanag.R
import com.example.projemanag.adapters.TaskListItemsAdapter
import com.example.projemanag.databinding.ActivityTaskListBinding
import com.example.projemanag.firebase.FireStoreClass
import com.example.projemanag.models.Board
import com.example.projemanag.models.Card
import com.example.projemanag.models.Task
import com.example.projemanag.utils.Constants

class TaskListActivity : BaseActivity() {

    private lateinit var binding: ActivityTaskListBinding

    private lateinit var mBoardDetails: Board

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var boardDocumentID = ""
        if(intent.hasExtra(Constants.DOCUMENT_ID)){
            boardDocumentID = intent.getStringExtra(Constants.DOCUMENT_ID)!!
        }

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().getBoardDetails(this,boardDocumentID)

    }

    private fun setupActionBar() {

        setSupportActionBar(binding.toolbarTaskListActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = mBoardDetails.name
        }

        binding.toolbarTaskListActivity.setNavigationOnClickListener { onBackPressed() }
    }

    fun boardDetails(board: Board){

        mBoardDetails = board

        hideProgressDialog()
        setupActionBar()

        val addTaskList = Task(resources.getString(R.string.add_list))
        board.taskList.add(addTaskList)

        binding.rvTaskList.layoutManager = LinearLayoutManager(
            this,LinearLayoutManager.HORIZONTAL,false)
        binding.rvTaskList.setHasFixedSize(true)

        val adapter = TaskListItemsAdapter(this,board.taskList)
        binding.rvTaskList.adapter = adapter

    }

    fun addUpdateTaskListSuccess(){
        hideProgressDialog()

        showProgressDialog(resources.getString(R.string.please_wait))

        FireStoreClass().getBoardDetails(this,mBoardDetails.documentID)
    }

    fun createTaskList(taskListName: String){
        val task = Task(taskListName,FireStoreClass().getCurrentUserId())
        mBoardDetails.taskList.add(0,task)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))

        FireStoreClass().addUpdateTaskList(this,mBoardDetails)

    }

    fun updateTaskList(position: Int,listName: String,model: Task){
        val task = Task(listName,model.createdBy)

        mBoardDetails.taskList[position] = task
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))

        FireStoreClass().addUpdateTaskList(this,mBoardDetails)

    }

    fun deleteTaskList(position: Int){
        mBoardDetails.taskList.removeAt(position)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))

        FireStoreClass().addUpdateTaskList(this,mBoardDetails)
    }

    fun addCardToTaskList(position: Int,cardName: String){
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        val cardAssignedUsersList: ArrayList<String> = ArrayList()
        cardAssignedUsersList.add(FireStoreClass().getCurrentUserId())

        val card = Card(cardName,FireStoreClass().getCurrentUserId(),cardAssignedUsersList)

        val cardsList = mBoardDetails.taskList[position].cards
        cardsList.add(card)

        val task = Task(
            mBoardDetails.taskList[position].title,
            mBoardDetails.taskList[position].createdBy,
            cardsList
        )

        mBoardDetails.taskList[position] = task

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this,mBoardDetails)

    }

}