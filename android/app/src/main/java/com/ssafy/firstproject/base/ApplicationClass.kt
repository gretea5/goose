package com.ssafy.firstproject.base

import android.app.Application
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ssafy.firstproject.data.repository.ContentSearchRepository
import com.ssafy.firstproject.data.repository.GameRepository
import com.ssafy.firstproject.data.source.local.SharedPreferencesUtil
import com.ssafy.firstproject.data.repository.NewsRepository
import com.ssafy.firstproject.data.repository.UserNewsRepository
import com.ssafy.firstproject.data.repository.UserRepository
import com.ssafy.firstproject.data.repository.SpellCheckRepository
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

private const val TAG = "ApplicationClass"

class ApplicationClass : Application() {
    companion object {
        // 핸드폰으로 접속은 같은 인터넷으로 연결 되어있어야함 (유,무선)
        const val SERVER_URL = "http://i12d208.p.ssafy.io:8090/api/"
        const val SPELL_CHECK_SERVER_URL = "http://i12d208.p.ssafy.io:5060/"
        lateinit var sharedPreferencesUtil: SharedPreferencesUtil
        lateinit var retrofit: Retrofit
        lateinit var spellCheckRetrofit: Retrofit
        lateinit var checkRetrofit: Retrofit

        val nullOnEmptyConverterFactory = object : Converter.Factory() {
            fun converterFactory() = this
            override fun responseBodyConverter(
                type: Type,
                annotations: Array<out Annotation>,
                retrofit: Retrofit
            ) = object :
                Converter<ResponseBody, Any?> {
                val nextResponseBodyConverter =
                    retrofit.nextResponseBodyConverter<Any?>(converterFactory(), type, annotations)

                override fun convert(value: ResponseBody) = if (value.contentLength() != 0L) {
                    try {
                        nextResponseBodyConverter.convert(value)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                } else {
                    null
                }
            }
        }

        //GSon은 엄격한 json type을 요구하는데, 느슨하게 하기 위한 설정. success, fail이 json이 아니라 단순 문자열로 리턴될 경우 처리..
        val gson: Gson = GsonBuilder()
            .setLenient()
            .create()

        // repository 객체
        lateinit var userRepository: UserRepository
        lateinit var newsRepository: NewsRepository
        lateinit var contentSearchRepository: ContentSearchRepository
        lateinit var userNewsRepository: UserNewsRepository
        lateinit var spellCheckRepository: SpellCheckRepository
        lateinit var gameRepository: GameRepository
    }

    override fun onCreate() {
        super.onCreate()

        //shared preference 초기화
        sharedPreferencesUtil = SharedPreferencesUtil(applicationContext)

        // 레트로핏 인스턴스를 생성하고, 레트로핏에 각종 설정값들을 지정해줍니다.
        // 연결 타임아웃시간은 5초로 지정이 되어있고, HttpLoggingInterceptor를 붙여서 어떤 요청이 나가고 들어오는지를 보여줍니다.
        val okHttpClient = OkHttpClient.Builder()
            .readTimeout(10000, TimeUnit.MILLISECONDS)
            .connectTimeout(5000, TimeUnit.MILLISECONDS)
            .addInterceptor(AuthInterceptor())
            .authenticator(TokenAuthenticator())
            // 로그캣에 okhttp.OkHttpClient로 검색하면 http 통신 내용을 보여줍니다.
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()

        // 앱이 처음 생성되는 순간, retrofit 인스턴스를 생성
        retrofit = Retrofit.Builder()
            .baseUrl(SERVER_URL)
            .addConverterFactory(nullOnEmptyConverterFactory)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()

        spellCheckRetrofit = Retrofit.Builder()
            .baseUrl(SPELL_CHECK_SERVER_URL)
            .addConverterFactory(nullOnEmptyConverterFactory)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()

        val checkOkHttpClient = OkHttpClient.Builder()
            .readTimeout(60000, TimeUnit.MILLISECONDS)
            .connectTimeout(5000, TimeUnit.MILLISECONDS)
            .addInterceptor(AuthInterceptor())
            .authenticator(TokenAuthenticator())
            // 로그캣에 okhttp.OkHttpClient로 검색하면 http 통신 내용을 보여줍니다.
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()

        checkRetrofit = Retrofit.Builder()
            .baseUrl(SERVER_URL)
            .addConverterFactory(nullOnEmptyConverterFactory)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(checkOkHttpClient)
            .build()

        // repository 초기화
        userRepository = UserRepository()
        newsRepository = NewsRepository()
        contentSearchRepository = ContentSearchRepository()
        userNewsRepository = UserNewsRepository()
        spellCheckRepository = SpellCheckRepository()
        gameRepository = GameRepository()
    }
}