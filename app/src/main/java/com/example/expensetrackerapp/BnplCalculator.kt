package com.example.expensetrackerapp

import java.util.Calendar

object BnplCalculator {
    
    data class InstallmentData(
        val amount: Double,
        val note: String,
        val dateMillis: Long,
        val isActivated: Boolean,
        val installmentNum: Int
    )
    
    fun calculateInstallments(
        totalAmount: Double,
        months: Int,
        baseNote: String,
        startDateMillis: Long,
        currentMillis: Long = System.currentTimeMillis()
    ): List<InstallmentData> {
        if (months <= 0) return emptyList()
        
        val baseInstallment = Math.floor((totalAmount / months) * 100) / 100
        val remainder = totalAmount - (baseInstallment * months)
        // Adjust precision for Kotlin double floating point drift
        val firstInstallment = Math.round((baseInstallment + remainder) * 100.0) / 100.0

        val list = mutableListOf<InstallmentData>()
        val calendar = Calendar.getInstance().apply { timeInMillis = startDateMillis }

        for (i in 0 until months) {
            if (i > 0) {
                calendar.add(Calendar.MONTH, 1)
            }
            val installAmt = if (i == 0) firstInstallment else baseInstallment
            val finalNote = if (baseNote.isEmpty()) {
                "BNPL Installment ${i + 1}/$months"
            } else {
                "$baseNote (Installment ${i + 1}/$months)"
            }
            
            list.add(
                InstallmentData(
                    amount = installAmt,
                    note = finalNote,
                    dateMillis = calendar.timeInMillis,
                    isActivated = (calendar.timeInMillis <= currentMillis),
                    installmentNum = i + 1
                )
            )
        }
        return list
    }
}
