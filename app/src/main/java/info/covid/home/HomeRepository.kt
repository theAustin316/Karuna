package info.covid.home

import info.covid.models.CovidResponse
import info.covid.network.RetrofitClient

class HomeRepository {

    private val apiService = RetrofitClient.get()?.create(CovidApiService::class.java)

    suspend fun <T> getData(success: (CovidResponse?) -> T, error: (String) -> T) {
        try {
            val resp = apiService?.getData()
            if (resp != null) {
                if (resp.isSuccessful){
                    success(resp.body())
                } else error("Wifi/Internet kuch toh chaalu kar bhai")
            }
        } catch (e: Exception) {
            error("Paisa barbaad iss phone mein bhench*d")
        }
    }
}