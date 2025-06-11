package com.example.travelonna

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView
import com.example.travelonna.model.Post
import com.example.travelonna.model.Comment
import com.example.travelonna.util.PostManager
import com.example.travelonna.adapter.CommentAdapter
import com.example.travelonna.api.CommentsResponse
import com.example.travelonna.api.CommentData
import com.example.travelonna.api.RetrofitClient
import com.example.travelonna.api.CreateCommentRequest
import com.example.travelonna.api.UpdateCommentResponse
import com.example.travelonna.api.DeleteCommentResponse
import com.example.travelonna.api.LikeToggleResponse
import com.example.travelonna.api.LogDetailApiResponse
import com.example.travelonna.api.FollowRequest
import com.example.travelonna.api.FollowResponse
import com.example.travelonna.api.UnfollowResponse
import com.example.travelonna.api.FollowStatusResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostDetailActivity : AppCompatActivity(), PostManager.PostUpdateListener, CommentAdapter.CommentActionListener {
    
    companion object {
        const val EXTRA_POST_ID = "extra_post_id"
        const val RESULT_REFRESH_NEEDED = "result_refresh_needed"
        private const val TAG = "PostDetailActivity"
    }
    
    private var hasDataChanged = false
    
    private lateinit var backButton: ImageView
    private lateinit var postImage: ImageView
    private lateinit var userName: TextView
    private lateinit var scheduleButton: Button
    private lateinit var postContent: TextView
    private lateinit var postDate: TextView
    private lateinit var likeIcon: ImageView
    private lateinit var likeCount: TextView
    private lateinit var commentIcon: ImageView
    private lateinit var commentCount: TextView
    private lateinit var commentEditText: EditText
    private lateinit var sendButton: ImageView
    private lateinit var followText: TextView
    private lateinit var followToggle: Switch
    private lateinit var commentHeader: TextView
    private lateinit var commentsRecyclerView: RecyclerView
    private lateinit var noCommentsText: TextView
    private lateinit var refreshButton: ImageView
    
    private lateinit var commentAdapter: CommentAdapter
    
    // 게시물 ID
    private var postId: Long = -1L
    private var currentPost: Post? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)
        
        // Initialize views
        initViews()
        
        // Set up listeners
        setupListeners()
        
        // Set up comments RecyclerView
        setupCommentsRecyclerView()
        
        // Load post data
        postId = intent.getLongExtra(EXTRA_POST_ID, -1L)
        if (postId != -1L) {
            loadPostData(postId)
            loadComments(postId)
        } else {
            // If no post ID, load dummy data for preview
            loadDummyData()
        }
        
        // PostManager 리스너 등록
        PostManager.addListener(this)
    }
    
    override fun onResume() {
        super.onResume()
        // 액티비티가 다시 활성화될 때 PostManager에서 최신 데이터를 다시 확인
        if (postId != -1L) {
            val updatedPost = PostManager.getPost(postId)
            updatedPost?.let { post ->
                currentPost = post
                updateUI(post)
                Log.d(TAG, "Post data refreshed from PostManager: likes=${post.likeCount}, comments=${post.commentCount}")
                
                // 댓글 수가 0이면 실제로 0인지 API로 다시 확인
                if (post.commentCount == 0) {
                    Log.d(TAG, "Post has 0 comments in onResume, verifying with real API call")
                    loadRealCommentCount(postId)
                }
                
                // 좋아요 수도 실제 서버 데이터로 확인
                Log.d(TAG, "Post shows ${post.likeCount} likes in onResume, verifying with real API call")
                loadRealLikeData(postId)
                
                // 팔로우 관련 UI 숨기기 (PostDetailActivity에서는 팔로우 기능 비활성화)
                followToggle.visibility = View.GONE
                followText.visibility = View.GONE
            } ?: run {
                // PostManager에 데이터가 없고 현재 UI에 기본값이 표시되고 있다면 실제 데이터 로드 시도
                if (commentCount.text == "-" || likeCount.text == "?") {
                    Log.d(TAG, "PostManager still empty and UI shows placeholder, attempting to load real data")
                    loadRealCommentCount(postId)
                }
                // 팔로우 관련 UI 숨기기
                followToggle.visibility = View.GONE
                followText.visibility = View.GONE
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // 메모리 누수 방지를 위해 리스너 제거
        PostManager.removeListener(this)
    }
    
    private fun initViews() {
        backButton = findViewById(R.id.backButton)
        postImage = findViewById(R.id.postImage)
        userName = findViewById(R.id.userName)
        scheduleButton = findViewById(R.id.scheduleButton)
        postContent = findViewById(R.id.postContent)
        postDate = findViewById(R.id.postDate)
        likeIcon = findViewById(R.id.likeIcon)
        likeCount = findViewById(R.id.likeCount)
        commentIcon = findViewById(R.id.commentIcon)
        commentCount = findViewById(R.id.commentCount)
        commentEditText = findViewById(R.id.commentEditText)
        sendButton = findViewById(R.id.sendButton)
        followText = findViewById(R.id.followText)
        followToggle = findViewById(R.id.followToggle)
        commentHeader = findViewById(R.id.commentHeader)
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView)
        noCommentsText = findViewById(R.id.noCommentsText)
        refreshButton = findViewById(R.id.refreshButton)
    }
    
    private fun setupCommentsRecyclerView() {
        commentAdapter = CommentAdapter(emptyList(), this)
        commentsRecyclerView.layoutManager = LinearLayoutManager(this)
        commentsRecyclerView.adapter = commentAdapter
    }
    
    private fun setupListeners() {
        // Back button click listener
        backButton.setOnClickListener {
            finishWithResult()
        }
        
        // Like button click listener
        likeIcon.setOnClickListener {
            if (postId != -1L) {
                toggleLike(postId.toInt())
            }
        }
        
        // Comment send button click listener
        sendButton.setOnClickListener {
            sendComment()
        }
        
        // Schedule button click listener
        scheduleButton.setOnClickListener {
            // 현재 게시물의 planId를 가져와서 전달
            val planId = currentPost?.planId ?: 0
            
            val intent = Intent(this, TravelPlanDetailActivity::class.java).apply {
                putExtra(TravelPlanDetailActivity.EXTRA_POST_ID, postId)
                putExtra(TravelPlanDetailActivity.EXTRA_PLAN_ID, planId)
            }
            startActivity(intent)
        }
        
        // Refresh button click listener
        refreshButton.setOnClickListener {
            loadComments(postId)
        }
    }
    
    private fun loadPostData(postId: Long) {
        // PostManager에서 게시물 데이터 가져오기
        Log.d(TAG, "Loading post data for postId: $postId")
        currentPost = PostManager.getPost(postId)
        currentPost?.let { post ->
            Log.d(TAG, "Found existing post in PostManager: likes=${post.likeCount}, comments=${post.commentCount}")
            Log.d(TAG, "Post details: userId=${post.userId}, isFollowing=${post.isFollowing}, postId=${post.id}")
            updateUI(post)
            
            // PostManager에 있는 데이터의 댓글 수가 0이면 실제 API로 다시 확인
            if (post.commentCount == 0) {
                Log.d(TAG, "Post has 0 comments, verifying with real API call")
                loadRealCommentCount(postId)
            }
            
            // 좋아요 수도 실제 서버 데이터로 확인
            Log.d(TAG, "Post shows ${post.likeCount} likes, verifying with real API call")
            loadRealLikeData(postId)
            
            // 팔로우 관련 UI 숨기기 (PostDetailActivity에서는 팔로우 기능 비활성화)
            followToggle.visibility = View.GONE
            followText.visibility = View.GONE
        } ?: run {
            // 게시물을 찾을 수 없으면 더미 데이터를 로드하되, 실제 댓글 수는 API에서 가져오기
            Log.d(TAG, "No post found in PostManager for postId: $postId, loading dummy data with real comment count")
            loadDummyDataWithRealCommentCount(postId)
        }
    }
    
    private fun loadComments(postId: Long) {
        Log.d(TAG, "Loading comments for logId: $postId")
        
        RetrofitClient.apiService.getLogComments(postId.toInt()).enqueue(object : Callback<CommentsResponse> {
            override fun onResponse(call: Call<CommentsResponse>, response: Response<CommentsResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val commentsResponse = response.body()!!
                    
                    if (commentsResponse.success) {
                        Log.d(TAG, "Comments loaded: ${commentsResponse.data.size} items")
                        updateCommentsUI(commentsResponse.data)
                    } else {
                        Log.w(TAG, "Failed to load comments: ${commentsResponse.message}")
                        showCommentsError("댓글 로드에 실패했습니다.")
                    }
                } else {
                    Log.e(TAG, "HTTP error loading comments: ${response.code()}")
                    
                    when (response.code()) {
                        404 -> {
                            Log.w(TAG, "Log not found for comments")
                            // 404는 댓글이 없는 것으로 처리
                            updateCommentsUI(emptyList())
                        }
                        500 -> {
                            showCommentsError("서버에 일시적인 문제가 있습니다. 잠시 후 다시 시도해주세요.")
                        }
                        else -> {
                            showCommentsError("댓글을 불러올 수 없습니다.")
                        }
                    }
                }
            }
            
            override fun onFailure(call: Call<CommentsResponse>, t: Throwable) {
                Log.e(TAG, "Network error loading comments", t)
                showCommentsError("네트워크 연결을 확인해주세요.")
            }
        })
    }
    
    private fun updateCommentsUI(comments: List<CommentData>) {
        if (comments.isEmpty()) {
            noCommentsText.text = "아직 댓글이 없습니다"
            noCommentsText.visibility = View.VISIBLE
            commentsRecyclerView.visibility = View.GONE
        } else {
            noCommentsText.visibility = View.GONE
            commentsRecyclerView.visibility = View.VISIBLE
            commentAdapter.updateComments(comments)
        }
        
        // 댓글 헤더 업데이트
        val commentCountText = if (comments.isEmpty()) "댓글" else "댓글 ${comments.size}개"
        commentHeader.text = commentCountText
    }
    
    private fun updateUI(post: Post) {
        // 이미지 처리 - 이미지가 있는 경우만 표시
        if (post.hasImage) {
            postImage.visibility = View.VISIBLE
            if (post.imageUrl != null && post.imageUrl.isNotEmpty()) {
                // TODO: Glide를 사용하여 실제 이미지 로드
                postImage.setImageResource(post.imageResource)
            } else {
                postImage.setImageResource(post.imageResource)
            }
        } else {
            // 이미지가 없는 경우 이미지뷰 숨김
            postImage.visibility = View.GONE
        }
        
        userName.text = post.userName
        postContent.text = post.description
        postDate.text = post.date
        likeCount.text = post.likeCount.toString()
        commentCount.text = post.commentCount.toString()
        
        // 좋아요 상태 업데이트
        updateLikeUI(post.isLiked)
    }
    
    private fun updateLikeUI(isLiked: Boolean) {
        if (isLiked) {
            likeIcon.setImageResource(R.drawable.ic_like_filled)
            likeIcon.setColorFilter(getColor(R.color.red))
            likeIcon.tag = "liked"
        } else {
            likeIcon.setImageResource(R.drawable.ic_like_outline)
            likeIcon.setColorFilter(getColor(R.color.gray_text))
            likeIcon.tag = "unliked"
        }
    }
    
    private fun loadDummyData() {
        // Set dummy post data
        postImage.setImageResource(R.drawable.main_dummy_1)
        userName.text = "Pro_traveler_kr"
        postContent.text = "지난 주에 일본 가벼지만을 다녀왔어요~ 모던하면서도 자분하고 깔끔한 느낌이었어요. 그 외 내용들을 적어주세요. 그 외 내용들을 적어주세요. 그외 내용들을 적어주세요. 그 외 내용들을 적어주세요. 그 외 내용들을 적어주세요."
        postDate.text = "2025.08.17"
        likeCount.text = "125"
        commentCount.text = "23"
        
        // 팔로우 관련 UI 숨기기 (PostDetailActivity에서는 팔로우 기능 비활성화)
        followToggle.visibility = View.GONE
        followText.visibility = View.GONE
        
        // 빈 댓글 목록 표시
        updateCommentsUI(emptyList())
    }
    
    private fun loadDummyDataWithRealCommentCount(postId: Long) {
        Log.d(TAG, "Loading dummy data with real comment count for postId: $postId")
        
        // Set dummy post data with placeholders
        postImage.setImageResource(R.drawable.main_dummy_1)
        userName.text = "User_Unknown"  // 실제 사용자 정보 알 수 없음
        postContent.text = "게시물을 불러오는 중..."  // 실제 내용 알 수 없음
        postDate.text = "2025.06.10"
        likeCount.text = "?"  // 서버 오류로 알 수 없음
        commentCount.text = "-"  // 로딩 중
        
        // 팔로우 관련 UI 숨기기 (PostDetailActivity에서는 팔로우 기능 비활성화)
        followToggle.visibility = View.GONE
        followText.visibility = View.GONE
        
        // 실제 댓글 수를 API에서 가져오기
        loadRealCommentCount(postId)
        
        // 빈 댓글 목록 표시
        updateCommentsUI(emptyList())
    }
    
    private fun loadRealCommentCount(postId: Long) {
        Log.d(TAG, "Loading real comment count for postId: $postId")
        
        // UI를 로딩 상태로 설정
        commentCount.text = "로딩중..."
        Log.d(TAG, "Set comment count to loading state: '${commentCount.text}'")
        
        RetrofitClient.apiService.getLogComments(postId.toInt()).enqueue(object : Callback<CommentsResponse> {
            override fun onResponse(call: Call<CommentsResponse>, response: Response<CommentsResponse>) {
                Log.d(TAG, "Comment API response received for postId: $postId, code: ${response.code()}")
                
                if (response.isSuccessful && response.body() != null) {
                    val commentsResponse = response.body()!!
                    
                    if (commentsResponse.success) {
                        val realCommentCount = commentsResponse.data.size
                        
                        // 메인 스레드에서 UI 업데이트
                        runOnUiThread {
                            commentCount.text = realCommentCount.toString()
                            Log.d(TAG, "Real comment count loaded and UI updated: $realCommentCount")
                            Log.d(TAG, "CommentCount TextView now shows: '${commentCount.text}'")
                        }
                        
                        // PostManager에도 업데이트하여 다른 화면과 동기화
                        updatePostInPostManager(postId, realCommentCount)
                    } else {
                        runOnUiThread {
                            commentCount.text = "?"
                            Log.w(TAG, "Failed to load real comment count: ${commentsResponse.message}")
                        }
                    }
                } else {
                    runOnUiThread {
                        commentCount.text = "?"
                        Log.e(TAG, "HTTP error loading real comment count: ${response.code()}")
                        if (response.errorBody() != null) {
                            try {
                                val errorBodyString = response.errorBody()?.string()
                                Log.e(TAG, "Error response body: $errorBodyString")
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to read error body", e)
                            }
                        }
                    }
                }
            }
            
            override fun onFailure(call: Call<CommentsResponse>, t: Throwable) {
                runOnUiThread {
                    commentCount.text = "?"
                    Log.e(TAG, "Network error loading real comment count", t)
                }
            }
        })
    }
    
    private fun loadRealLikeData(postId: Long) {
        Log.d(TAG, "Loading real like data for logId: $postId")
        
        // 좋아요 수 로딩 상태 표시
        runOnUiThread {
            likeCount.text = "로딩중..."
            Log.d(TAG, "Set like count to loading state: '로딩중...'")
        }
        
        RetrofitClient.apiService.getLogDetailFromApi(postId.toInt()).enqueue(object : Callback<LogDetailApiResponse> {
            override fun onResponse(call: Call<LogDetailApiResponse>, response: Response<LogDetailApiResponse>) {
                Log.d(TAG, "Like data API response received for postId: $postId, code: ${response.code()}")
                
                if (response.isSuccessful && response.body() != null) {
                    val logDetailResponse = response.body()!!
                    
                    if (logDetailResponse.success) {
                        val logData = logDetailResponse.data
                        
                        // 메인 스레드에서 UI 업데이트
                        runOnUiThread {
                            likeCount.text = logData?.likeCount?.toString() ?: "?"
                            updateLikeUI(logData?.isLiked ?: false)
                            Log.d(TAG, "Real like data loaded and UI updated: count=${logData?.likeCount}, isLiked=${logData?.isLiked}")
                            Log.d(TAG, "LikeCount TextView now shows: '${likeCount.text}'")
                        }
                        
                        // PostManager에도 업데이트하여 다른 화면과 동기화
                        logData?.let {
                            updatePostInPostManagerForLike(postId, it.likeCount, it.isLiked)
                        }
                    } else {
                        runOnUiThread {
                            likeCount.text = "?"
                            Log.w(TAG, "Failed to load real like data: ${logDetailResponse.message}")
                        }
                    }
                } else {
                    runOnUiThread {
                        likeCount.text = "?"
                        Log.e(TAG, "HTTP error loading real like data: ${response.code()}")
                        if (response.errorBody() != null) {
                            try {
                                val errorBodyString = response.errorBody()?.string()
                                Log.e(TAG, "Error response body: $errorBodyString")
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to read error body", e)
                            }
                        }
                    }
                }
            }
            
            override fun onFailure(call: Call<LogDetailApiResponse>, t: Throwable) {
                runOnUiThread {
                    likeCount.text = "?"
                    Log.e(TAG, "Network error loading real like data", t)
                }
            }
        })
    }

    private fun updatePostInPostManager(postId: Long, commentCount: Int) {
        val existingPost = PostManager.getPost(postId)
        if (existingPost != null) {
            // 기존 Post가 있으면 댓글 수만 업데이트
            val updatedPost = existingPost.copy(commentCount = commentCount)
            PostManager.updatePost(updatedPost)
            Log.d(TAG, "Updated existing post in PostManager with comment count: $commentCount")
        } else {
            // 새 Post 생성해서 등록 (최소한의 정보로)
            val post = Post(
                id = postId,
                imageResource = R.drawable.main_dummy_1,
                imageUrl = null,
                hasImage = true, // 기본적으로 이미지가 있다고 가정
                userName = userName.text.toString(),
                isFollowing = followToggle.isChecked,
                description = postContent.text.toString(),
                date = postDate.text.toString(),
                isLiked = likeIcon.tag == "liked",
                likeCount = likeCount.text.toString().toIntOrNull() ?: 0,
                commentCount = commentCount
            )
            PostManager.updatePost(post)
            currentPost = post
            Log.d(TAG, "Created new post in PostManager with comment count: $commentCount")
        }
    }
    
    private fun updatePostInPostManagerForLike(postId: Long, likeCount: Int, isLiked: Boolean) {
        val existingPost = PostManager.getPost(postId)
        if (existingPost != null) {
            // 기존 Post가 있으면 좋아요 데이터만 업데이트
            val updatedPost = existingPost.copy(
                likeCount = likeCount,
                isLiked = isLiked
            )
            PostManager.updatePost(updatedPost)
            Log.d(TAG, "Updated existing post in PostManager with like data: count=$likeCount, isLiked=$isLiked")
        } else {
            // 새 Post 생성해서 등록 (최소한의 정보로)
            val post = Post(
                id = postId,
                imageResource = R.drawable.main_dummy_1,
                imageUrl = null,
                hasImage = true, // 기본적으로 이미지가 있다고 가정
                userName = userName.text.toString(),
                isFollowing = followToggle.isChecked,
                description = postContent.text.toString(),
                date = postDate.text.toString(),
                isLiked = isLiked,
                likeCount = likeCount,
                commentCount = commentCount.text.toString().toIntOrNull() ?: 0
            )
            PostManager.updatePost(post)
            currentPost = post
            Log.d(TAG, "Created new post in PostManager with like data: count=$likeCount, isLiked=$isLiked")
        }
    }
    
    private fun sendComment() {
        val commentText = commentEditText.text.toString().trim()
        if (commentText.isNotEmpty() && postId != -1L) {
            Log.d(TAG, "Creating comment for logId: $postId")
            
            val request = CreateCommentRequest(
                comment = commentText,
                parentId = null // 일반 댓글 (대댓글이 아님)
            )
            
            // 전송 버튼 비활성화 (중복 전송 방지)
            sendButton.isEnabled = false
            
            RetrofitClient.apiService.createComment(postId.toInt(), request).enqueue(object : Callback<CommentData> {
                override fun onResponse(call: Call<CommentData>, response: Response<CommentData>) {
                    sendButton.isEnabled = true
                    
                    if (response.isSuccessful && response.body() != null) {
                        val newComment = response.body()!!
                        Log.d(TAG, "Comment created successfully: ${newComment.commentId}")
                        
                        // 입력 필드 클리어
                        commentEditText.setText("")
                        
                        // 새 댓글을 즉시 목록에 추가
                        commentAdapter.addComment(newComment)
                        
                        // 댓글 목록이 비어있었다면 UI 업데이트
                        if (commentAdapter.itemCount == 1) {
                            noCommentsText.visibility = View.GONE
                            commentsRecyclerView.visibility = View.VISIBLE
                        }
                        
                        // 댓글 헤더 업데이트
                        commentHeader.text = "댓글 ${commentAdapter.itemCount}개"
                        
                        // 댓글 수 UI 업데이트
                        val currentCommentCount = commentCount.text.toString().toIntOrNull() ?: 0
                        commentCount.text = (currentCommentCount + 1).toString()
                        
                        // 새 댓글로 스크롤
                        commentsRecyclerView.scrollToPosition(0)
                        hasDataChanged = true
                        
                        Toast.makeText(this@PostDetailActivity, "댓글이 작성되었습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e(TAG, "HTTP error creating comment: ${response.code()}")
                        
                        when (response.code()) {
                            400 -> {
                                Toast.makeText(this@PostDetailActivity, "잘못된 요청입니다.", Toast.LENGTH_SHORT).show()
                            }
                            404 -> {
                                Toast.makeText(this@PostDetailActivity, "게시물을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                Toast.makeText(this@PostDetailActivity, "댓글 작성에 실패했습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                
                override fun onFailure(call: Call<CommentData>, t: Throwable) {
                    sendButton.isEnabled = true
                    Log.e(TAG, "Network error creating comment", t)
                    Toast.makeText(this@PostDetailActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
    
    override fun onPostUpdated(post: Post) {
        // 현재 게시물이 업데이트되면 UI 반영
        if (post.id == postId) {
            runOnUiThread {
                currentPost = post
                updateUI(post)
            }
        }
    }
    
    override fun onCommentsUpdated(postId: Long, comments: List<Comment>) {
        // TODO: 실제 댓글 API 연동 후 구현 예정
        // 현재는 PostManager 기반이므로 주석처리
    }
    
    override fun onEditComment(comment: CommentData, position: Int) {
        // 댓글 수정 다이얼로그 표시
        showEditCommentDialog(comment, position)
    }
    
    override fun onDeleteComment(comment: CommentData, position: Int) {
        // 삭제 확인 다이얼로그 표시
        android.app.AlertDialog.Builder(this)
            .setTitle("댓글 삭제")
            .setMessage("정말로 댓글을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                deleteComment(comment.commentId, position)
            }
            .setNegativeButton("취소", null)
            .show()
    }
    
    private fun deleteComment(commentId: Int, position: Int) {
        Log.d(TAG, "Deleting comment: $commentId")
        
        RetrofitClient.apiService.deleteComment(commentId).enqueue(object : Callback<DeleteCommentResponse> {
            override fun onResponse(call: Call<DeleteCommentResponse>, response: Response<DeleteCommentResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val deleteResponse = response.body()!!
                    
                    if (deleteResponse.success) {
                        Log.d(TAG, "Comment deleted successfully")
                        
                        // 어댑터에서 댓글 제거
                        commentAdapter.removeComment(position)
                        
                        // 댓글 헤더 업데이트
                        val remainingCommentCount = commentAdapter.itemCount
                        if (remainingCommentCount == 0) {
                            noCommentsText.visibility = View.VISIBLE
                            commentsRecyclerView.visibility = View.GONE
                            commentHeader.text = "댓글"
                        } else {
                            commentHeader.text = "댓글 ${remainingCommentCount}개"
                        }
                        
                        // 댓글 수 UI 업데이트
                        val currentCommentCount = commentCount.text.toString().toIntOrNull() ?: 0
                        commentCount.text = (currentCommentCount - 1).toString()
                        hasDataChanged = true
                        
                        Toast.makeText(this@PostDetailActivity, "댓글이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.w(TAG, "Failed to delete comment: ${deleteResponse.message}")
                        Toast.makeText(this@PostDetailActivity, "댓글 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e(TAG, "HTTP error deleting comment: ${response.code()}")
                    
                    when (response.code()) {
                        403 -> {
                            Toast.makeText(this@PostDetailActivity, "댓글 삭제 권한이 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                        404 -> {
                            Toast.makeText(this@PostDetailActivity, "댓글을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            Toast.makeText(this@PostDetailActivity, "댓글 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            
            override fun onFailure(call: Call<DeleteCommentResponse>, t: Throwable) {
                Log.e(TAG, "Network error deleting comment", t)
                Toast.makeText(this@PostDetailActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }
    
    private fun showEditCommentDialog(comment: CommentData, position: Int) {
        val editText = EditText(this).apply {
            setText(comment.comment)
            selectAll()
        }
        
        android.app.AlertDialog.Builder(this)
            .setTitle("댓글 수정")
            .setView(editText)
            .setPositiveButton("수정") { _, _ ->
                val newCommentText = editText.text.toString().trim()
                if (newCommentText.isNotEmpty() && newCommentText != comment.comment) {
                    updateComment(comment.commentId, newCommentText, comment.parentId, position)
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }
    
    private fun updateComment(commentId: Int, newComment: String, parentId: Int?, position: Int) {
        Log.d(TAG, "Updating comment: $commentId")
        
        val request = CreateCommentRequest(
            comment = newComment,
            parentId = parentId
        )
        
        RetrofitClient.apiService.updateComment(commentId, request).enqueue(object : Callback<UpdateCommentResponse> {
            override fun onResponse(call: Call<UpdateCommentResponse>, response: Response<UpdateCommentResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val updateResponse = response.body()!!
                    
                    if (updateResponse.success) {
                        Log.d(TAG, "Comment updated successfully")
                        
                        // 어댑터에서 댓글 업데이트
                        commentAdapter.updateComment(position, updateResponse.data)
                        hasDataChanged = true
                        
                        Toast.makeText(this@PostDetailActivity, "댓글이 수정되었습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.w(TAG, "Failed to update comment: ${updateResponse.message}")
                        Toast.makeText(this@PostDetailActivity, "댓글 수정에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e(TAG, "HTTP error updating comment: ${response.code()}")
                    
                    when (response.code()) {
                        403 -> {
                            Toast.makeText(this@PostDetailActivity, "댓글 수정 권한이 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                        404 -> {
                            Toast.makeText(this@PostDetailActivity, "댓글을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            Toast.makeText(this@PostDetailActivity, "댓글 수정에 실패했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            
            override fun onFailure(call: Call<UpdateCommentResponse>, t: Throwable) {
                Log.e(TAG, "Network error updating comment", t)
                Toast.makeText(this@PostDetailActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }
    
    private fun showCommentsError(message: String) {
        // 이미 댓글이 표시된 경우 에러 메시지만 토스트로 표시
        if (commentAdapter.itemCount > 0) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        } else {
            // 댓글이 없는 경우 에러 메시지를 텍스트로 표시
            noCommentsText.text = message
            noCommentsText.visibility = View.VISIBLE
            commentsRecyclerView.visibility = View.GONE
        }
    }
    
    private fun toggleLike(logId: Int) {
        Log.d(TAG, "Toggling like for logId: $logId")
        
        // 현재 좋아요 상태와 수 저장 (에러 시 복원용)
        val isCurrentlyLiked = likeIcon.tag == "liked"
        val currentLikeCountText = likeCount.text.toString()
        
        // 즉시 UI 업데이트 (낙관적 업데이트)
        updateLikeUI(!isCurrentlyLiked)
        val currentCount = currentLikeCountText.toIntOrNull() ?: 0
        val newCount = if (!isCurrentlyLiked) currentCount + 1 else maxOf(0, currentCount - 1)
        likeCount.text = newCount.toString()
        
        // API 호출
        RetrofitClient.apiService.toggleLogLike(logId).enqueue(object : Callback<LikeToggleResponse> {
            override fun onResponse(call: Call<LikeToggleResponse>, response: Response<LikeToggleResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val likeResponse = response.body()!!
                    
                    if (likeResponse.success) {
                        // API 응답에 따라 UI 상태 확정
                        val isLiked = likeResponse.data
                        Log.d(TAG, "Like toggle successful. Current state: $isLiked")
                        
                        updateLikeUI(isLiked)
                        
                        // 좋아요 수는 현재 UI 상태 유지 (서버에서 정확한 수를 주지 않으므로)
                        hasDataChanged = true // 데이터 변경 플래그 설정
                        
                        // PostManager에도 업데이트하여 다른 화면과 동기화
                        updatePostInPostManagerForLike(postId, newCount, isLiked)
                        
                    } else {
                        // API 실패 시 원래 상태로 되돌리기
                        Log.w(TAG, "Like toggle failed: ${likeResponse.message}")
                        updateLikeUI(isCurrentlyLiked)
                        likeCount.text = currentLikeCountText
                        Toast.makeText(this@PostDetailActivity, "좋아요 처리에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // HTTP 에러 시 원래 상태로 되돌리기
                    Log.e(TAG, "Like toggle HTTP error: ${response.code()}")
                    updateLikeUI(isCurrentlyLiked)
                    likeCount.text = currentLikeCountText
                    
                    val errorMessage = when (response.code()) {
                        404 -> "게시물을 찾을 수 없습니다."
                        else -> "좋아요 처리에 실패했습니다."
                    }
                    Toast.makeText(this@PostDetailActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
            
            override fun onFailure(call: Call<LikeToggleResponse>, t: Throwable) {
                Log.e(TAG, "Like toggle network error", t)
                
                // 네트워크 에러 시 원래 상태로 되돌리기
                updateLikeUI(isCurrentlyLiked)
                likeCount.text = currentLikeCountText
                Toast.makeText(this@PostDetailActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }
    
    private fun finishWithResult() {
        if (hasDataChanged) {
            val resultIntent = Intent().apply {
                putExtra(RESULT_REFRESH_NEEDED, true)
            }
            setResult(RESULT_OK, resultIntent)
            Log.d(TAG, "Finishing with refresh result due to data changes")
        }
        finish()
    }
    
    override fun onBackPressed() {
        finishWithResult()
    }
} 