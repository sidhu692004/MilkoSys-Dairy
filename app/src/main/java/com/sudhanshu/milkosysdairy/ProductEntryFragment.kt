package com.sudhanshu.milkosysdairy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ProductEntryFragment : Fragment() {

    private lateinit var productSpinner: Spinner
    private lateinit var qtyInput: EditText
    private lateinit var rateInput: EditText
    private lateinit var saveBtn: Button

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val productList = listOf(
        "Milk",
        "Paneer (पनीर)",
        "Ghee (घी)",
        "Curd (दही)",
        "Lassi / Butter Milk"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.product_entry, container, false)

        productSpinner = view.findViewById(R.id.productSpinner)
        qtyInput = view.findViewById(R.id.productQty)
        rateInput = view.findViewById(R.id.productRate)
        saveBtn = view.findViewById(R.id.saveProductBtn)

        // Setup Spinner
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, productList)
        productSpinner.adapter = adapter

        saveBtn.setOnClickListener {
            saveProductToFirestore()
        }

        return view
    }

    private fun saveProductToFirestore() {
        val selectedProduct = productSpinner.selectedItem.toString()
        val quantity = qtyInput.text.toString().trim()
        val rate = rateInput.text.toString().trim()

        if (quantity.isEmpty() || rate.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val collectionRef = firestore.collection("users")
            .document(uid)
            .collection("DairyProducts")

        // Step 1: Check if product with same name and date exists for this user
        collectionRef
            .whereEqualTo("productName", selectedProduct)
            .whereEqualTo("date", currentDate)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // Step 2: If found, update existing document
                    val docId = documents.documents[0].id
                    collectionRef.document(docId)
                        .update(
                            mapOf(
                                "quantity" to quantity.toDouble(),
                                "rate" to rate.toDouble()
                            )
                        )
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Product updated successfully", Toast.LENGTH_SHORT).show()
                            qtyInput.text.clear()
                            rateInput.text.clear()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Failed to update product", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // Step 3: If not found, add new document
                    val productData = hashMapOf(
                        "productName" to selectedProduct,
                        "quantity" to quantity.toDouble(),
                        "rate" to rate.toDouble(),
                        "date" to currentDate
                    )

                    collectionRef
                        .add(productData)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Product saved successfully", Toast.LENGTH_SHORT).show()
                            qtyInput.text.clear()
                            rateInput.text.clear()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Failed to save product", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error checking product", Toast.LENGTH_SHORT).show()
            }
    }
}
