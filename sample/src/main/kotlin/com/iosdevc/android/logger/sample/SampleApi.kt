package com.iosdevc.android.logger.sample

import com.iosdevc.android.logger.Kulse
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface SampleApi {

    @GET("posts")
    suspend fun getPosts(): List<Post>

    @GET("posts/{id}")
    suspend fun getPost(@Path("id") id: Int): Post

    @POST("posts")
    suspend fun createPost(@Body post: Post): Post

    @PUT("posts/{id}")
    suspend fun updatePost(@Path("id") id: Int, @Body post: Post): Post

    @DELETE("posts/{id}")
    suspend fun deletePost(@Path("id") id: Int)

    @GET("comments")
    suspend fun getComments(): List<Comment>

    @GET("users")
    suspend fun getUsers(): List<User>

    @GET("todos")
    suspend fun getTodos(): List<Todo>

    companion object {
        fun create(): SampleApi {
            val client = OkHttpClient.Builder()
                .addInterceptor(Kulse.interceptor())
                .eventListenerFactory(Kulse.eventListenerFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl("https://jsonplaceholder.typicode.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SampleApi::class.java)
        }
    }
}

data class Post(
    val id: Int? = null,
    val userId: Int = 1,
    val title: String = "",
    val body: String = "",
)

data class Comment(
    val id: Int? = null,
    val postId: Int = 0,
    val name: String = "",
    val email: String = "",
    val body: String = "",
)

data class User(
    val id: Int? = null,
    val name: String = "",
    val username: String = "",
    val email: String = "",
)

data class Todo(
    val id: Int? = null,
    val userId: Int = 0,
    val title: String = "",
    val completed: Boolean = false,
)
