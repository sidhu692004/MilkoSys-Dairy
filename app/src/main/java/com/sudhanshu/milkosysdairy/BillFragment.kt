package com.sudhanshu.milkosysdairy

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.*
import android.widget.*
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class BillFragment : Fragment() {

    private lateinit var billLayout: LinearLayout
    private lateinit var paymentModeGroup: RadioGroup
    private lateinit var radioCash: RadioButton
    private lateinit var radioOnline: RadioButton
    private lateinit var generateBillBtn: Button
    private lateinit var saveBtn: Button
    private lateinit var shareBtn: Button

    private lateinit var tvDairyName: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvName: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvShift: TextView
    private lateinit var tvType: TextView
    private lateinit var tvQty: TextView
    private lateinit var tvRate: TextView
    private lateinit var tvAmt: TextView
    private lateinit var tvPaymentMode: TextView
    private lateinit var tvTotal: TextView

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var currentEntry: MilkEntry? = null
    private var currentTotal: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_bill, container, false)

        billLayout = view.findViewById(R.id.billLayout)
        paymentModeGroup = view.findViewById(R.id.paymentModeGroup)
        radioCash = view.findViewById(R.id.radioCash)
        radioOnline = view.findViewById(R.id.radioOnline)
        generateBillBtn = view.findViewById(R.id.generateBillBtn)
        saveBtn = view.findViewById(R.id.saveBtn)
        shareBtn = view.findViewById(R.id.shareBtn)

        tvDairyName = view.findViewById(R.id.tvDairyName)
        tvDate = view.findViewById(R.id.tvDate)
        tvName = view.findViewById(R.id.tvName)
        tvPhone = view.findViewById(R.id.tvPhone)
        tvShift = view.findViewById(R.id.tvShift)
        tvType = view.findViewById(R.id.tvType)
        tvQty = view.findViewById(R.id.tvQty)
        tvRate = view.findViewById(R.id.tvRate)
        tvAmt = view.findViewById(R.id.tvAmt)
        tvPaymentMode = view.findViewById(R.id.tvPaymentMode)
        tvTotal = view.findViewById(R.id.tvTotal)

        generateBillBtn.setOnClickListener {
            fetchAndShowBill()
        }

        paymentModeGroup.setOnCheckedChangeListener { _, checkedId ->
            if (currentEntry != null) {
                val paymentMode = if (checkedId == R.id.radioCash) "Cash" else "Online"
                tvPaymentMode.text = "Payment Mode: $paymentMode"
            }
        }

        saveBtn.setOnClickListener {
            saveBillAsImage()
        }

        shareBtn.setOnClickListener {
            shareBillAsImage()
        }

        return view
    }

    private fun fetchAndShowBill() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("milk-collection")
            .whereEqualTo("date", today)
            .whereEqualTo("uid", uid)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val entryList = documents.map { doc ->
                        MilkEntry(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            phone = doc.getString("phone") ?: "",
                            shift = doc.getString("shift") ?: "",
                            type = doc.getString("type") ?: "",
                            quantity = doc.getLong("quantity") ?: 0,
                            rate = doc.getLong("rate") ?: 0,
                            date = doc.getString("date") ?: ""
                        )
                    }
                    showEntrySelectionDialog(entryList)
                } else {
                    Toast.makeText(context, "No entries for today", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showEntrySelectionDialog(entries: List<MilkEntry>) {
        val names = entries.map { "${it.name} (${it.shift}) - ${it.quantity}L" }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("Select Entry")
            .setItems(names) { _, index ->
                val entry = entries[index]
                currentEntry = entry
                currentTotal = entry.quantity * entry.rate
                updateBillUI(entry, currentTotal, "Not selected")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateBillUI(entry: MilkEntry, total: Long, paymentMode: String) {
        tvDairyName.text = "MilkoSys Dairy"
        tvDate.text = "Date: ${entry.date}"
        tvName.text = "Name: ${entry.name}"
        tvPhone.text = "Phone: ${entry.phone}"
        tvShift.text = "Shift: ${entry.shift}"
        tvType.text = "Type: ${entry.type}"
        tvQty.text = "Qty: ${entry.quantity} L"
        tvRate.text = "Rate: ₹${entry.rate}"
        tvAmt.text = "Amount: ₹${total}"
        tvPaymentMode.text = "Payment Mode: $paymentMode"
        tvTotal.text = "Total: ₹${total}"
    }

    private fun saveBillAsImage() {
        if (currentEntry == null) {
            Toast.makeText(context, "Generate bill first", Toast.LENGTH_SHORT).show()
            return
        }

        val bitmap = getBitmapFromView(billLayout)
        val filename = "bill_${System.currentTimeMillis()}.png"

        val fos: OutputStream?
        val uri: Uri?

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Bills")
            }
            val resolver = requireContext().contentResolver
            uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            fos = uri?.let { resolver.openOutputStream(it) }
        } else {
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Bills")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, filename)
            fos = FileOutputStream(file)
            uri = Uri.fromFile(file)
        }

        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }

        Toast.makeText(context, "Bill saved in Pictures/Bills", Toast.LENGTH_SHORT).show()
    }

    private fun shareBillAsImage() {
        if (currentEntry == null) {
            Toast.makeText(context, "Generate bill first", Toast.LENGTH_SHORT).show()
            return
        }

        val bitmap = getBitmapFromView(billLayout)
        val file = File(requireContext().externalCacheDir, "bill_share_${System.currentTimeMillis()}.png")

        FileOutputStream(file).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }

        if (!file.exists()) {
            Toast.makeText(context, "Error creating bill file", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(shareIntent, "Share Bill via"))
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    data class MilkEntry(
        val id: String = "",
        val name: String = "",
        val phone: String = "",
        val shift: String = "",
        val type: String = "",
        val quantity: Long = 0,
        val rate: Long = 0,
        val date: String = ""
    )
}
