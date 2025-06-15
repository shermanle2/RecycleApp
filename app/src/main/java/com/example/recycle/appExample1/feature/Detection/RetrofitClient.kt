import com.example.recycle.appExample1.feature.Detection.MyApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.recycle.BuildConfig

object RetrofitClient {
    private const val BASE_URL = BuildConfig.API_BASE_URL

    val api: MyApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MyApiService::class.java)
    }
}