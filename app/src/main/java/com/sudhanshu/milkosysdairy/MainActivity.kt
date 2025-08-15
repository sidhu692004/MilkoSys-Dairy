package com.sudhanshu.milkosysdairy

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginBtn: Button
    private lateinit var signupBtn: Button
    private lateinit var forgotPasswordText: TextView
    private lateinit var googleSignInBtn: Button

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Views
        emailEditText = findViewById(R.id.etEmail)
        passwordEditText = findViewById(R.id.etPassword)
        loginBtn = findViewById(R.id.btnLogin)
        signupBtn = findViewById(R.id.btnSignup)
        forgotPasswordText = findViewById(R.id.ftPassword)
        googleSignInBtn = findViewById(R.id.loginWithGoogle)

        // Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        // Google Sign-In setup
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Google sign-in button
        googleSignInBtn.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        // Email/password login
        loginBtn.setOnClickListener {
            loginUser()
        }

        // Signup
        signupBtn.setOnClickListener {
            startActivity(Intent(this, signUpActivity::class.java))
            finish()
        }

        // Forgot password
        forgotPasswordText.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
            finish()
        }
    }

    // ---------------- EMAIL/PASSWORD LOGIN ----------------
    private fun loginUser() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val user = firebaseAuth.currentUser
                if (user != null) checkUserRole(user)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Login failed: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    // ---------------- GOOGLE LOGIN ----------------
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    if (user != null) checkAndSetUserRole(user)
                } else {
                    Toast.makeText(this, "Firebase Auth failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // ---------------- ROLE CHECKS ----------------
    private fun checkAndSetUserRole(user: FirebaseUser) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("users").child(user.uid)

        databaseRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (!task.result.exists()) {
                    // New Google user -> set default role = dairy
                    val userMap = mapOf(
                        "fullName" to (user.displayName ?: ""),
                        "email" to (user.email ?: ""),
                        "role" to "dairy"
                    )

                    databaseRef.setValue(userMap).addOnCompleteListener { saveTask ->
                        if (saveTask.isSuccessful) {
                            goToDairyActivity()
                        } else {
                            Toast.makeText(this, "Failed to save role: ${saveTask.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    validateRoleAndProceed(task.result.child("role").value?.toString())
                }
            } else {
                Toast.makeText(this, "Somethings Else", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkUserRole(user: FirebaseUser) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("users").child(user.uid)
        databaseRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                validateRoleAndProceed(task.result.child("role").value?.toString())
            } else {
                Toast.makeText(this, "Error checking role: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateRoleAndProceed(role: String?) {
        if (role == "dairy") {
            goToDairyActivity()
        } else {
            firebaseAuth.signOut()
            Toast.makeText(this, "Access denied: you are not dairy user", Toast.LENGTH_SHORT).show()
        }
    }

    // ---------------- NAVIGATION ----------------
    private fun goToDairyActivity() {
        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, DairyActivity::class.java))
        finish()
    }
}
