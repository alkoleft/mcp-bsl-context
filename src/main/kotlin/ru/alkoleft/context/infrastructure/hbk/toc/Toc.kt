/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk.toc

import ru.alkoleft.context.infrastructure.hbk.DoubleLanguageString
import ru.alkoleft.context.infrastructure.hbk.HbkParser
import ru.alkoleft.context.infrastructure.hbk.Page

class Toc {
    private constructor(pages: List<Page>) {
        this.pages = pages
    }

    val pages: List<Page>

    companion object {
        fun parse(packBlock: ByteArray): Toc {
            val parser = HbkParser()
            var toc = Page(DoubleLanguageString("TOC", "TOC"), "")
            val pagesById = mutableMapOf(0 to toc)
            parser.parseContent(packBlock).forEach { chunk ->
                pagesById[chunk.id] =
                    Page(
                        title = chunk.title,
                        htmlPath = chunk.htmlPath,
                        children = mutableListOf()
                    ).also { pagesById[chunk.parentId]?.children?.add(it) }
            }
            return Toc(toc.children.toList())
        }

        val Chunk.title: DoubleLanguageString
            get() {
                val nameContainer: NameContainer = properties.nameContainer
                val namesContext: List<NameObject>? = nameContainer.nameObjects

                if (namesContext == null || namesContext.isEmpty()) {
                    return DoubleLanguageString("", "")
                } else if (namesContext.size == 1) {
                    val engName: NameObject = namesContext[0]
                    return DoubleLanguageString(getName(engName), "")
                } else {
                    val ruName: NameObject = namesContext[0]
                    val engName: NameObject = namesContext[1]
                    return DoubleLanguageString(getName(engName), getName(ruName))
                }
            }

        private fun getName(nameContext: NameObject): String {
            return nameContext.name.replace("\"", "")
        }

        val Chunk.htmlPath: String
            get() = properties.htmlPath.replace("\"", "")

    }
}