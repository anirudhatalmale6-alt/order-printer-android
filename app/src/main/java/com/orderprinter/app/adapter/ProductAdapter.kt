package com.orderprinter.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.orderprinter.app.databinding.ItemProductBinding
import com.orderprinter.app.model.Product

/**
 * Adapter για τη λίστα προϊόντων.
 * Εμφανίζει όνομα, κωδικό, τιμή (με έκπτωση), +/- κουμπιά και σύνολο γραμμής.
 */
class ProductAdapter(
    private val products: MutableList<Product>,
    private val onTotalChanged: (() -> Unit)? = null
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    var discountPercent: Double = 0.0
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private fun getDiscountedPrice(originalPrice: Double): Double {
        return if (discountPercent > 0) {
            originalPrice * (1 - discountPercent / 100.0)
        } else {
            originalPrice
        }
    }

    inner class ProductViewHolder(val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.tvProductName.text = product.name
            binding.tvProductCode.text = if (product.unit.isNotBlank()) "${product.code} · ${product.unit}" else product.code
            binding.tvQuantity.text = product.quantity.toString()

            val discountedPrice = getDiscountedPrice(product.price)

            // Τιμή μονάδας (με έκπτωση)
            if (product.price > 0) {
                binding.tvProductPrice.text = String.format("%.2f €", discountedPrice)
                binding.tvProductPrice.visibility = View.VISIBLE
            } else {
                binding.tvProductPrice.visibility = View.GONE
            }

            // Σύνολο γραμμής
            updateLineTotal(product, discountedPrice)

            binding.btnMinus.setOnClickListener {
                if (product.quantity > 0) {
                    product.quantity--
                    binding.tvQuantity.text = product.quantity.toString()
                    updateLineTotal(product, discountedPrice)
                    onTotalChanged?.invoke()
                }
            }

            binding.btnPlus.setOnClickListener {
                product.quantity++
                binding.tvQuantity.text = product.quantity.toString()
                updateLineTotal(product, discountedPrice)
                onTotalChanged?.invoke()
            }
        }

        private fun updateLineTotal(product: Product, discountedPrice: Double) {
            if (product.quantity > 0 && product.price > 0) {
                val lineTotal = discountedPrice * product.quantity
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

    /** Υπολογίζει το συνολικό ποσό (με έκπτωση, χωρίς ΦΠΑ) */
    fun getTotal(): Double {
        return products.sumOf { getDiscountedPrice(it.price) * it.quantity }
    }

    /** Μηδενίζει όλες τις ποσότητες */
    fun clearQuantities() {
        products.forEach { it.quantity = 0 }
        notifyDataSetChanged()
        onTotalChanged?.invoke()
    }
}
