package com.example.abobusteam

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.IOException
import okhttp3.Response as ResponseOkHTTP

class APIKeyProvider : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): ResponseOkHTTP {
        val builder = chain.request().newBuilder()
        builder.addHeader("x-api-key", "f6c7361a8152484fa8902c747b577387")
        return chain.proceed(builder.build())
    }
}

interface SpoonacularAPI {
    @GET("recipes/complexSearch")
    suspend fun getRecipes(@Query("offset") offset: Int = 0,
                           @Query("query") query: String = "",
                           @Query("diet") diet: Recipe.Diet = Recipe.Diet.Default,
                           @Query("maxReadyTime") maxReadyTime: Int = 20) : RecipeListResponse

    @GET("recipes/{id}/information")
    suspend fun getRecipe(@Path(value = "id", encoded = true) id: Int) : RecipeResponse
}

class Request {
    private val client = OkHttpClient.Builder().addNetworkInterceptor(APIKeyProvider()).build()
    private val retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("https://api.spoonacular.com")
        .client(client)
        .build()
        .create(SpoonacularAPI::class.java)

    suspend fun getRecipes(offset: Int = 0,
                           query: String = "",
                           diet : Recipe.Diet = Recipe.Diet.Default,
                           maxReadyTime: Int = 20): List<RecipeListItem>
    {
        return retrofit.getRecipes(offset, query, diet, maxReadyTime).results
    }

    suspend fun getRecipe(id: Int = 0) : Recipe {
        val response = retrofit.getRecipe(id);
        return Recipe(
            response.id,
            response.title,
            response.image,
            response.summary,
            response.readyInMinutes,
            response.instructions,
            response.analyzedInstructions[0].steps
        );
    }
}