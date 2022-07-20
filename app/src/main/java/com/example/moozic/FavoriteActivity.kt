package com.example.moozic

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.moozic.databinding.ActivityFavoriteBinding

class FavoriteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavoriteBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.coolPink)
        binding= ActivityFavoriteBinding.inflate(layoutInflater)

        setContentView(binding.root)
        binding.backBtnFA.setOnClickListener{finish()}
    }
}