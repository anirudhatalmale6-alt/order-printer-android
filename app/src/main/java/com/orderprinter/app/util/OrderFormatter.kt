package com.orderprinter.app.util

import com.orderprinter.app.model.Product
import java.text.SimpleDateFormat
import java.util.*

/**
 * Μορφοποίηση δελτίου παραγγελίας για εκτύπωση.
 */
object OrderFormatter {

    fun formatOrder(
        selectedProducts: List<Product>,
        discountPercent: Double = 0.0,
        vatPercent: Double = 0.0
    ): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("el", "GR"))
        val now = dateFormat.format(Date())

        val sb = StringBuilder()
        sb.appendLine("========================================")
        sb.appendLine("         ΔΕΛΤΙΟ ΠΑΡΑΓΓΕΛΙΑΣ")
        sb.appendLine("========================================")
        sb.appendLine("Ημ/νία: $now")
        sb.appendLine("----------------------------------------")
        sb.appendLine(String.format("%-20s %-8s %4s %8s", "ΠΡΟΪΟΝ", "ΚΩΔΙΚΟΣ", "ΠΟΣ.", "ΑΞΙΑ"))
        sb.appendLine("----------------------------------------")

        var subtotal = 0.0
        for (product in selectedProducts) {
            val lineTotal = product.price * product.quantity
            subtotal += lineTotal
            if (product.price > 0) {
                sb.appendLine(
                    String.format("%-20s %-8s %4d %8.2f",
                        product.name.take(20),
                        product.code.take(8),
                        product.quantity,
                        lineTotal
                    )
                )
            } else {
                sb.appendLine(
                    String.format("%-20s %-8s %4d",
                        product.name.take(20),
                        product.code.take(8),
                        product.quantity
                    )
                )
            }
        }

        sb.appendLine("----------------------------------------")
        sb.appendLine("Σύνολο ειδών: ${selectedProducts.size}")
        sb.appendLine("Σύνολο τεμαχίων: ${selectedProducts.sumOf { it.quantity }}")

        if (subtotal > 0) {
            sb.appendLine(String.format("Υποσύνολο: %.2f €", subtotal))

            if (discountPercent > 0) {
                val discountAmount = subtotal * discountPercent / 100.0
                sb.appendLine(String.format("Έκπτωση (%.1f%%): -%.2f €", discountPercent, discountAmount))
                subtotal -= discountAmount
            }

            if (vatPercent > 0) {
                val netAfterDiscount = subtotal
                val vatAmount = netAfterDiscount * vatPercent / 100.0
                sb.appendLine(String.format("ΦΠΑ (%.1f%%): +%.2f €", vatPercent, vatAmount))
                subtotal += vatAmount
            }

            sb.appendLine(String.format("ΣΥΝΟΛΟ: %.2f €", subtotal))
        }

        sb.appendLine("========================================")

        return sb.toString()
    }

    fun formatSubject(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("el", "GR"))
        return "Παραγγελία - ${dateFormat.format(Date())}"
    }
}
