package com.example.expensetrackerapp

import org.junit.Assert.assertEquals
import org.junit.Test

class FinancialMathTest {

    @Test
    fun testCurrencyUtils_convertToBasePure_USD() {
        val displayAmount = 10.0 // 10 USD
        val baseLkr = CurrencyUtils.convertToBasePure(displayAmount, isUsd = true)
        assertEquals(3284.2, baseLkr, 0.01)
    }

    @Test
    fun testCurrencyUtils_convertToBasePure_LKR() {
        val displayAmount = 500.0 // 500 LKR
        val baseLkr = CurrencyUtils.convertToBasePure(displayAmount, isUsd = false)
        assertEquals(500.0, baseLkr, 0.01)
    }

    @Test
    fun testCurrencyUtils_convertFromBasePure_USD() {
        val baseLkr = 3284.2 // 3284.2 LKR
        val displayAmount = CurrencyUtils.convertFromBasePure(baseLkr, isUsd = true)
        assertEquals(10.0, displayAmount, 0.01)
    }

    @Test
    fun testBnplCalculator_splitsEvenly() {
        val amount = 300.0
        val months = 3
        val installments = BnplCalculator.calculateInstallments(amount, months, "Phone", 0L, 0L)
        
        assertEquals(3, installments.size)
        assertEquals(100.0, installments[0].amount, 0.01)
        assertEquals(100.0, installments[1].amount, 0.01)
        assertEquals(100.0, installments[2].amount, 0.01)
        
        assertEquals("Phone (Installment 1/3)", installments[0].note)
    }

    @Test
    fun testBnplCalculator_handlesRemaindersCorrectly() {
        val amount = 100.0
        val months = 3
        val installments = BnplCalculator.calculateInstallments(amount, months, "", 0L, 0L)
        
        assertEquals(3, installments.size)
        // 100 / 3 = 33.33. Remainder 0.01 added to first month
        assertEquals(33.34, installments[0].amount, 0.01)
        assertEquals(33.33, installments[1].amount, 0.01)
        assertEquals(33.33, installments[2].amount, 0.01)
        
        assertEquals("BNPL Installment 1/3", installments[0].note)
        assertEquals(1, installments[0].installmentNum)
        assertEquals(2, installments[1].installmentNum)
    }
}
