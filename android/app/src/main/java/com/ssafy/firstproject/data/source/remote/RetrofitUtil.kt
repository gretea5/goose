package com.ssafy.firstproject.data.source.remote

import com.ssafy.firstproject.base.ApplicationClass

class RetrofitUtil {
    companion object {
        val userService: UserService = ApplicationClass.retrofit.create(UserService::class.java)
        val newsService: NewsService = ApplicationClass.retrofit.create(NewsService::class.java)
        val contentSearchService: ContentSearchService = ApplicationClass.checkRetrofit.create(ContentSearchService::class.java)
        val userNewsService: UserNewsService = ApplicationClass.retrofit.create(UserNewsService::class.java)
        val gameService: GameService = ApplicationClass.retrofit.create(GameService::class.java)
        val spellCheckService: SpellCheckService = ApplicationClass.spellCheckRetrofit.create(SpellCheckService::class.java)
        val userGrowthService: UserGrowthService = ApplicationClass.retrofit.create(UserGrowthService::class.java)
    }
}