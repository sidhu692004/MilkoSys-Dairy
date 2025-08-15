package com.sudhanshu.milkosysdairy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class ProductModel(
    val productName: String = "",
    val quantity: Double = 0.0,
    val rate: Double = 0.0,
    val date: String = ""
)

class ProductListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyText: TextView
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val productList = mutableListOf<ProductModel>()
    private lateinit var adapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_product_list, container, false)

        recyclerView = view.findViewById(R.id.recyclerProducts)
        progressBar = view.findViewById(R.id.progressBarProducts)
        emptyText = view.findViewById(R.id.tvEmptyProducts)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ProductAdapter(productList)
        recyclerView.adapter = adapter

        loadProducts()

        return view
    }

    private fun loadProducts() {
        val uid = auth.currentUser?.uid ?: return
        progressBar.visibility = View.VISIBLE
        firestore.collection("users")
            .document(uid)
            .collection("DairyProducts")
            .get()
            .addOnSuccessListener { documents ->
                progressBar.visibility = View.GONE
                productList.clear()
                for (doc in documents) {
                    val product = ProductModel(
                        doc.getString("productName") ?: "",
                        doc.getDouble("quantity") ?: 0.0,
                        doc.getDouble("rate") ?: 0.0,
                        doc.getString("date") ?: ""
                    )
                    productList.add(product)
                }
                if (productList.isEmpty()) {
                    emptyText.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    emptyText.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    adapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Failed to load products", Toast.LENGTH_SHORT).show()
            }
    }
}
