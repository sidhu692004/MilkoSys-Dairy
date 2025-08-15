package com.sudhanshu.milkosysdairy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var tvFullName: TextView
    private lateinit var tvDairyName: TextView
    private lateinit var tvContactNumber: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvPinCode: TextView
    private lateinit var tvUID: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var cardProfile: CardView

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        tvFullName = view.findViewById(R.id.tvFullName)
        tvDairyName = view.findViewById(R.id.tvDairyName)
        tvContactNumber = view.findViewById(R.id.tvContactNumber)
        tvEmail = view.findViewById(R.id.tvEmail)
        tvAddress = view.findViewById(R.id.tvAddress)
        tvPinCode = view.findViewById(R.id.tvPinCode)
        tvUID = view.findViewById(R.id.tvUID)
        progressBar = view.findViewById(R.id.progressBarProfile)
        cardProfile = view.findViewById(R.id.cardProfile)

        loadProfile()

        return view
    }

    private fun loadProfile() {
        val userId = auth.currentUser?.uid ?: return

        progressBar.visibility = View.VISIBLE
        cardProfile.visibility = View.GONE

        firestore.collection("dairyProfiles")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                progressBar.visibility = View.GONE
                if (document != null && document.exists()) {
                    tvFullName.text = document.getString("fullName") ?: "-"
                    tvDairyName.text = document.getString("dairyName") ?: "-"
                    tvContactNumber.text = document.getString("contactNumber") ?: "-"
                    tvEmail.text = document.getString("email") ?: "-"
                    tvAddress.text = document.getString("address") ?: "-"
                    tvPinCode.text = document.getString("pinCode") ?: "-"
                    tvUID.text = document.getString("uid") ?: userId

                    cardProfile.visibility = View.VISIBLE
                } else {
                    Toast.makeText(requireContext(), "Profile not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
