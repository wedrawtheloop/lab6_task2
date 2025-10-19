package com.example.lab6_task2.activity

import android.content.Intent
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
import androidx.activity.OnBackPressedCallback

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
        adapter = ClientsAdapter(
            onItemClick = { client ->
                showClientDetails(client)
            },
            onEditClick = { client ->
                showEditClientDialog(client)
            }
        )

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

        val spinnerPrograms = dialogView.findViewById<Spinner>(R.id.spinnerLoyaltyProgram)
        val programNames = loyaltyPrograms.map { "Level ${it.loyalty_level} - ${it.discount_amount}%" }.toMutableList()
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

                    val loyaltyProgram = LoyaltyProgram(
                        id_loyalty_program = selectedProgram.id_loyalty_program,
                        loyalty_level = selectedProgram.loyalty_level,
                        discount_amount = selectedProgram.discount_amount,
                        validity_period = selectedProgram.validity_period,
                        description = selectedProgram.description
                    )

                    val currentDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Date())

                    val client = Client(
                        last_name = lastName,
                        first_name = firstName,
                        patronymic = patronymic,
                        phone_number = phoneNumber,
                        address = address,
                        email = email,
                        date_registration = currentDate,
                        loyalty_program = loyaltyProgram
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

    private fun showEditClientDialog(client: Client) {
        if (client.id_client == null) {
            Toast.makeText(this, "Error: Client ID is null", Toast.LENGTH_LONG).show()
            return
        }

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_client, null)

        val etLastName = dialogView.findViewById<EditText>(R.id.etLastName)
        val etFirstName = dialogView.findViewById<EditText>(R.id.etFirstName)
        val etPatronymic = dialogView.findViewById<EditText>(R.id.etPatronymic)
        val etPhoneNumber = dialogView.findViewById<EditText>(R.id.etPhoneNumber)
        val etAddress = dialogView.findViewById<EditText>(R.id.etAddress)
        val etEmail = dialogView.findViewById<EditText>(R.id.etEmail)
        val spinnerPrograms = dialogView.findViewById<Spinner>(R.id.spinnerLoyaltyProgram)

        // Заполняем поля текущими значениями
        etLastName.setText(client.last_name)
        etFirstName.setText(client.first_name)
        etPatronymic.setText(client.patronymic)
        etPhoneNumber.setText(client.phone_number)
        etAddress.setText(client.adress)
        etEmail.setText(client.email)

        // Настраиваем спиннер программ лояльности
        val programNames = loyaltyPrograms.map { "Level ${it.loyalty_level} - ${it.discount_amount}%" }.toMutableList()
        programNames.add(0, "Select Program")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, programNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPrograms.adapter = adapter

        // Выбираем текущую программу лояльности
        val currentProgramIndex = loyaltyPrograms.indexOfFirst {
            it.id_loyalty_program == client.loyaltyProgram.id_loyalty_program
        }
        if (currentProgramIndex >= 0) {
            spinnerPrograms.setSelection(currentProgramIndex + 1) // +1 потому что первый элемент "Select Program"
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Edit Client")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                val lastName = etLastName.text.toString()
                val firstName = etFirstName.text.toString()
                val patronymic = etPatronymic.text.toString()
                val phoneNumber = etPhoneNumber.text.toString()
                val address = etAddress.text.toString()
                val email = etEmail.text.toString()
                val selectedProgramPosition = spinnerPrograms.selectedItemPosition

                if (lastName.isNotEmpty() && firstName.isNotEmpty() && patronymic.isNotEmpty() &&
                    phoneNumber.isNotEmpty() && address.isNotEmpty() && email.isNotEmpty() &&
                    selectedProgramPosition > 0) {

                    val selectedProgram = loyaltyPrograms[selectedProgramPosition - 1]

                    // Создаем Map с измененными полями
                    val updates = mutableMapOf<String, Any>()

                    // Добавляем только те поля, которые изменились
                    if (lastName != client.last_name) {
                        updates["last_name"] = lastName
                    }
                    if (firstName != client.first_name) {
                        updates["first_name"] = firstName
                    }
                    if (patronymic != client.patronymic) {
                        updates["patronymic"] = patronymic
                    }
                    if (phoneNumber != client.phone_number) {
                        updates["phone_number"] = phoneNumber
                    }
                    if (address != client.adress) {
                        updates["adress"] = address
                    }
                    if (email != client.email) {
                        updates["email"] = email
                    }
                    if (selectedProgram.id_loyalty_program != client.loyaltyProgram.id_loyalty_program) {
                        updates["loyalty_program"] = mapOf(
                            "id_loyalty_program" to selectedProgram.id_loyalty_program
                        )
                    }

                    // Если есть изменения - отправляем PATCH
                    if (updates.isNotEmpty()) {
                        patchClient(client.id_client!!, updates)
                    } else {
                        Toast.makeText(this, "No changes detected", Toast.LENGTH_SHORT).show()
                    }
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
                println("🔴 Error in createClient: ${e.message}")
                Toast.makeText(this@ClientsActivity, "Error creating client: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun patchClient(clientId: Long, updates: Map<String, Any>) {
        showLoading(true)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val success = withContext(Dispatchers.IO) {
                    clientProvider.patchClient(clientId, updates)
                }
                if (success) {
                    loadClients() // Refresh the list
                    Toast.makeText(this@ClientsActivity, "Client updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ClientsActivity, "Failed to update client", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                println("🔴 Error in patchClient: ${e.message}")
                Toast.makeText(this@ClientsActivity, "Error updating client: ${e.message}", Toast.LENGTH_LONG).show()
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
        val programName = loyaltyPrograms.find { it.id_loyalty_program == client.loyaltyProgram.id_loyalty_program }?.let {
            "Level ${it.loyalty_level} - ${it.discount_amount}%"
        } ?: "Unknown Program"

        val dialog = AlertDialog.Builder(this)
            .setTitle("Client Details")
            .setMessage(
                "Name: ${client.last_name} ${client.first_name} ${client.patronymic}\n" +
                        "Email: ${client.email}\n" +
                        "Phone: ${client.phone_number}\n" +
                        "Address: ${client.adress}\n" +
                        "Registration Date: ${client.date_registration}\n" +
                        "Loyalty Program: $programName"
            )
            .setPositiveButton("OK", null)
            .create()

        dialog.show()
    }

    private fun showLoading(show: Boolean) {
        loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateBackToMain()
            }
        })
    }

    private fun navigateBackToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        navigateBackToMain()
        return true
    }

    inner class ClientsAdapter(
        private val onItemClick: (Client) -> Unit,
        private val onEditClick: (Client) -> Unit
    ) : RecyclerView.Adapter<ClientsAdapter.ViewHolder>() {

        private var clients = emptyList<Client>()

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvName: TextView = itemView.findViewById(R.id.tvClientName)
            val tvPhone: TextView = itemView.findViewById(R.id.tvClientPhone)
            val tvEmail: TextView = itemView.findViewById(R.id.tvClientEmail)
            val tvAddress: TextView = itemView.findViewById(R.id.tvClientAddress)
            val tvProgram: TextView = itemView.findViewById(R.id.tvLoyaltyProgram)
            val tvRegistrationDate: TextView = itemView.findViewById(R.id.tvRegistrationDate)
            val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
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
            holder.tvName.text = "${client.last_name} ${client.first_name} ${client.patronymic}"

            // Телефон
            holder.tvPhone.text = client.phone_number

            // Email
            holder.tvEmail.text = client.email

            // Адрес
            holder.tvAddress.text = client.adress

            // Программа лояльности
            val program = loyaltyPrograms.find { it.id_loyalty_program == client.loyaltyProgram.id_loyalty_program }
            val programText = program?.let {
                "Level ${it.loyalty_level} - ${it.discount_amount}%"
            } ?: "Unknown Program"
            holder.tvProgram.text = programText

            // Дата регистрации
            holder.tvRegistrationDate.text = client.date_registration

            holder.itemView.setOnClickListener {
                onItemClick(client)
            }

            holder.btnEdit.setOnClickListener {
                onEditClick(client)
            }

            holder.btnDelete.setOnClickListener {
                AlertDialog.Builder(this@ClientsActivity)
                    .setTitle("Delete Client")
                    .setMessage("Are you sure you want to delete ${client.last_name} ${client.first_name}?")
                    .setPositiveButton("Delete") { _, _ ->
                        deleteClient(client.id_client!!)
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