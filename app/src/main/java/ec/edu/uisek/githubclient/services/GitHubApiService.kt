package ec.edu.uisek.githubclient.services

import ec.edu.uisek.githubclient.models.Repo
import retrofit2.Call
import retrofit2.http.*

interface GitHubApiService {

    @GET("/user/repos")
    fun getRepos(): Call<List<Repo>>

    @POST("/user/repos")
    fun createRepo(
        @Body repoData: Map<String, String>
    ): Call<Repo>

    @PATCH("/repos/{owner}/{repo}")
    fun updateRepo(
        @Path("owner") owner: String,
        @Path("repo") repoName: String,
        @Body repoUpdate: Map<String, String>
    ): Call<Repo>

    @DELETE("/repos/{owner}/{repo}")
    fun deleteRepo(
        @Path("owner") owner: String,
        @Path("repo") repoName: String
    ): Call<Void>

}