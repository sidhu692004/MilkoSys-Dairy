package com.sudhanshu.milkosysdairy

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DairyOrdersFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DairyOrderAdapter
    private val orderList = mutableListOf<DairyOrderModel>()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val dairyUid = auth.currentUser?.uid ?: ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dairy_orders, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewDairyOrders)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = DairyOrderAdapter(orderList) { selectedOrder ->
            val intent = Intent(requireContext(), DairyOrderDetailActivity::class.java)
            intent.putExtra("orderId", selectedOrder.orderId)
            intent.putExtra("customerUid", selectedOrder.uid)
            startActivity(intent)
        }

        recyclerView.adapter = adapter

        fetchOrders()

        return view
    }

    private fun fetchOrders() {
        if (dairyUid.isEmpty()) return

        db.collectionGroup("orders")
            .get()
            .addOnSuccessListener { qs ->
                orderList.clear()
                for (doc in qs) {
                    val order = doc.toObject(DairyOrderModel::class.java)
                    // filter: sirf wahi order jisme is dairy ke products hain
                    if (order.items.any { it.dairyUid == dairyUid }) {
                        order.orderId = doc.id
                        orderList.add(order)
                    }
                }
                if (orderList.isEmpty()) {
                    Toast.makeText(requireContext(), "No orders found", Toast.LENGTH_SHORT).show()
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to fetch orders", Toast.LENGTH_SHORT).show()
            }
    }
}
