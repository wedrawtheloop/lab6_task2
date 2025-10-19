package com.example.lab6_task2.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lab6_task2.network.ClientProvider
import com.example.lab6_task2.R
import com.example.lab6_task2.models.LoyaltyProgram
import com.example.lab6_task2.network.Result
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
    private lateinit var fabAddProgram: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loyalty_programs)

        clientProvider = ClientProvider()

        initViews()
        setupRecyclerView()
        setupClickListeners()

        loadLoyaltyPrograms()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewPrograms)
        emptyState = findViewById(R.id.tvEmptyState)
        loadingOverlay = findViewById(R.id.loadingOverlay)
        fabAddProgram = findViewById(R.id.fabAddProgram)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Loyalty Programs"
    }

    private fun setupRecyclerView() {
        adapter = LoyaltyProgramsAdapter(
            onItemClick = { program ->
                showProgramDetails(program)
            },
            onEditClick = { program ->
                showEditProgramDialog(program)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
    }

    private fun setupClickListeners() {
        fabAddProgram.setOnClickListener {
            showAddProgramDialog()
        }
    }

    private fun loadLoyaltyPrograms() {
        showLoading(true)

        CoroutineScope(Dispatchers.Main).launch {
            when (val result = withContext(Dispatchers.IO) {
                clientProvider.getAllLoyaltyPrograms()
            }) {
                is Result.Success -> {
                    val programs = result.data
                    adapter.submitList(programs)
                    emptyState.visibility = if (programs.isEmpty()) View.VISIBLE else View.GONE
                }
                is Result.Error -> {
                    Toast.makeText(
                        this@LoyaltyProgramsActivity,
                        "Error loading programs: ${result.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    emptyState.visibility = View.VISIBLE
                }
            }
            showLoading(false)
        }
    }

    private fun showAddProgramDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_loyalty_program, null)

        val etLevel = dialogView.findViewById<EditText>(R.id.etLevel)
        val etDiscount = dialogView.findViewById<EditText>(R.id.etDiscount)
        val etValidity = dialogView.findViewById<EditText>(R.id.etValidity)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add New Loyalty Program")
            .setView(dialogView)
            .setPositiveButton("Add") { dialog, _ ->
                val levelText = etLevel.text.toString()
                val discountText = etDiscount.text.toString()
                val validityText = etValidity.text.toString()
                val description = etDescription.text.toString()

                if (levelText.isNotEmpty() && discountText.isNotEmpty() &&
                    validityText.isNotEmpty() && description.isNotEmpty()) {

                    try {
                        val level = levelText.toInt()
                        val discount = discountText.toInt()
                        val validity = validityText.toInt()

                        if (level > 0 && discount in 1..100 && validity > 0) {
                            val program = LoyaltyProgram(
                                id_loyalty_program = null,
                                loyalty_level = level,
                                discount_amount = discount,
                                validity_period = validity,
                                description = description
                            )

                            createLoyaltyProgram(program)
                        } else {
                            Toast.makeText(this, "Please enter valid values:\n- Level > 0\n- Discount 1-100%\n- Validity > 0 days", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: NumberFormatException) {
                        Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun showEditProgramDialog(program: LoyaltyProgram) {
        if (program.id_loyalty_program == null) {
            Toast.makeText(this, "Error: Program ID is null", Toast.LENGTH_LONG).show()
            return
        }

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_loyalty_program, null)

        val etLevel = dialogView.findViewById<EditText>(R.id.etLevel)
        val etDiscount = dialogView.findViewById<EditText>(R.id.etDiscount)
        val etValidity = dialogView.findViewById<EditText>(R.id.etValidity)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)

        // Заполняем поля текущими значениями
        etLevel.setText(program.loyalty_level.toString())
        etDiscount.setText(program.discount_amount.toString())
        etValidity.setText(program.validity_period.toString())
        etDescription.setText(program.description)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Edit Loyalty Program")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                val levelText = etLevel.text.toString()
                val discountText = etDiscount.text.toString()
                val validityText = etValidity.text.toString()
                val description = etDescription.text.toString()

                if (levelText.isNotEmpty() && discountText.isNotEmpty() &&
                    validityText.isNotEmpty() && description.isNotEmpty()) {

                    try {
                        val level = levelText.toInt()
                        val discount = discountText.toInt()
                        val validity = validityText.toInt()

                        if (level > 0 && discount in 1..100 && validity > 0) {
                            // Создаем Map с измененными полями с правильными типами
                            val updates = mutableMapOf<String, Any>()

                            // Добавляем только те поля, которые изменились
                            if (level != program.loyalty_level) {
                                updates["loyalty_level"] = level
                            }
                            if (discount != program.discount_amount) {
                                updates["discount_amount"] = discount
                            }
                            if (validity != program.validity_period) {
                                updates["validity_period"] = validity
                            }
                            if (description != program.description) {
                                updates["description"] = description
                            }

                            // Если есть изменения - отправляем PATCH
                            if (updates.isNotEmpty()) {
                                patchLoyaltyProgram(program.id_loyalty_program!!, updates)
                            } else {
                                Toast.makeText(this, "No changes detected", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this, "Please enter valid values:\n- Level > 0\n- Discount 1-100%\n- Validity > 0 days", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: NumberFormatException) {
                        Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun createLoyaltyProgram(program: LoyaltyProgram) {
        showLoading(true)

        CoroutineScope(Dispatchers.Main).launch {
            when (val result = withContext(Dispatchers.IO) {
                clientProvider.createLoyaltyProgram(program)
            }) {
                is Result.Success -> {
                    loadLoyaltyPrograms()
                    Toast.makeText(this@LoyaltyProgramsActivity, "Loyalty program created successfully", Toast.LENGTH_SHORT).show()
                }
                is Result.Error -> {
                    Toast.makeText(
                        this@LoyaltyProgramsActivity,
                        "Error creating loyalty program: ${result.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            showLoading(false)
        }
    }

    private fun patchLoyaltyProgram(programId: Long, updates: Map<String, Any>) {
        showLoading(true)

        CoroutineScope(Dispatchers.Main).launch {
            when (val result = withContext(Dispatchers.IO) {
                clientProvider.patchLoyaltyProgram(programId, updates)
            }) {
                is Result.Success -> {
                    if (result.data) {
                        loadLoyaltyPrograms()
                        Toast.makeText(this@LoyaltyProgramsActivity, "Loyalty program updated successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@LoyaltyProgramsActivity, "Failed to update loyalty program", Toast.LENGTH_LONG).show()
                    }
                }
                is Result.Error -> {
                    Toast.makeText(
                        this@LoyaltyProgramsActivity,
                        "Error updating loyalty program: ${result.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            showLoading(false)
        }
    }

    private fun deleteLoyaltyProgram(id: Long) {
        showLoading(true)

        CoroutineScope(Dispatchers.Main).launch {
            when (val result = withContext(Dispatchers.IO) {
                clientProvider.deleteLoyaltyProgram(id)
            }) {
                is Result.Success -> {
                    if (result.data) {
                        loadLoyaltyPrograms()
                        Toast.makeText(this@LoyaltyProgramsActivity, "Loyalty program deleted successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@LoyaltyProgramsActivity, "Failed to delete loyalty program", Toast.LENGTH_LONG).show()
                    }
                }
                is Result.Error -> {
                    Toast.makeText(
                        this@LoyaltyProgramsActivity,
                        "Error deleting loyalty program: ${result.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            showLoading(false)
        }
    }

    private fun showProgramDetails(program: LoyaltyProgram) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Loyalty Program - Level ${program.loyalty_level}")
            .setMessage(
                "Description: ${program.description}\n" +
                        "Discount Amount: ${program.discount_amount}%\n" +
                        "Validity Period: ${program.validity_period} days\n" +
                        "Level: ${program.loyalty_level}"
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

    inner class LoyaltyProgramsAdapter(
        private val onItemClick: (LoyaltyProgram) -> Unit,
        private val onEditClick: (LoyaltyProgram) -> Unit
    ) : RecyclerView.Adapter<LoyaltyProgramsAdapter.ViewHolder>() {

        private var programs = emptyList<LoyaltyProgram>()

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvLevel: TextView = itemView.findViewById(R.id.tvProgramLevel)
            val tvDiscount: TextView = itemView.findViewById(R.id.tvDiscountAmount)
            val tvValidity: TextView = itemView.findViewById(R.id.tvValidityPeriod)
            val tvDescription: TextView = itemView.findViewById(R.id.tvProgramDescription)
            val btnEdit: View = itemView.findViewById(R.id.btnEdit)
            val btnDelete: View = itemView.findViewById(R.id.btnDelete)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_loyalty_program, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val program = programs[position]

            holder.tvLevel.text = "Level ${program.loyalty_level}"
            holder.tvDiscount.text = "${program.discount_amount}%"
            holder.tvValidity.text = "${program.validity_period} days"
            holder.tvDescription.text = program.description

            holder.itemView.setOnClickListener {
                onItemClick(program)
            }

            holder.btnEdit.setOnClickListener {
                onEditClick(program)
            }

            holder.btnDelete.setOnClickListener {
                AlertDialog.Builder(this@LoyaltyProgramsActivity)
                    .setTitle("Delete Loyalty Program")
                    .setMessage("Are you sure you want to delete Level ${program.loyalty_level} program?")
                    .setPositiveButton("Delete") { _, _ ->
                        if (program.id_loyalty_program != null) {
                            deleteLoyaltyProgram(program.id_loyalty_program!!)
                        } else {
                            Toast.makeText(
                                this@LoyaltyProgramsActivity,
                                "Error: Program ID is null",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }

        override fun getItemCount() = programs.size

        fun submitList(newList: List<LoyaltyProgram>) {
            programs = newList
            notifyDataSetChanged()
        }
    }
}