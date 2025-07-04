/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.search

import ru.alkoleft.context.business.entities.Definition
import ru.alkoleft.context.business.valueobjects.ApiType
import ru.alkoleft.context.infrastructure.persistent.storage.PlatformContextStorage
import ru.alkoleft.context.infrastructure.search.SimpleSearchEngine.SearchResult

class WordOrderSearch(
    private val context: PlatformContextStorage,
) {
    fun search(
        query: String,
        type: ApiType?,
    ): List<SearchResult> {
        val words = query.split("\\s+".toRegex()).filter { it.isNotBlank() }
        val results = mutableListOf<SearchResult>()

        val searchTargets =
            when (type) {
                ApiType.METHOD -> context.methods.asSequence()
                ApiType.PROPERTY -> context.properties.asSequence()
                ApiType.TYPE -> context.types.asSequence()
                else ->
                    sequenceOf(
                        context.methods,
                        context.properties,
                        context.types,
                    ).flatten()
            }

        searchTargets.forEach { item ->
            val itemName = item.name
            val matchingWords = countMatchingWords(itemName, words)

            if (matchingWords > 0) {
                results.add(wordOrder(item, matchingWords))
            }
        }

        return results
    }

    private fun countMatchingWords(
        elementName: String,
        queryWords: List<String>,
    ): Int {
        val nameLower = elementName.lowercase()
        return queryWords.count { word ->
            nameLower.contains(word.lowercase())
        }
    }
}

fun wordOrder(
    item: Definition,
    wordsMatched: Int,
) = SearchResult(item, 4, wordsMatched, "word-order")
