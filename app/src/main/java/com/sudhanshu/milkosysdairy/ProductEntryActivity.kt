package com.sudhanshu.milkosysdairy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit

class ProductEntryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_entry)

        // Fragment load करना
        supportFragmentManager.commit {
            replace(R.id.productFragmentContainer, ProductEntryFragment())
        }
    }
}
