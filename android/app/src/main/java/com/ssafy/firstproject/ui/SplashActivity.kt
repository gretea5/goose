package com.ssafy.firstproject.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.ssafy.firstproject.base.BaseActivity
import com.ssafy.firstproject.databinding.ActivitySplashBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>(ActivitySplashBinding::inflate) {

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)

        // AnimationDrawable로 캐스팅
        val animationDrawable = binding.ivLoading.drawable as AnimationDrawable

        // 애니메이션 시작
        animationDrawable.start()

        // 일정 시간 후 MainActivity로 이동
        lifecycleScope.launch {
            delay(2000) // 2초 후 전환
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }
    }
}
