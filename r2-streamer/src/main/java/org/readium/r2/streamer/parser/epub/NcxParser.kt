/*
 * Module: r2-streamer-kotlin
 * Developers: Aferdita Muriqi, Clément Baumann
 *
 * Copyright (c) 2018. Readium Foundation. All rights reserved.
 * Use of this source code is governed by a BSD-style license which is detailed in the
 * LICENSE file present in the project repository where this source code is maintained.
 */

package org.readium.r2.streamer.parser.epub

import android.sax.Element
import org.readium.r2.shared.Link
import org.readium.r2.shared.parser.xml.ElementNode
import org.readium.r2.streamer.parser.normalize

internal data class Ncx(
        val toc: List<Link>,
        val pageList: List<Link>
)

internal object NcxParser {
    fun parse(document: ElementNode, filePath: String): Ncx? {
        val toc = document.getFirst("navMap", Namespaces.Ncx)?.let { parseNavMapElement(it, filePath) }
                ?: emptyList()
        val pageList = document.getFirst("pageList", Namespaces.Ncx)?.let { parsePageListElement(it, filePath) }
                ?: emptyList()
        return Ncx(toc, pageList)
    }

    private fun parseNavMapElement(element: ElementNode, filePath: String): List<Link> =
            element.get("navPoint", Namespaces.Ncx).map { parseNavPointElement(it, filePath) }

    private fun parsePageListElement(element: ElementNode, filePath: String): List<Link> =
            element.get("pageTarget", Namespaces.Ncx).map {
                Link().apply {
                    title = extractTitle(element)
                    href = extractHref(element, filePath)
                }
            }

    private fun extractTitle(element: ElementNode) =
            element.getFirst("navLabel", Namespaces.Ncx)?.getFirst("text", Namespaces.Ncx)?.text

    private fun extractHref(element: ElementNode, filePath: String): String? {
        val href = element.getFirst("content", Namespaces.Ncx)?.getAttr("src") ?: return null
        return normalize(filePath, href)
    }

    private fun parseNavPointElement(element: ElementNode, filePath: String): Link {
        val title = extractTitle(element)
        val href = extractHref(element, filePath)
        val children = element.get("navPoint", Namespaces.Ncx).map { parseNavPointElement(it, filePath) }
        return Link().apply {
            this.title = title
            this.href = href
            this.children = children.toMutableList()
        }
    }
}
