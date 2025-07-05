/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk.toc

private const val BOM = '\uFEFF'

object Tokenizer {
    fun tokenize(content: String): List<String> {
        val tokens = mutableListOf<String>()
        val currentToken = StringBuilder()
        var inString = false
        var i = 0
        while (i < content.length) {
            val char = content[i]
            when {
                char == BOM -> {}

                char == '"' -> {
                    if (inString) {
                        // Проверяем экранирование кавычки
                        if (i + 1 < content.length && content[i + 1] == '"') {
                            currentToken.append('"')
                            i++
                        } else {
                            currentToken.append(char)
                            tokens.add(currentToken.toString())
                            currentToken.clear()
                            inString = false
                        }
                    } else {
                        if (currentToken.isNotEmpty()) {
                            tokens.add(currentToken.toString().trim())
                            currentToken.clear()
                        }
                        currentToken.append(char)
                        inString = true
                    }
                }

                inString -> {
                    currentToken.append(char)
                }

                char.isWhitespace() -> {
                    if (currentToken.isNotEmpty()) {
                        tokens.add(currentToken.toString().trim())
                        currentToken.clear()
                    }
                }

                char == '{' || char == '}' || char == ',' -> {
                    if (currentToken.isNotEmpty()) {
                        tokens.add(currentToken.toString().trim())
                        currentToken.clear()
                    }
                    tokens.add(char.toString())
                }

                else -> {
                    currentToken.append(char)
                }
            }
            i++
        }
        if (currentToken.isNotEmpty()) {
            tokens.add(currentToken.toString().trim())
        }
        return tokens.filter { it.isNotEmpty() && it != "," }
    }

}