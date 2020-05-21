package info.covid.essentials

import info.covid.database.enities.Resources
import info.covid.network.RetrofitClient

class EssentialsRepository {
    private val apiService = RetrofitClient.get()?.create(EssentialsApiService::class.java)

    suspend fun <T> getResources(success: (List<Resources>?) -> T, error: (String) -> T) {
        try {
            val resp = apiService?.getResources()
            if (resp != null) {
                if (resp.isSuccessful) {
                    success(resp.body()?.resources ?: emptyList())
                } else error("Wifi/Internet kuch toh chaalu kar le bhai")
            }
        } catch (e: Exception) {
            error("Paise barbaad iss phone mein bhench*d")
        }
    }
}