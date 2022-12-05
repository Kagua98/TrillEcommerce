package com.trill.ecommerce.screens.menu.comments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.trill.ecommerce.adapter.CommentsAdapter
import com.trill.ecommerce.callback.ICommentCallBackListener
import com.trill.ecommerce.databinding.FragmentCommentsBinding
import com.trill.ecommerce.model.CommentsModel
import com.trill.ecommerce.util.Common
import com.trill.ecommerce.viewmodel.CommentsViewModel

class CommentsFragment : BottomSheetDialogFragment(), ICommentCallBackListener {

    private var _binding: FragmentCommentsBinding? = null
    private val binding get() = _binding!!

    private var commentsViewModel: CommentsViewModel? = null
    private var listener: ICommentCallBackListener = this


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCommentsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        commentsViewModel = ViewModelProvider(this)[CommentsViewModel::class.java]

        loadComments(root)

        commentsViewModel!!.mutableLiveDataCommentsList!!.observe(this, Observer { commentsList ->
            val adapter = CommentsAdapter(requireContext(), commentsList)
            binding.recyclerView.adapter = adapter
        })

        return root
    }

    private fun loadComments(root: View) {
        //show progressbar


        val recyclerView = binding.recyclerView
        recyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, true)
        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(DividerItemDecoration(context, layoutManager.orientation))

        val mCommentsModel = ArrayList<CommentsModel>()
        FirebaseDatabase.getInstance().getReference(Common.COMMENT_REFERENCE)
            .child(Common.listItemSelected!!.id!!)
            .orderByChild("commentTimeStamp")
            .limitToLast(100)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (commentSnapshot in snapshot.children) {
                        val commentsModel =
                            commentSnapshot.getValue<CommentsModel>(CommentsModel::class.java)
                        mCommentsModel.add(commentsModel!!)
                    }
                    listener.onCommentsLoadSuccess(mCommentsModel)

                }

                override fun onCancelled(error: DatabaseError) {
                    listener.onCommentsLoadFailed(error.message)
                }

            })

    }

    override fun onCommentsLoadSuccess(commentsList: List<CommentsModel>) {
        //dismiss progress bar
        commentsViewModel!!.setCommentsList(commentsList)
    }

    override fun onCommentsLoadFailed(message: String) {
        Snackbar.make(requireView(), "" + message, Snackbar.LENGTH_LONG).show()
        //dismiss progress bar
    }

    companion object {
        private var instance: CommentsFragment? = null

        fun getInstance(): CommentsFragment {
            if (instance == null)
                instance = CommentsFragment()
            return instance!!
        }
    }
}