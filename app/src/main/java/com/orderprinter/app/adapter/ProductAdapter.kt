package com.orderprinter.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.orderprinter.app.databinding.ItemProductBinding
import com.orderprinter.app.model.Product

/**
 * Adapter για τη λίστα προϊόντων.
 * Εμφανίζει όνομα, κωδικό, τιμή, +/- κουμπιά και σύνολο γραμμής.
 */
class ProductAdapter(
    private val products: MutableList<Product>,
    private val onTotalChanged: (() -> Unit)? = null
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.tvProductName.text = product.name
            binding.tvProductCode.text = if (product.unit.isNotBlank()) "${product.code} · ${product.unit}" else product.code
            binding.tvQuantity.text = product.quantity.toString()

            // Τιμή μονάδας
            if (product.price > 0) {
                binding.tvProductPrice.text = String.format("%.2f €", product.price)
                binding.tvProductPrice.visibility = View.VISIBLE
            } else {
                binding.tvProductPrice.visibility = View.GONE
            }

            // Σύνολο γραμμής
            updateLineTotal(product)

            binding.btnMinus.setOnClickListener {
                if (product.quantity > 0) {
                    product.quantity--
                    binding.tvQuantity.text = product.quantity.toString()
                    updateLineTotal(product)
                    onTotalChanged?.invoke()
                }
            }

            binding.btnPlus.setOnClickListener {
                product.quantity++
                binding.tvQuantity.text = product.quantity.toString()
                updateLineTotal(product)
                onTotalChanged?.invoke()
            }
        }

        private fun updateLineTotal(product: Product) {
            if (product.quantity > 0 && product.price > 0) {
                val lineTotal = product.price * product.quantity
                binding.tvLineTotal.text = String.format("%.2f €", lineTotal)
                binding.tvLineTotal.visibility = View.VISIBLE
            } else {
                binding.tvLineTotal.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    /** Επιστρέφει μόνο τα προϊόντα με ποσότητα > 0 */
    fun getSelectedProducts(): List<Product> {
        return products.filter { it.quantity > 0 }
    }

    /** Υπολογίζει το συνολικό ποσό */
    fun getTotal(): Double {
        return products.sumOf { it.price * it.quantity }
    }

    /** Μηδενίζει όλες τις ποσότητες */
    fun clearQuantities() {
        products.forEach { it.quantity = 0 }
        notifyDataSetChanged()
        onTotalChanged?.invoke()
    }
}
