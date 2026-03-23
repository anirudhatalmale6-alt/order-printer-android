package com.orderprinter.app.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.orderprinter.app.model.Product

/**
 * Φόρτωση λίστας προϊόντων από assets/products.json
 */
object ProductLoader {

    fun loadProducts(context: Context): MutableList<Product> {
        val json = context.assets.open("products.json")
            .bufferedReader().use { it.readText() }

        val type = object : TypeToken<List<ProductJson>>() {}.type
        val items: List<ProductJson> = Gson().fromJson(json, type)

        return items.map {
            Product(
                name = it.name,
                code = it.code,
                unit = it.unit ?: "",
                price = it.price ?: 0.0
            )
        }.toMutableList()
    }

    private data class ProductJson(
        val name: String,
        val code: String,
        val unit: String? = null,
        val price: Double? = null
    )
}
