/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.search

import ru.alkoleft.context.business.entities.Definition
import ru.alkoleft.context.infrastructure.search.SimpleSearchEngine.SearchResult
import ru.alkoleft.context.infrastructure.search.indexes.Indexes

class CompoundTypeSearch(
    private val indexes: Indexes,
) {
    fun search(query: String): List<SearchResult> {
        val words = query.split("\\s+".toRegex()).filter { it.isNotBlank() }
        val results = mutableListOf<SearchResult>()
        val variants = generateCompoundVariants(words)

        variants.forEach { variant ->
            indexes.types
                .get(variant)
                .forEach { type ->
                    val wordsMatched = countWordsInVariant(variant, words)
                    results.add(compoundType(type, wordsMatched))
                }
        }

        return results
    }

    private fun countWordsInVariant(
        variant: String,
        originalWords: List<String>,
    ): Int =
        originalWords.count { word ->
            variant.lowercase().contains(word.lowercase())
        }

    private fun generateCompoundVariants(words: List<String>): List<String> {
        val variants = mutableListOf<String>()

        // Простое объединение всех слов
        variants.add(words.joinToString(""))

        // Объединение соседних слов
        for (i in 0 until words.size - 1) {
            variants.add(words[i] + words[i + 1])
        }

        // Объединение первого и последнего
        if (words.size > 2) {
            variants.add(words.first() + words.last())
        }

        return variants.distinct()
    }
}

fun compoundType(
    item: Definition,
    wordsMatched: Int,
) = SearchResult(item, 1, wordsMatched, "compound-type")
