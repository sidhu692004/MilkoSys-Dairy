package com.sudhanshu.milkosysdairy

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            // User is already logged in
            startActivity(Intent(this, DairyActivity::class.java))
        } else {
            // User is not logged in
            startActivity(Intent(this, MainActivity::class.java))
        }

        // Finish this activity so user can't return here
        finish()
    }
}
