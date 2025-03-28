package info.covid.state

import info.covid.models.DistrictState
import info.covid.models.StateDailyResponse
import retrofit2.Response
import retrofit2.http.GET

interface StateAPIService {
    @GET("/v2/state_district_wise.json")
    suspend fun getStateDistrictWise(): Response<List<DistrictState>>

    @GET("/states_daily.json")
    suspend fun getStateDaily(): Response<StateDailyResponse>

}