package com.orderprinter.app.model

/**
 * Μοντέλο προϊόντος.
 * @param name Όνομα προϊόντος
 * @param code Κωδικός προϊόντος
 * @param quantity Επιλεγμένη ποσότητα (runtime)
 */
data class Product(
    val name: String,
    val code: String,
    var quantity: Int = 0
)
