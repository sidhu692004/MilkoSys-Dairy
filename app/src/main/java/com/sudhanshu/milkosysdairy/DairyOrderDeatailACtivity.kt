package com.sudhanshu.milkosysdairy

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DairyOrderDetailActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvCustomer: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvMobile: TextView
    private lateinit var tvTotal: TextView
    private lateinit var tvPayment: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvTime: TextView
    private lateinit var adapter: DairyOrderDetailAdapter

    private val db = FirebaseFirestore.getInstance()
    private val dairyUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val itemList = mutableListOf<OrderItem>()
    private var orderId: String? = null
    private var customerUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dairy_order_detail)

        recyclerView = findViewById(R.id.recyclerViewOrderDetail)
        tvCustomer = findViewById(R.id.tvDetailCustomer)
        tvAddress = findViewById(R.id.tvDetailAddress)
        tvMobile = findViewById(R.id.tvDetailMobile)
        tvTotal = findViewById(R.id.tvDetailTotal)
        tvPayment = findViewById(R.id.tvDetailPayment)
        tvStatus = findViewById(R.id.tvDetailStatus)
        tvTime = findViewById(R.id.tvDetailTime)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = DairyOrderDetailAdapter(itemList)
        recyclerView.adapter = adapter

        orderId = intent.getStringExtra("orderId")
        customerUid = intent.getStringExtra("customerUid")

        fetchOrderDetail()
    }

    private fun fetchOrderDetail() {
        if (orderId.isNullOrEmpty() || customerUid.isNullOrEmpty()) return

        db.collection("users").document(customerUid!!)
            .collection("orders")
            .document(orderId!!)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val order = doc.toObject(DairyOrderModel::class.java)
                    order?.let {
                        tvCustomer.text = it.name
                        tvAddress.text = it.address
                        tvMobile.text = it.mobile
                        tvTotal.text = "₹${it.totalPrice}"
                        tvPayment.text = it.paymentMode
                        tvStatus.text = it.status
                        tvTime.text = it.timestamp?.toDate().toString()

                        itemList.clear()
                        // sirf current dairy ke items hi show hon
                        itemList.addAll(it.items.filter { item -> item.dairyUid == dairyUid })
                        adapter.notifyDataSetChanged()
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to fetch order detail", Toast.LENGTH_SHORT).show()
            }
    }
}
