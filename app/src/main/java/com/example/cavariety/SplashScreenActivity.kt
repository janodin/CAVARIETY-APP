package com.example.cavariety

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val stressIcon = findViewById<ImageView>(R.id.cacao_bg)
        stressIcon.alpha = 0f
        stressIcon.animate().setDuration(3000).alpha(1f).withEndAction {
            val s = Intent(this, MainActivity::class.java)
            startActivity(s)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }
}