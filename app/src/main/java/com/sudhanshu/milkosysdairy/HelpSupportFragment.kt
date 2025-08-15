package com.sudhanshu.milkosysdairy

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment

class HelpSupportFragment : Fragment() {

    private lateinit var btnCall: Button
    private lateinit var btnEmail: Button
    private lateinit var cardHelp: CardView

    private val phoneNumber = "+918228958397"
    private val emailAddress = "sudhanshushekhar692004@gmail.com"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_help_support, container, false)

        btnCall = view.findViewById(R.id.btnCallSupport)
        btnEmail = view.findViewById(R.id.btnEmailSupport)
        cardHelp = view.findViewById(R.id.cardHelpSupport)

        btnCall.setOnClickListener { makeCall() }
        btnEmail.setOnClickListener { sendEmail() }

        return view
    }

    private fun makeCall() {
        try {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$phoneNumber")
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Unable to open dialer", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendEmail() {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$emailAddress")
                putExtra(Intent.EXTRA_SUBJECT, "Support Request - MilkoSys Dairy")
                putExtra(Intent.EXTRA_TEXT, "Hello Support Team,\n\nI need help regarding...")
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Unable to open mail app", Toast.LENGTH_SHORT).show()
        }
    }
}
