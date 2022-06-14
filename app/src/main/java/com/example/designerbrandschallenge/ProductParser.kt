package com.example.designerbrandschallenge

import java.io.File
import java.lang.Exception
import java.lang.StringBuilder

fun main() {
    val filePath = "data.txt"
    parseFile(filePath)
}

const val CLEARANCE = "Clearance Price"
const val NORMAL = "Normal Price"
const val IN_CART = "Price In Cart"

/**
 * This method reads a file, parses its contents and generates a report
 */
fun parseFile(filePath: String) {
    val resultMap = mutableMapOf<String, Triple<Double, Double, Int>>()
    var parseSuccess = false
    resultMap[CLEARANCE] = Triple(0.0, 0.0, 0)
    resultMap[NORMAL] = Triple(0.0, 0.0, 0)
    resultMap[IN_CART] = Triple(0.0, 0.0, 0)

    try {
        File(filePath).forEachLine { line ->
            val lineSplit = line.split(',')
            if (lineSplit.firstOrNull() == "Product") {
                val regularPrice = lineSplit[1].toDouble()
                val clearancePrice = lineSplit[2].toDouble()
                val count = lineSplit[3].toInt()
                val priceInCart = lineSplit[4].toBoolean()
                if (count > 2) {
                    when {
                        priceInCart -> resultMap.updateMap(IN_CART, clearancePrice)
                        clearancePrice < regularPrice -> resultMap.updateMap(
                            CLEARANCE,
                            clearancePrice
                        )
                        else -> resultMap.updateMap(NORMAL, clearancePrice)
                    }
                }

            }
        }
        parseSuccess = true
    } catch (ex: Exception) {
        println("\nERROR: ${ex.message ?: "Something went wrong"}")
    } finally {
        println(resultMap)
        if (parseSuccess) resultMap.generateReport()
    }

}

/**
 * This extension function updates the map based on the clearancePrice provided
 *,
 * @param key the key of the map
 * @param clearancePrice the price used to calculate the price range
 */
fun MutableMap<String, Triple<Double, Double, Int>>.updateMap(key: String, clearancePrice: Double) {
    val (currentRegularPrice, currentClearancePrice, currentCount) = this[key]!!
    val updatedRegularPrice: Double = when {
        currentRegularPrice == 0.0 -> clearancePrice
        currentClearancePrice > clearancePrice -> clearancePrice
        else -> currentRegularPrice
    }
    val updatedClearancePrice: Double = when {
        clearancePrice > currentClearancePrice -> clearancePrice
        else -> currentClearancePrice
    }
    this[key] = Triple(
        first = updatedRegularPrice,
        second = updatedClearancePrice,
        third = currentCount.plus(1)
    )
}

/**
 * This extension function iterates through the map
 * then it formats and prints each item in the map based on data saved
 *
 * EXAMPLE
 * map: {Clearance Price=(39.98, 49.98, 2), Normal Price=(49.99, 49.99, 1), Price In Cart=(0.0, 0.0, 0)}
 * report output:
                        Clearance Price: 2 products @ $39.98-$49.98
                        Normal Price: 1 products @ $49.99
                        Price In Cart: 0 products
 */
fun Map<String, Triple<Double, Double, Int>>.generateReport() = StringBuilder().apply {
    this@generateReport.forEach { s, triple ->
        val (regularPrice, clearancePrice, count) = triple
        val line = when {
            count == 0 -> String.format("%s: %d products", s, count)
            clearancePrice == regularPrice -> String.format(
                "%s: %d products @ $%s",
                s,
                count,
                regularPrice
            )
            else -> String.format(
                "%s: %d products @ $%s-$%s",
                s,
                count,
                regularPrice,
                clearancePrice
            )
        }
        append("\n")
        append(line)
    }
}.toString().also { println(it) }
