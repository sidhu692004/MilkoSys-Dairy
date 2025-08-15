package com.sudhanshu.milkosysdairy

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PickHeroFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DairyOrderAdapter
    private val orderList = mutableListOf<DairyOrderModel>()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val dairyUid = auth.currentUser?.uid ?: ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pick_hero, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewPickHero)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = DairyOrderAdapter(orderList) { selectedOrder ->
            showDeliveryBoyDialog(selectedOrder)
        }

        recyclerView.adapter = adapter

        fetchOrders()

        return view
    }

    // ✅ Sirf "Order Confirmed" wale orders fetch karna
    private fun fetchOrders() {
        if (dairyUid.isEmpty()) return

        db.collectionGroup("orders")
            .get()
            .addOnSuccessListener { qs ->
                orderList.clear()
                for (doc in qs) {
                    val order = doc.toObject(DairyOrderModel::class.java)
                    if (order.items.any { it.dairyUid == dairyUid } && order.status == "Order Confirmed") {
                        order.orderId = doc.id
                        orderList.add(order)
                    }
                }
                if (orderList.isEmpty()) {
                    Toast.makeText(requireContext(), "No confirmed orders found", Toast.LENGTH_SHORT).show()
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to fetch orders", Toast.LENGTH_SHORT).show()
            }
    }

    // ✅ Delivery boy list dikhane ka dialog
    private fun showDeliveryBoyDialog(order: DairyOrderModel) {
        db.collection("deliveryBoys")
            .whereEqualTo("dairyUid", dairyUid)
            .get()
            .addOnSuccessListener { qs ->
                if (qs.isEmpty) {
                    Toast.makeText(requireContext(), "No delivery boys registered!", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val deliveryBoys = qs.documents.map {
                    val id = it.getString("deliveryBoyId") ?: ""
                    val name = it.getString("name") ?: "N/A"
                    val mobile = it.getString("mobile") ?: "N/A"
                    DeliveryBoy(id, name, mobile)
                }

                val namesList = deliveryBoys.map { "${it.name} (${it.mobile})" }

                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, namesList)

                AlertDialog.Builder(requireContext())
                    .setTitle("Select Delivery Hero")
                    .setAdapter(adapter) { _: DialogInterface, which: Int ->
                        val selectedHero = deliveryBoys[which]
                        assignDeliveryBoy(order, selectedHero)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to fetch heroes", Toast.LENGTH_SHORT).show()
            }
    }

    // ✅ Delivery boy assign karna
    private fun assignDeliveryBoy(order: DairyOrderModel, hero: DeliveryBoy) {
        val orderRef = db.collection("users")
            .document(order.uid)
            .collection("orders")
            .document(order.orderId)

        val updates = mapOf(
            "deliveryBoyId" to hero.id,
            "deliveryBoyName" to hero.name,
            "deliveryBoyMobile" to hero.mobile,
            "status" to "Assigned Delivery Boy" // status change
        )

        orderRef.update(updates)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Assigned to ${hero.name}", Toast.LENGTH_SHORT).show()
                fetchOrders() // list refresh
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to assign hero", Toast.LENGTH_SHORT).show()
            }
    }
}

// ✅ Helper model for delivery boy
data class DeliveryBoy(
    val id: String,
    val name: String,
    val mobile: String
)
