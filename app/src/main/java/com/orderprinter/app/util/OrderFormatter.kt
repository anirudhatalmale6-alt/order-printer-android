package com.orderprinter.app.util

import com.orderprinter.app.model.Product
import java.text.SimpleDateFormat
import java.util.*

/**
 * Μορφοποίηση δελτίου παραγγελίας για εκτύπωση.
 */
object OrderFormatter {

    fun formatOrder(selectedProducts: List<Product>): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("el", "GR"))
        val now = dateFormat.format(Date())

        val sb = StringBuilder()
        sb.appendLine("========================================")
        sb.appendLine("         ΔΕΛΤΙΟ ΠΑΡΑΓΓΕΛΙΑΣ")
        sb.appendLine("========================================")
        sb.appendLine("Ημ/νία: $now")
        sb.appendLine("----------------------------------------")
        sb.appendLine(String.format("%-20s %-10s %5s", "ΠΡΟΪΟΝ", "ΚΩΔΙΚΟΣ", "ΠΟΣ."))
        sb.appendLine("----------------------------------------")

        for (product in selectedProducts) {
            sb.appendLine(
                String.format("%-20s %-10s %5d",
                    product.name.take(20),
                    product.code.take(10),
                    product.quantity
                )
            )
        }

        sb.appendLine("----------------------------------------")
        sb.appendLine("Σύνολο ειδών: ${selectedProducts.size}")
        sb.appendLine("Σύνολο τεμαχίων: ${selectedProducts.sumOf { it.quantity }}")
        sb.appendLine("========================================")

        return sb.toString()
    }

    fun formatSubject(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("el", "GR"))
        return "Παραγγελία - ${dateFormat.format(Date())}"
    }
}
