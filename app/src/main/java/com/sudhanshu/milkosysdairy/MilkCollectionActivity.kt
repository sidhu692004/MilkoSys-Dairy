package com.sudhanshu.milkosysdairy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MilkCollectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_milk_collection)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, MilkEntryFragment())
            .commit()
    }
}
