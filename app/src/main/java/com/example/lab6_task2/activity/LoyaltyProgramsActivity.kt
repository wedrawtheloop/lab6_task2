package com.example.lab6_task2.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lab6_task2.ClientProvider
import com.example.lab6_task2.R
import com.example.lab6_task2.models.Client
import com.example.lab6_task2.models.LoyaltyProgram
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoyaltyProgramsActivity : AppCompatActivity() {

    private lateinit var clientProvider: ClientProvider
    private lateinit var adapter: LoyaltyProgramsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: TextView
    private lateinit var loadingOverlay: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loyalty_programs)

        clientProvider = ClientProvider()

        initViews()
        setupRecyclerView()

        loadLoyaltyPrograms()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewPrograms)
        emptyState = findViewById(R.id.tvEmptyState)
        loadingOverlay = findViewById(R.id.loadingOverlay)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Loyalty Programs"
    }

    private fun setupRecyclerView() {
        adapter = LoyaltyProgramsAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
    }

    private fun loadLoyaltyPrograms() {
        showLoading(true)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val programs = withContext(Dispatchers.IO) {
                    clientProvider.getAllLoyaltyPrograms()
                }

                adapter.submitList(programs)
                emptyState.visibility = if (programs.isEmpty()) View.VISIBLE else View.GONE
            } catch (e: Exception) {
                Toast.makeText(this@LoyaltyProgramsActivity, "Error loading programs: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showProgramDetails(program: LoyaltyProgram) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Loyalty Program - Level ${program.loyaltyLevel}")
            .setMessage(
                "Description: ${program.description}\n" +
                        "Discount Amount: ${program.discountAmount}%\n" +
                        "Validity Period: ${program.validityPeriod} days\n" +
                        "Level: ${program.loyaltyLevel}"
            )
            .setPositiveButton("OK", null)
            .create()

        dialog.show()
    }

    private fun showLoading(show: Boolean) {
        loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    inner class LoyaltyProgramsAdapter : RecyclerView.Adapter<LoyaltyProgramsAdapter.ViewHolder>() {

        private var programs = emptyList<LoyaltyProgram>()

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvLevel: TextView = itemView.findViewById(R.id.tvProgramLevel)
            val tvDiscount: TextView = itemView.findViewById(R.id.tvDiscountAmount)
            val tvValidity: TextView = itemView.findViewById(R.id.tvValidityPeriod)
            val tvDescription: TextView = itemView.findViewById(R.id.tvProgramDescription)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.activity_item_loyalty_program, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val program = programs[position]

            holder.tvLevel.text = "Level ${program.loyaltyLevel}"
            holder.tvDiscount.text = "${program.discountAmount}%"
            holder.tvValidity.text = "${program.validityPeriod} days"
            holder.tvDescription.text = program.description

            holder.itemView.setOnClickListener {
                showProgramDetails(program)
            }
        }

        override fun getItemCount() = programs.size

        fun submitList(newList: List<LoyaltyProgram>) {
            programs = newList
            notifyDataSetChanged()
        }
    }
}