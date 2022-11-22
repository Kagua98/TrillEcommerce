package com.trill.ecommerce.callback

import com.trill.ecommerce.model.CommentsModel
import com.trill.ecommerce.model.MenuCategoriesModel

interface ICommentCallBackListener {
    fun onCommentsLoadSuccess(commentsList:List<CommentsModel>)
    fun onCommentsLoadFailed(message: String)
}