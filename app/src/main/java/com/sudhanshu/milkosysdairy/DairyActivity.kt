package com.sudhanshu.milkosysdairy

import android.os.Bundle

//impoirt android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.sudhanshu.milkosysdairy.AdminHomeFragment

class DairyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dairy)

        // Fragment सिर्फ पहली बार add करें
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.admin_fragment_container, AdminHomeFragment())
            }
        }
    }
}
