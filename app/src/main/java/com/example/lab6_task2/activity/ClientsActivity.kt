package com.example.lab6_task2.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Spinner
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ClientsActivity : AppCompatActivity() {

    private lateinit var clientProvider: ClientProvider
    private lateinit var adapter: ClientsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: TextView
    private lateinit var loadingOverlay: FrameLayout
    private lateinit var fabAddClient: FloatingActionButton

    private var clientsList = mutableListOf<Client>()
    private var loyaltyPrograms = listOf<LoyaltyProgram>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clients)

        clientProvider = ClientProvider()

        initViews()
        setupRecyclerView()
        setupClickListeners()

        loadLoyaltyPrograms()
        loadClients()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewClients)
        emptyState = findViewById(R.id.tvEmptyState)
        loadingOverlay = findViewById(R.id.loadingOverlay)
        fabAddClient = findViewById(R.id.fabAddClient)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Clients"
    }

    private fun setupRecyclerView() {
        adapter = ClientsAdapter { client ->
            showClientDetails(client)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
    }

    private fun setupClickListeners() {
        fabAddClient.setOnClickListener {
            showAddClientDialog()
        }
    }

    private fun loadLoyaltyPrograms() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                loyaltyPrograms = withContext(Dispatchers.IO) {
                    clientProvider.getAllLoyaltyPrograms()
                }
            } catch (e: Exception) {
                // Ignore error for now, programs will be loaded when needed
            }
        }
    }

    private fun loadClients() {
        showLoading(true)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val clients = withContext(Dispatchers.IO) {
                    clientProvider.getAllClients()
                }

                clientsList.clear()
                clientsList.addAll(clients)
                adapter.submitList(clientsList.toList())

                emptyState.visibility = if (clients.isEmpty()) View.VISIBLE else View.GONE
            } catch (e: Exception) {
                Toast.makeText(this@ClientsActivity, "Error loading clients: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showAddClientDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_client, null)

        // Setup spinner for loyalty programs
        val spinnerPrograms = dialogView.findViewById<Spinner>(R.id.spinnerLoyaltyProgram)
        val programNames = loyaltyPrograms.map { "Level ${it.loyaltyLevel} - ${it.discountAmount}%" }.toMutableList()
        programNames.add(0, "Select Program")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, programNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPrograms.adapter = adapter

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add New Client")
            .setView(dialogView)
            .setPositiveButton("Add") { dialog, _ ->
                val lastName = dialogView.findViewById<EditText>(R.id.etLastName).text.toString()
                val firstName = dialogView.findViewById<EditText>(R.id.etFirstName).text.toString()
                val patronymic = dialogView.findViewById<EditText>(R.id.etPatronymic).text.toString()
                val phoneNumber = dialogView.findViewById<EditText>(R.id.etPhoneNumber).text.toString()
                val address = dialogView.findViewById<EditText>(R.id.etAddress).text.toString()
                val email = dialogView.findViewById<EditText>(R.id.etEmail).text.toString()
                val selectedProgramPosition = spinnerPrograms.selectedItemPosition

                if (lastName.isNotEmpty() && firstName.isNotEmpty() && patronymic.isNotEmpty() &&
                    phoneNumber.isNotEmpty() && address.isNotEmpty() && email.isNotEmpty() &&
                    selectedProgramPosition > 0) {

                    val selectedProgram = loyaltyPrograms[selectedProgramPosition - 1]
                    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                    val client = Client(
                        lastName = lastName,
                        firstName = firstName,
                        patronymic = patronymic,
                        phoneNumber = phoneNumber,
                        address = address,
                        email = email,
                        dateRegistration = currentDate,
                        loyaltyProgram = selectedProgram.id!!
                    )
                    createClient(client)
                } else {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun createClient(client: Client) {
        showLoading(true)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                withContext(Dispatchers.IO) {
                    clientProvider.createClient(client)
                }
                loadClients() // Refresh the list
                Toast.makeText(this@ClientsActivity, "Client created successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@ClientsActivity, "Error creating client: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun deleteClient(id: Long) {
        showLoading(true)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                withContext(Dispatchers.IO) {
                    clientProvider.deleteClient(id)
                }
                loadClients() // Refresh the list
                Toast.makeText(this@ClientsActivity, "Client deleted successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@ClientsActivity, "Error deleting client: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showClientDetails(client: Client) {
        val programName = loyaltyPrograms.find { it.id == client.loyaltyProgram }?.let {
            "Level ${it.loyaltyLevel} - ${it.discountAmount}%"
        } ?: "Unknown Program"

        val dialog = AlertDialog.Builder(this)
            .setTitle("Client Details")
            .setMessage(
                "Name: ${client.lastName} ${client.firstName} ${client.patronymic}\n" +
                        "Email: ${client.email}\n" +
                        "Phone: ${client.phoneNumber}\n" +
                        "Address: ${client.address}\n" +
                        "Registration Date: ${client.dateRegistration}\n" +
                        "Loyalty Program: $programName"
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

    inner class ClientsAdapter(private val onItemClick: (Client) -> Unit) :
        RecyclerView.Adapter<ClientsAdapter.ViewHolder>() {

        private var clients = emptyList<Client>()

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvName: TextView = itemView.findViewById(R.id.tvClientName)
            val tvPhone: TextView = itemView.findViewById(R.id.tvClientPhone)
            val tvEmail: TextView = itemView.findViewById(R.id.tvClientEmail)
            val tvAddress: TextView = itemView.findViewById(R.id.tvClientAddress)
            val tvProgram: TextView = itemView.findViewById(R.id.tvLoyaltyProgram)
            val tvRegistrationDate: TextView = itemView.findViewById(R.id.tvRegistrationDate)
            val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_client, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val client = clients[position]

            // ФИО
            holder.tvName.text = "${client.lastName} ${client.firstName} ${client.patronymic}"

            // Телефон
            holder.tvPhone.text = client.phoneNumber

            // Email
            holder.tvEmail.text = client.email

            // Адрес
            holder.tvAddress.text = client.address

            // Программа лояльности
            val program = loyaltyPrograms.find { it.id == client.loyaltyProgram }
            val programText = program?.let {
                "Level ${it.loyaltyLevel} - ${it.discountAmount}%"
            } ?: "Unknown Program"
            holder.tvProgram.text = programText

            // Дата регистрации
            holder.tvRegistrationDate.text = client.dateRegistration

            holder.itemView.setOnClickListener {
                onItemClick(client)
            }

            holder.btnDelete.setOnClickListener {
                AlertDialog.Builder(this@ClientsActivity)
                    .setTitle("Delete Client")
                    .setMessage("Are you sure you want to delete ${client.lastName} ${client.firstName}?")
                    .setPositiveButton("Delete") { _, _ ->
                        deleteClient(client.id!!)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }

        override fun getItemCount() = clients.size

        fun submitList(newList: List<Client>) {
            clients = newList
            notifyDataSetChanged()
        }
    }
}