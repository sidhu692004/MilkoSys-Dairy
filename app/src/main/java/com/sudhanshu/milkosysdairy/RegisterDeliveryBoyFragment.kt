package com.sudhanshu.milkosysdairy

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class RegisterDeliveryBoyFragment : Fragment() {

    private lateinit var etMobile: EditText
    private lateinit var etName: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var btnFetch: Button
    private lateinit var tvResult: TextView
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register_delivery_boy, container, false)

        etMobile = view.findViewById(R.id.etMobile)
        etName = view.findViewById(R.id.etName)
        etPassword = view.findViewById(R.id.etPassword)
        btnRegister = view.findViewById(R.id.btnRegister)
        btnFetch = view.findViewById(R.id.btnFetch)
        tvResult = view.findViewById(R.id.tvResult)

        btnRegister.setOnClickListener { registerDeliveryBoy() }
        btnFetch.setOnClickListener { fetchDeliveryBoy() }

        return view
    }

    private fun registerDeliveryBoy() {
        val name = etName.text.toString().trim()
        val mobile = etMobile.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val dairyUid = FirebaseAuth.getInstance().currentUser?.uid // current dairy UID

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(mobile) || TextUtils.isEmpty(password) || dairyUid == null) {
            Toast.makeText(requireContext(), "All fields are required!", Toast.LENGTH_SHORT).show()
            return
        }

        // ✅ Pehle check karein ki mobile already exist hai ya nahi
        db.collection("deliveryBoys")
            .whereEqualTo("mobile", mobile)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // Agar record already hai
                    Toast.makeText(requireContext(), "Hero already existed!", Toast.LENGTH_SHORT).show()
                } else {
                    // Agar record nahi hai to naya add karo
                    val deliveryBoyId = UUID.randomUUID().toString()

                    val deliveryBoy = hashMapOf(
                        "deliveryBoyId" to deliveryBoyId,
                        "name" to name,
                        "mobile" to mobile,
                        "password" to password,
                        "dairyUid" to dairyUid
                    )

                    db.collection("deliveryBoys")
                        .document(deliveryBoyId)
                        .set(deliveryBoy)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Delivery Boy Registered!", Toast.LENGTH_SHORT).show()
                            etName.text.clear()
                            etMobile.text.clear()
                            etPassword.text.clear()
                            tvResult.text = ""
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun fetchDeliveryBoy() {
        val mobile = etMobile.text.toString().trim()

        if (TextUtils.isEmpty(mobile)) {
            Toast.makeText(requireContext(), "Enter mobile number to fetch!", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("deliveryBoys")
            .whereEqualTo("mobile", mobile)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (doc in documents) {
                        val foundMobile = doc.getString("mobile") ?: "N/A"
                        val foundPassword = doc.getString("password") ?: "N/A"
                        tvResult.text = "Mobile: $foundMobile\nPassword: $foundPassword"
                    }
                } else {
                    tvResult.text = "No Delivery Boy found with this number!"
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
