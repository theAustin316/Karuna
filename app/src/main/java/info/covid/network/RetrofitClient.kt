package info.covid.network

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


object RetrofitClient {

   const val BASE_URL = "https://api.covid19india.org"
    
    fun get(baseUrl : String?= BASE_URL) = baseUrl?.let {
        Retrofit.Builder()
        .baseUrl(it)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
    }
}


