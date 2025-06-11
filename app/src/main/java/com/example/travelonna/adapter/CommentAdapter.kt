package com.example.travelonna.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.travelonna.R
import com.example.travelonna.api.CommentData
import java.text.SimpleDateFormat
import java.util.*

class CommentAdapter(
    private var comments: List<CommentData> = emptyList(),
    private val commentActionListener: CommentActionListener? = null
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    interface CommentActionListener {
        fun onEditComment(comment: CommentData, position: Int)
        fun onDeleteComment(comment: CommentData, position: Int)
    }

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userProfileImage: ImageView = itemView.findViewById(R.id.commentUserProfile)
        val userName: TextView = itemView.findViewById(R.id.commentUserName)
        val content: TextView = itemView.findViewById(R.id.commentContent)
        val timestamp: TextView = itemView.findViewById(R.id.commentTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        
        // 사용자 정보
        holder.userName.text = comment.userName ?: "익명"
        holder.content.text = comment.comment ?: ""
        
        // 프로필 이미지 로드
        val profileImageUrl = comment.userProfileImage
        if (!profileImageUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(profileImageUrl)
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .circleCrop()
                .into(holder.userProfileImage)
        } else {
            holder.userProfileImage.setImageResource(R.drawable.default_profile)
        }
        
        // 타임스탬프 포맷팅
        holder.timestamp.text = formatTimestamp(comment.createdAt)
        
        // 길게 누르기 이벤트 (본인 댓글인 경우만)
        holder.itemView.setOnLongClickListener {
            commentActionListener?.let { listener ->
                // TODO: 현재 사용자 ID와 비교하여 본인 댓글인지 확인
                // 임시로 모든 댓글에 대해 수정/삭제 허용
                showCommentActionDialog(holder.itemView.context, comment, position)
            }
            true
        }
    }

    private fun showCommentActionDialog(context: android.content.Context, comment: CommentData, position: Int) {
        val items = arrayOf("수정", "삭제")
        
        android.app.AlertDialog.Builder(context)
            .setItems(items) { _, which ->
                when (which) {
                    0 -> commentActionListener?.onEditComment(comment, position)
                    1 -> commentActionListener?.onDeleteComment(comment, position)
                }
            }
            .show()
    }

    override fun getItemCount(): Int = comments.size

    fun updateComments(newComments: List<CommentData>) {
        comments = newComments
        notifyDataSetChanged()
    }

    fun addComment(newComment: CommentData) {
        val updatedComments = comments.toMutableList()
        updatedComments.add(0, newComment) // 맨 위에 추가
        comments = updatedComments
        notifyItemInserted(0)
        // 댓글이 추가되면 목록의 맨 위로 스크롤
    }

    fun updateComment(position: Int, updatedComment: CommentData) {
        val updatedComments = comments.toMutableList()
        updatedComments[position] = updatedComment
        comments = updatedComments
        notifyItemChanged(position)
    }

    fun removeComment(position: Int) {
        val updatedComments = comments.toMutableList()
        updatedComments.removeAt(position)
        comments = updatedComments
        notifyItemRemoved(position)
    }

    private fun formatTimestamp(dateString: String?): String {
        if (dateString.isNullOrEmpty()) {
            return "방금 전"
        }
        
        return try {
            // API에서 받은 날짜 형식에 맞게 파싱
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            
            if (date != null) {
                val now = System.currentTimeMillis()
                val diff = now - date.time
                
                when {
                    diff < 60 * 1000 -> "방금 전"
                    diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}분 전"
                    diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}시간 전"
                    diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)}일 전"
                    else -> {
                        val outputFormat = SimpleDateFormat("MM.dd", Locale.getDefault())
                        outputFormat.format(date)
                    }
                }
            } else {
                dateString.substring(0, minOf(10, dateString.length)) // YYYY-MM-DD 부분만 표시
            }
        } catch (e: Exception) {
            // 파싱 실패시 원본 문자열의 앞부분만 표시
            dateString.take(10).replace("-", ".")
        }
    }
} 