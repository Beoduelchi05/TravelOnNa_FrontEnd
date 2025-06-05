package com.example.travelonna.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.travelonna.LogDetailActivity
import com.example.travelonna.R
import com.example.travelonna.api.RecommendationItem
import java.text.SimpleDateFormat
import java.util.*

class RecommendationAdapter(
    private var recommendations: List<RecommendationItem>
) : RecyclerView.Adapter<RecommendationAdapter.RecommendationViewHolder>() {

    class RecommendationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.userName)
        val postDescription: TextView = itemView.findViewById(R.id.description)
        val postDate: TextView = itemView.findViewById(R.id.date)
        val postImage: ImageView = itemView.findViewById(R.id.postImage)
        val likeIcon: ImageView = itemView.findViewById(R.id.likeIcon)
        val likeCount: TextView = itemView.findViewById(R.id.likeCount)
        val commentIcon: ImageView = itemView.findViewById(R.id.commentIcon)
        val commentCount: TextView = itemView.findViewById(R.id.commentCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return RecommendationViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecommendationViewHolder, position: Int) {
        val recommendation = recommendations[position]
        
        // 사용자 이름 (userId를 기반으로 표시, 실제로는 프로필 API를 호출해야 함)
        holder.userName.text = "User_${recommendation.userId}"
        
        // 게시물 설명
        holder.postDescription.text = recommendation.comment
        
        // 날짜 포맷팅
        holder.postDate.text = formatDate(recommendation.createdAt)
        
        // 기본 이미지 설정 (실제로는 이미지 URL이 있어야 함)
        holder.postImage.setImageResource(R.drawable.main_dummy_1)
        
        // 좋아요 및 댓글 수는 현재 API에서 제공되지 않으므로 기본값 설정
        holder.likeCount.text = "0"
        holder.commentCount.text = "0"
        
        // 좋아요 아이콘 기본 상태
        holder.likeIcon.setImageResource(R.drawable.ic_like_outline)
        
        // 게시물 클릭 시 상세 화면으로 이동
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, LogDetailActivity::class.java).apply {
                putExtra("LOG_ID", recommendation.logId)
            }
            context.startActivity(intent)
        }
        
        // 좋아요 버튼 클릭 리스너
        holder.likeIcon.setOnClickListener {
            // TODO: 좋아요 API 호출
            val isLiked = holder.likeIcon.tag == "liked"
            if (isLiked) {
                holder.likeIcon.setImageResource(R.drawable.ic_like_outline)
                holder.likeIcon.tag = "unliked"
            } else {
                holder.likeIcon.setImageResource(R.drawable.ic_like_filled)
                holder.likeIcon.tag = "liked"
            }
        }
        
        // 댓글 버튼 클릭 리스너
        holder.commentIcon.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, LogDetailActivity::class.java).apply {
                putExtra("LOG_ID", recommendation.logId)
                putExtra("SCROLL_TO_COMMENTS", true)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = recommendations.size

    fun updateData(newRecommendations: List<RecommendationItem>) {
        recommendations = newRecommendations
        notifyDataSetChanged()
    }
    
    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            // 파싱에 실패하면 원본 문자열의 앞 10자리만 반환 (yyyy-MM-dd)
            dateString.take(10).replace("-", ".")
        }
    }
} 