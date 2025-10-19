package com.example.lab6_task2.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.lab6_task2.network.ClientProvider
import com.example.lab6_task2.R
import com.example.lab6_task2.network.Result
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var clientProvider: ClientProvider
    private var clientsCount = 0
    private var programsCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        clientProvider = ClientProvider()
        setupToolbar()
        setupClickListeners()
        loadData()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Client Management"
    }

    private fun setupClickListeners() {
        findViewById<CardView>(R.id.cardClients).setOnClickListener {
            startActivity(Intent(this, ClientsActivity::class.java))
        }

        findViewById<CardView>(R.id.cardPrograms).setOnClickListener {
            startActivity(Intent(this, LoyaltyProgramsActivity::class.java))
        }
    }

    private fun loadData() {
        showLoading(true)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val clientsDeferred = async(Dispatchers.IO) {
                    when (val result = clientProvider.getAllClients()) {
                        is Result.Success -> result.data
                        is Result.Error -> {
                            println("Error loading clients: ${result.message}")
                            emptyList()
                        }
                    }
                }

                val programsDeferred = async(Dispatchers.IO) {
                    when (val result = clientProvider.getAllLoyaltyPrograms()) {
                        is Result.Success -> result.data
                        is Result.Error -> {
                            println("Error loading programs: ${result.message}")
                            emptyList()
                        }
                    }
                }

                val clients = clientsDeferred.await()
                val programs = programsDeferred.await()

                clientsCount = clients.size
                programsCount = programs.size

                updateUI()

                if (clients.isEmpty() && programs.isEmpty()) {
                    Toast.makeText(
                        this@MainActivity,
                        "Unable to load data. Please check your connection.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (clients.isEmpty()) {
                    Toast.makeText(
                        this@MainActivity,
                        "Unable to load clients",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (programs.isEmpty()) {
                    Toast.makeText(
                        this@MainActivity,
                        "Unable to load loyalty programs",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Error loading data: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                clientsCount = 0
                programsCount = 0
                updateUI()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun updateUI() {
        findViewById<TextView>(R.id.tvClientsCount).text = clientsCount.toString()
        findViewById<TextView>(R.id.tvProgramsCount).text = programsCount.toString()
    }

    private fun showLoading(show: Boolean) {
        findViewById<FrameLayout>(R.id.loadingOverlay).visibility =
            if (show) View.VISIBLE else View.GONE
    }
}