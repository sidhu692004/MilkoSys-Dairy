package com.sudhanshu.milkosysdairy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ShowActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show)

        // Agar savedInstanceState null hai toh fragment load karo
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProductListFragment())
                .commit()
        }
    }
}
