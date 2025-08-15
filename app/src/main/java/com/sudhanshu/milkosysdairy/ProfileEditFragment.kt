package com.sudhanshu.milkosysdairy

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class ProfileEditFragment : Fragment() {

    private lateinit var etFullName: EditText
    private lateinit var etDairyName: EditText
    private lateinit var etContactNumber: EditText
    private lateinit var etEmail: EditText
    private lateinit var etAddress: EditText
    private lateinit var etPinCode: EditText
    private lateinit var btnSaveProfile: Button
    private lateinit var btnUploadImage: Button
    private lateinit var ivProfileImage: ImageView
    private lateinit var progressBar: ProgressBar

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private var imageUri: Uri? = null
    private var uploadedImageUrl: String? = null

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
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile)
        btnUploadImage = view.findViewById(R.id.btnUploadImage)
        ivProfileImage = view.findViewById(R.id.ivProfileImage)
        progressBar = view.findViewById(R.id.progressBarProfile)

        loadProfileData()

        btnUploadImage.setOnClickListener {
            ImagePicker.with(this)
                .cropSquare()      // crop karega
                .compress(512)     // compress karega
                .maxResultSize(512, 512)
                .start()
        }

        btnSaveProfile.setOnClickListener {
            if (imageUri != null) {
                uploadImageAndSaveProfile()
            } else {
                saveProfile()
            }
        }

        return view
    }

    // 🔹 Firestore se data load karo
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

                    uploadedImageUrl = document.getString("imageUrl")
                    uploadedImageUrl?.let {
                        Glide.with(this).load(it).into(ivProfileImage)
                    }
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // 🔹 Image picker result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            ivProfileImage.setImageURI(imageUri)
        }
    }

    // 🔹 Pehle Storage me upload karo
    private fun uploadImageAndSaveProfile() {
        val userId = auth.currentUser?.uid ?: return
        val ref = storage.reference.child("profile_images/$userId/${UUID.randomUUID()}.jpg")

        progressBar.visibility = View.VISIBLE

        ref.putFile(imageUri!!)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    uploadedImageUrl = uri.toString()
                    saveProfile()
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Image upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // 🔹 Profile Firestore me save karo
    private fun saveProfile() {
        val userId = auth.currentUser?.uid ?: return

        val profileData = hashMapOf(
            "uid" to userId,
            "fullName" to etFullName.text.toString().trim(),
            "dairyName" to etDairyName.text.toString().trim(),
            "contactNumber" to etContactNumber.text.toString().trim(),
            "email" to etEmail.text.toString().trim(),
            "address" to etAddress.text.toString().trim(),
            "pinCode" to etPinCode.text.toString().trim(),
            "imageUrl" to (uploadedImageUrl ?: "")
        )

        firestore.collection("dairyProfiles")
            .document(userId)
            .set(profileData)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
