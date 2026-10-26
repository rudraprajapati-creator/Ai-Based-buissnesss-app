package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.AiOrderParser
import com.example.data.model.Customer
import com.example.data.model.Product
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("BizFlow AI", appName)
    }

    @Test
    fun `test ai order parser extracts customer and products`() {
        val sampleCustomers = listOf(
            Customer(id = 1L, name = "Rajesh", phone = "+91 9822011223"),
            Customer(id = 2L, name = "Amit", phone = "+91 9822044556")
        )

        val sampleProducts = listOf(
            Product(id = 10L, name = "10mm Bolt", sellingPrice = 15.0, gstPercentage = 18.0, currentStock = 100),
            Product(id = 11L, name = "12mm Nut", sellingPrice = 8.0, gstPercentage = 18.0, currentStock = 80)
        )

        val prompt = "Rajesh needs 20 10mm bolts and 5 12mm nuts"
        val parsed = AiOrderParser.parseOrderPrompt(prompt, sampleCustomers, sampleProducts)

        assertEquals("Rajesh", parsed.customerName)
        assertNotNull(parsed.matchedCustomer)
        assertEquals(2, parsed.items.size)

        val boltItem = parsed.items.first { it.rawName.contains("Bolt", ignoreCase = true) }
        assertEquals(20, boltItem.quantity)
        assertEquals(15.0, boltItem.unitPrice, 0.01)

        val nutItem = parsed.items.first { it.rawName.contains("Nut", ignoreCase = true) }
        assertEquals(5, nutItem.quantity)
        assertEquals(8.0, nutItem.unitPrice, 0.01)

        assertTrue(parsed.grandTotal > 0.0)
    }

    @Test
    fun `test ai order parser detects product ambiguity`() {
        val sampleCustomers = listOf(
            Customer(id = 1L, name = "Rajesh", phone = "+91 9822011223")
        )

        val sampleProducts = listOf(
            Product(id = 10L, name = "10mm Bolt", sellingPrice = 15.0, currentStock = 50),
            Product(id = 11L, name = "12mm Bolt", sellingPrice = 22.0, currentStock = 40)
        )

        val prompt = "Give Rajesh 10 bolts"
        val parsed = AiOrderParser.parseOrderPrompt(prompt, sampleCustomers, sampleProducts)

        assertEquals("Rajesh", parsed.customerName)
        assertEquals(1, parsed.items.size)
        assertTrue(parsed.hasAmbiguity)
        assertEquals(2, parsed.items[0].ambiguousMatches.size)
    }
}
