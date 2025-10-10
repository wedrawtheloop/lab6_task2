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
import com.example.lab6_task2.ClientProvider
import com.example.lab6_task2.R
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

        // Загружаем данные в фоновом потоке
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val clientsDeferred = async(Dispatchers.IO) { clientProvider.getAllClients() }
                val programsDeferred = async(Dispatchers.IO) { clientProvider.getAllLoyaltyPrograms() }

                val clients = clientsDeferred.await()
                val programs = programsDeferred.await()

                clientsCount = clients.size
                programsCount = programs.size

                updateUI()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error loading data: ${e.message}", Toast.LENGTH_LONG).show()
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