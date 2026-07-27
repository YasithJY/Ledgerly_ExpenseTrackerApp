package com.example.expensetrackerapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

// ListAdapter automatically handles smooth animations when data changes
class TransactionAdapter : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionsComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvNote: TextView = itemView.findViewById(R.id.tvNote)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)

        fun bind(transaction: Transaction) {
            tvCategory.text = transaction.category
            tvNote.text = transaction.note

            // Format the amount as currency automatically
            val format = NumberFormat.getCurrencyInstance(Locale.US)
            val formattedAmount = format.format(transaction.amount)

            // Dynamically change text color based on income vs expense
            if (transaction.isExpense) {
                tvAmount.text = "-$formattedAmount"
                tvAmount.setTextColor(Color.parseColor("#F44336")) // Red for expenses
            } else {
                tvAmount.text = "+$formattedAmount"
                tvAmount.setTextColor(Color.parseColor("#4CAF50")) // Green for income
            }
        }
    }

    class TransactionsComparator : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
}