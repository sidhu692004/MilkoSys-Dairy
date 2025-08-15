package com.sudhanshu.milkosysdairy


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sudhanshu.milkosysdairy.databinding.ActivityOrdersBinding

class OrderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrdersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .replace(R.id.ordersContainer, DairyOrdersFragment())
            .commit()
    }
}
