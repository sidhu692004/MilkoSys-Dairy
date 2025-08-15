package com.sudhanshu.milkosysdairy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileEditFragment : Fragment() {

    private lateinit var etFullName: EditText
    private lateinit var etDairyName: EditText
    private lateinit var etContactNumber: EditText
    private lateinit var etEmail: EditText
    private lateinit var etAddress: EditText
    private lateinit var etPinCode: EditText
    private lateinit var etBankAccount: EditText
    private lateinit var btnSaveProfile: Button
    private lateinit var progressBar: ProgressBar

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_edit, container, false)

        etFullName = view.findViewById(R.id.etFullName)
        etDairyName = view.findViewById(R.id.etDairyName)
        etContactNumber = view.findViewById(R.id.etContactNumber)
        etEmail = view.findViewById(R.id.etEmail)
        etAddress = view.findViewById(R.id.etAddress)
        etPinCode = view.findViewById(R.id.etPinCode)
//        etBankAccount = view.findViewById(R.id.etBankAccount)
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile)
        progressBar = view.findViewById(R.id.progressBarProfile)

        loadProfileData()

        btnSaveProfile.setOnClickListener {
            saveProfile()
        }

        return view
    }

    private fun loadProfileData() {
        val userId = auth.currentUser?.uid ?: return

        progressBar.visibility = View.VISIBLE

        firestore.collection("dairyProfiles")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                progressBar.visibility = View.GONE
                if (document != null && document.exists()) {
                    etFullName.setText(document.getString("fullName") ?: "")
                    etDairyName.setText(document.getString("dairyName") ?: "")
                    etContactNumber.setText(document.getString("contactNumber") ?: "")
                    etEmail.setText(document.getString("email") ?: "")
                    etAddress.setText(document.getString("address") ?: "")
                    etPinCode.setText(document.getString("pinCode") ?: "")
//                    etBankAccount.setText(document.getString("bankAccount") ?: "")
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Failed to load profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveProfile() {
        val fullName = etFullName.text.toString().trim()
        val dairyName = etDairyName.text.toString().trim()
        val contactNumber = etContactNumber.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val address = etAddress.text.toString().trim()
        val pinCode = etPinCode.text.toString().trim()
//        val bankAccount = etBankAccount.text.toString().trim()

        if (fullName.isEmpty() || dairyName.isEmpty() || contactNumber.isEmpty() || email.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }
        if (pinCode.isNotEmpty() && pinCode.length != 6) {
            Toast.makeText(requireContext(), "Please enter valid 6-digit Pin Code", Toast.LENGTH_SHORT).show()
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(requireContext(), "Please enter valid Email", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid ?: return

        val profileData = hashMapOf(
            "fullName" to fullName,
            "dairyName" to dairyName,
            "contactNumber" to contactNumber,
            "email" to email,
            "address" to address,
            "pinCode" to pinCode,
//            "bankAccount" to bankAccount
        )

        progressBar.visibility = View.VISIBLE

        firestore.collection("dairyProfiles")
            .document(userId)
            .set(profileData)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
