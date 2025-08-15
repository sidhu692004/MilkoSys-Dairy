package com.sudhanshu.milkosysdairy

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class AdminHomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdminFeatureAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true) // enable menu in fragment
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_admin_home, container, false)

        recyclerView = view.findViewById(R.id.adminFeatureRecycler)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        val features = listOf(
            AdminFeature("Dashboard", R.drawable.ic_dashboard),
            AdminFeature("Supplier Management", R.drawable.ic_supplier),
            AdminFeature("Customer Management", R.drawable.ic_customer),
            AdminFeature("Milk Collection", R.drawable.ic_milk),
            AdminFeature("Product Collection", R.drawable.ic_product),
            AdminFeature("Billing & Payment", R.drawable.ic_billing),
            AdminFeature("Reports & Analytics", R.drawable.ic_report),
            AdminFeature("Inventory Management", R.drawable.ic_inventory)
        )

        adapter = AdminFeatureAdapter(features) { feature ->
            when (feature.title) {
                "Billing & Payment" -> {
                    startActivity(Intent(requireContext(), BillActivity::class.java))
                }
                "Product Collection" -> {
                    startActivity(Intent(requireContext(), ProductEntryActivity::class.java))
                }
                "Milk Collection" -> {
                    startActivity(Intent(requireContext(), MilkCollectionActivity::class.java))
                }
                else -> {
                    Toast.makeText(requireContext(), "${feature.title} clicked", Toast.LENGTH_SHORT).show()
                }
            }
        }
        recyclerView.adapter = adapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar: Toolbar = view.findViewById(R.id.adminToolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.admin_home_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_notifications -> {
                Toast.makeText(requireContext(), "Notifications clicked", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_show_profile -> {
                Toast.makeText(requireContext(), "Show Profile clicked", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_edit_profile -> {
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.admin_fragment_container, ProfileEditFragment())  // Use correct container id here
                    .addToBackStack(null)
                    .commit()
                true
            }
            R.id.action_logout -> {
                logoutUser()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logoutUser() {
        val prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        val mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        mGoogleSignInClient.revokeAccess().addOnCompleteListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
