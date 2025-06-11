package com.example.travelonna.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.travelonna.PostDetailActivity
import com.example.travelonna.R
import com.example.travelonna.model.Post
import com.example.travelonna.view.CustomToggleButton
import com.example.travelonna.util.PostManager
import com.example.travelonna.HomeActivity

class MainPostAdapter(private var posts: List<Post>) : 
    RecyclerView.Adapter<MainPostAdapter.MainPostViewHolder>() {

    class MainPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postImage: ImageView = itemView.findViewById(R.id.postImage)
        val userName: TextView = itemView.findViewById(R.id.userName)
        val followText: TextView = itemView.findViewById(R.id.followText)
        val followToggle: CustomToggleButton = itemView.findViewById(R.id.followToggle)
        val description: TextView = itemView.findViewById(R.id.description)
        val date: TextView = itemView.findViewById(R.id.date)
        val likeIcon: ImageView = itemView.findViewById(R.id.likeIcon)
        val likeCount: TextView = itemView.findViewById(R.id.likeCount)
        val commentIcon: ImageView = itemView.findViewById(R.id.commentIcon)
        val commentCount: TextView = itemView.findViewById(R.id.commentCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainPostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return MainPostViewHolder(view)
    }

    override fun onBindViewHolder(holder: MainPostViewHolder, position: Int) {
        val post = posts[position]
        
        // 포스트 이미지 설정 - 이미지가 있을 경우에만 표시
        if (post.hasImage) {
            holder.postImage.visibility = View.VISIBLE
            if (post.imageUrl != null && post.imageUrl.isNotEmpty()) {
                // 실제 이미지 URL이 있는 경우 Glide 등을 사용하여 로드
                // TODO: Glide를 사용한 이미지 로딩 구현
                holder.postImage.setImageResource(post.imageResource)
            } else {
                // 기본 이미지 리소스 사용
                holder.postImage.setImageResource(post.imageResource)
            }
        } else {
            // 이미지가 없는 경우 이미지뷰 숨김
            holder.postImage.visibility = View.GONE
        }
        
        // 사용자 이름 설정
        holder.userName.text = post.userName
        
        // 설명 설정
        holder.description.text = post.description
        
        // 날짜 설정
        holder.date.text = post.date
        
        // 좋아요 개수 설정
        holder.likeCount.text = post.likeCount.toString()
        
        // 댓글 개수 설정
        holder.commentCount.text = post.commentCount.toString()
        
        // 팔로우 상태에 따라 토글과 텍스트 UI 업데이트
        updateFollowStatus(holder, post.isFollowing)
        
        // 좋아요 상태에 따라 아이콘 업데이트
        updateLikeStatus(holder, post.isLiked)
        
        // 좋아요 버튼 클릭 이벤트 (이벤트 전파 방지)
        holder.likeIcon.setOnClickListener { view ->
            view.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
            PostManager.toggleLike(post.id)
        }
        
        // 댓글 아이콘 클릭 이벤트 - 상세 화면으로 이동
        holder.commentIcon.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, PostDetailActivity::class.java).apply {
                putExtra(PostDetailActivity.EXTRA_POST_ID, post.id)
            }
            
            // Activity 컨텍스트인 경우 startActivityForResult 사용
            when (context) {
                is HomeActivity -> {
                    context.startActivityForResult(intent, HomeActivity.REQUEST_POST_DETAIL)
                }
                is AppCompatActivity -> {
                    // 다른 AppCompatActivity인 경우 일반 startActivity 사용
                    context.startActivity(intent)
                }
                else -> {
                    context.startActivity(intent)
                }
            }
        }
        
        // 팔로우 토글 클릭 이벤트
        holder.followToggle.setOnCheckedChangeListener { isChecked ->
            PostManager.updateFollowStatus(post.id, isChecked)
        }
        
        // 게시물 클릭 이벤트 - 상세 화면으로 이동 (좋아요 영역 제외)
        val postClickListener = View.OnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, PostDetailActivity::class.java).apply {
                putExtra(PostDetailActivity.EXTRA_POST_ID, post.id)
            }
            
            // Activity 컨텍스트인 경우 startActivityForResult 사용
            when (context) {
                is HomeActivity -> {
                    context.startActivityForResult(intent, HomeActivity.REQUEST_POST_DETAIL)
                }
                is AppCompatActivity -> {
                    // 다른 AppCompatActivity인 경우 일반 startActivity 사용
                    context.startActivity(intent)
                }
                else -> {
                    context.startActivity(intent)
                }
            }
        }
        
        // 게시물의 여러 영역에 클릭 리스너 설정
        holder.itemView.setOnClickListener(postClickListener)
        // 이미지가 있을 경우에만 이미지 클릭 리스너 설정
        if (post.hasImage) {
            holder.postImage.setOnClickListener(postClickListener)
        }
        holder.userName.setOnClickListener(postClickListener)
        holder.description.setOnClickListener(postClickListener)
        holder.date.setOnClickListener(postClickListener)
    }

    private fun updateFollowStatus(holder: MainPostViewHolder, isFollowing: Boolean) {
        holder.followToggle.setChecked(isFollowing)
        if (isFollowing) {
            holder.followText.text = "팔로우 중"
            holder.followText.setTextColor(holder.itemView.context.getColor(R.color.blue))
        } else {
            holder.followText.text = "팔로우"
            holder.followText.setTextColor(holder.itemView.context.getColor(R.color.gray_text))
        }
    }
    
    private fun updateLikeStatus(holder: MainPostViewHolder, isLiked: Boolean) {
        if (isLiked) {
            holder.likeIcon.setImageResource(R.drawable.ic_like_filled)
            holder.likeIcon.setColorFilter(holder.itemView.context.getColor(R.color.red))
        } else {
            holder.likeIcon.setImageResource(R.drawable.ic_like_filled)
            holder.likeIcon.setColorFilter(holder.itemView.context.getColor(R.color.gray_text))
        }
    }

    override fun getItemCount(): Int = posts.size

    fun updateData(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }
} 