package com.sudhanshu.milkosysdairy

import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.sudhanshu.milkosysdairy.databinding.FragmentBillingBinding
import java.io.File
import java.io.FileOutputStream

class BillingFragment : Fragment() {

    private var _binding: FragmentBillingBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val orderList = mutableListOf<DairyOrderModel>()
    private lateinit var adapter: BillingAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBillingBinding.inflate(inflater, container, false)

        binding.recyclerViewBilling.layoutManager = LinearLayoutManager(requireContext())
        adapter = BillingAdapter(orderList)
        binding.recyclerViewBilling.adapter = adapter

        fetchOrders()

        binding.btnShareBill.setOnClickListener { shareBill() }
        binding.btnPrintBill.setOnClickListener { printBill() }

        return binding.root
    }

    private fun fetchOrders() {
        db.collectionGroup("orders")
            .whereEqualTo("status", "Assigned Delivery Boy") // Only completed/assigned orders
            .get()
            .addOnSuccessListener { qs ->
                orderList.clear()
                for (doc in qs) {
                    val order = doc.toObject(DairyOrderModel::class.java)
                    order.orderId = doc.id
                    orderList.add(order)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to fetch orders", Toast.LENGTH_SHORT).show()
            }
    }

    private fun generateBillText(): String {
        if (orderList.isEmpty()) return "No orders to show"

        val sb = StringBuilder()
        sb.append("===== MilkoSys Bill =====\n\n")
        var total = 0.0

        for (order in orderList) {
            sb.append("Order ID: ${order.orderId}\n")
            order.items.forEach { item ->
                val itemTotal = item.quantity * item.price
                sb.append(" ${item.quantity} = ₹$itemTotal\n")
                total += itemTotal
            }
            sb.append("Delivery Boy: ${order.deliveryBoyName}\n")
            sb.append("--------------------------\n")
        }

        sb.append("Total Amount: ₹$total\n")
        sb.append("==========================\n")
        return sb.toString()
    }

    private fun shareBill() {
        val billText = generateBillText()
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, billText)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Share Bill via"))
    }

    private fun printBill() {
        val billText = generateBillText()

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val paint = android.graphics.Paint()
        paint.textSize = 12f
        val lines = billText.split("\n")
        var y = 25
        for (line in lines) {
            canvas.drawText(line, 10f, y.toFloat(), paint)
            y += 20
        }
        document.finishPage(page)

        try {
            val file = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "MilkoSys_Bill.pdf")
            document.writeTo(FileOutputStream(file))
            document.close()

            val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", file)
            val printIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            startActivity(printIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed to generate bill PDF", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
