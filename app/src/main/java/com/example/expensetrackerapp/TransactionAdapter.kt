package com.example.expensetrackerapp

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.content.ContextCompat

class TransactionAdapter(
    private val context: Context,
    private val onItemClick: ((Transaction) -> Unit)? = null
) : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionsComparator()) {

    private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick?.invoke(getItem(position))
                }
            }
        }

        private val tvCategoryInitial: TextView = itemView.findViewById(R.id.tvCategoryInitial)
        private val tvNote: TextView            = itemView.findViewById(R.id.tvNote)
        private val tvBnplBadge: TextView       = itemView.findViewById(R.id.tvBnplBadge)
        private val tvCategoryDate: TextView    = itemView.findViewById(R.id.tvCategoryDate)
        private val tvAmount: TextView          = itemView.findViewById(R.id.tvAmount)

        fun bind(transaction: Transaction) {
            // Category initial letter
            tvCategoryInitial.text = if (transaction.category.isNotEmpty())
                transaction.category.first().uppercase() else "T"

            // Set initial letter color by category
            tvCategoryInitial.setTextColor(getCategoryColor(transaction.category))

            // Note
            tvNote.text = transaction.note.ifEmpty { transaction.category }

            // BNPL badge
            if (transaction.isBnpl && transaction.totalInstallments > 0) {
                tvBnplBadge.visibility = View.VISIBLE
                tvBnplBadge.text = "Installment ${transaction.installmentNum}/${transaction.totalInstallments}"
            } else {
                tvBnplBadge.visibility = View.GONE
            }

            // Category · Date subtitle
            val formattedDate = dateFormat.format(Date(transaction.date))
            tvCategoryDate.text = "${transaction.category} · $formattedDate"

            // Amount with currency
            val formatted = CurrencyUtils.format(context, transaction.amount, transaction.isExpense)
            tvAmount.text = formatted
            tvAmount.setTextColor(
                if (transaction.isExpense) ContextCompat.getColor(context, R.color.expense_red)
                else ContextCompat.getColor(context, R.color.income_green)
            )
        }

        private fun getCategoryColor(category: String): Int = when (category.lowercase()) {
            "food"          -> ContextCompat.getColor(context, R.color.bnpl_amber) // Amber
            "grocery"       -> ContextCompat.getColor(context, R.color.income_green) // Green
            "shopping"      -> ContextCompat.getColor(context, R.color.primary_blue) // Blue
            "salary"        -> ContextCompat.getColor(context, R.color.income_green) // Green
            "utilities"     -> ContextCompat.getColor(context, R.color.insights_purple) // Purple
            "transport"     -> ContextCompat.getColor(context, R.color.primary_blue) // Blue
            "entertainment" -> ContextCompat.getColor(context, R.color.expense_red) // Red
            else            -> ContextCompat.getColor(context, R.color.text_secondary) // Gray
        }
    }

    class TransactionsComparator : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction) =
            oldItem == newItem
    }
}
