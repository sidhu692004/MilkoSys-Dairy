package com.sudhanshu.milkosysdairy

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class MilkEntryFragment : Fragment() {

    private lateinit var nameInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var shiftSpinner: Spinner
    private lateinit var typeSpinner: Spinner
    private lateinit var quantityInput: EditText
    private lateinit var fatInput: EditText
    private lateinit var snfInput: EditText
    private lateinit var rateText: TextView
    private lateinit var submitButton: Button
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_milk_entry, container, false)

        // Initialize views
        nameInput = view.findViewById(R.id.nameInput)
        phoneInput = view.findViewById(R.id.phoneInput)
        shiftSpinner = view.findViewById(R.id.shiftSpinner)
        typeSpinner = view.findViewById(R.id.typeSpinner)
        quantityInput = view.findViewById(R.id.quantityInput)
        fatInput = view.findViewById(R.id.fatInput)
        snfInput = view.findViewById(R.id.snfInput)
        rateText = view.findViewById(R.id.rateText)
        submitButton = view.findViewById(R.id.submitButton)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Set spinner options
        shiftSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Morning", "Evening")
        )

        typeSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Cow", "Buffalo", "Mix")
        )

        // Calculate rate when fat or snf changes
        val rateWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = calculateRate()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        fatInput.addTextChangedListener(rateWatcher)
        snfInput.addTextChangedListener(rateWatcher)

        submitButton.setOnClickListener {
            saveEntryToFirestore()
        }

        return view
    }

    private fun calculateRate() {
        val fat = fatInput.text.toString().toDoubleOrNull()
        val snf = snfInput.text.toString().toDoubleOrNull()

        if (fat != null && snf != null) {
            val rate = (fat * 8.0) + (snf * 4.0)
            rateText.text = "₹%.2f / litre".format(rate)
        } else {
            rateText.text = "₹0.0 / litre"
        }
    }

    private fun saveEntryToFirestore() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val name = nameInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()
        val shift = shiftSpinner.selectedItem.toString()
        val type = typeSpinner.selectedItem.toString()
        val quantity = quantityInput.text.toString().toDoubleOrNull()
        val fat = fatInput.text.toString().toDoubleOrNull()
        val snf = snfInput.text.toString().toDoubleOrNull()
        val rate = rateText.text.toString().replace("₹", "").replace("/ litre", "").trim().toDoubleOrNull()
        val date = getCurrentDate()

        if (name.isEmpty() || phone.isEmpty() || quantity == null || fat == null || snf == null || rate == null) {
            Toast.makeText(requireContext(), "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            return
        }

        val entry = mapOf(
            "uid" to uid, // store the logged-in user ID
            "name" to name,
            "phone" to phone,
            "shift" to shift,
            "type" to type,
            "quantity" to quantity,
            "fat" to fat,
            "snf" to snf,
            "rate" to rate,
            "date" to date,
            "timestamp" to getCurrentTimestamp()
        )

        // Query with uid + other matching fields
        firestore.collection("milk-collection")
            .whereEqualTo("uid", uid)
            .whereEqualTo("name", name)
            .whereEqualTo("phone", phone)
            .whereEqualTo("shift", shift)
            .whereEqualTo("type", type)
            .whereEqualTo("date", date)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val docId = documents.documents[0].id
                    firestore.collection("milk-collection").document(docId)
                        .update(entry)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Entry updated", Toast.LENGTH_SHORT).show()
                            clearFields()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show()
                            Log.e("FIRESTORE", "Update error", e)
                        }
                } else {
                    firestore.collection("milk-collection")
                        .add(entry)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "New entry added", Toast.LENGTH_SHORT).show()
                            clearFields()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Save failed", Toast.LENGTH_SHORT).show()
                            Log.e("FIRESTORE", "Add error", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Query failed", Toast.LENGTH_SHORT).show()
                Log.e("FIRESTORE", "Query error", e)
            }
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun clearFields() {
        nameInput.text.clear()
        phoneInput.text.clear()
        quantityInput.text.clear()
        fatInput.text.clear()
        snfInput.text.clear()
        rateText.text = "₹0.0 / litre"
        shiftSpinner.setSelection(0)
        typeSpinner.setSelection(0)
    }
}
