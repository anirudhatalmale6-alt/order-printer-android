package com.orderprinter.app

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.orderprinter.app.adapter.ProductAdapter
import com.orderprinter.app.databinding.ActivityMainBinding
import com.orderprinter.app.util.EmailSender
import com.orderprinter.app.util.OrderFormatter
import com.orderprinter.app.util.ProductLoader
import androidx.preference.PreferenceManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)

        // Φόρτωση προϊόντων
        val products = ProductLoader.loadProducts(this)
        adapter = ProductAdapter(products)

        binding.recyclerProducts.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        // Κουμπί Αποστολή
        binding.btnSend.setOnClickListener {
            sendOrder()
        }

        // Κουμπί Καθαρισμός
        binding.btnClear.setOnClickListener {
            adapter.clearQuantities()
            Snackbar.make(binding.root, getString(R.string.quantities_cleared), Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun sendOrder() {
        val selected = adapter.getSelectedProducts()
        if (selected.isEmpty()) {
            Snackbar.make(binding.root, getString(R.string.no_products_selected), Snackbar.LENGTH_SHORT).show()
            return
        }

        val config = EmailSender.getConfig(this)
        if (config == null) {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.settings_required))
                .setMessage(getString(R.string.settings_required_message))
                .setPositiveButton(getString(R.string.go_to_settings)) { _, _ ->
                    startActivity(Intent(this, SettingsActivity::class.java))
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
            return
        }

        // Ανάγνωση ρυθμίσεων έκπτωσης & ΦΠΑ
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val discountPercent = (prefs.getString("discount_percent", "0") ?: "0").toDoubleOrNull() ?: 0.0
        val vatPercent = (prefs.getString("vat_percent", "0") ?: "0").toDoubleOrNull() ?: 0.0

        // Εμφάνιση σύνοψης πριν αποστολή
        val orderText = OrderFormatter.formatOrder(selected, discountPercent, vatPercent)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.order_summary))
            .setMessage(orderText)
            .setPositiveButton(getString(R.string.send)) { _, _ ->
                performSend(config, orderText)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun performSend(config: EmailSender.SmtpConfig, orderText: String) {
        binding.btnSend.isEnabled = false
        binding.btnSend.text = getString(R.string.sending)

        lifecycleScope.launch {
            val subject = OrderFormatter.formatSubject()
            val result = EmailSender.sendOrder(config, subject, orderText)

            binding.btnSend.isEnabled = true
            binding.btnSend.text = getString(R.string.send_button)

            result.fold(
                onSuccess = {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle(getString(R.string.success))
                        .setMessage(getString(R.string.order_sent_success))
                        .setPositiveButton("OK") { _, _ ->
                            adapter.clearQuantities()
                        }
                        .show()
                },
                onFailure = { e ->
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle(getString(R.string.error))
                        .setMessage("${getString(R.string.send_failed)}\n\n${e.message}")
                        .setPositiveButton("OK", null)
                        .show()
                }
            )
        }
    }
}
