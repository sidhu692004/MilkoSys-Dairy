package com.sudhanshu.milkosysdairy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class BillActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bill)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, BillFragment())
            .commit()
    }
}
