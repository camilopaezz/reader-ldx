package dev.reader.ldx

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import java.net.URI
import java.util.zip.ZipFile

/** Bounded extraction from the imported EPUB, with its original document-relative context. */
class BookNotePublication(private val file: File) {
    private val resourceNames = ZipFile(file).use { zip -> zip.entries().asSequence().filter { !it.isDirectory }.map { it.name }.toSet() }
    private val documents = java.util.concurrent.ConcurrentHashMap<String, Document>()
    fun resolve(base: String, href: String): String {
        if (resourceNames.contains(href.substringBefore('#'))) return href
        return runCatching {
            URI("https://publication.invalid/$base").resolve(href).normalize().let {
                if (it.host != "publication.invalid") "" else it.path.removePrefix("/") + (it.rawFragment?.let { f -> "#$f" } ?: "")
            }
        }.getOrDefault("")
    }
    fun extract(target: String, marked: Boolean = false): String? {
        val path = target.substringBefore('#')
        val fragment = target.substringAfter('#', "").takeIf { it.isNotEmpty() } ?: return null
        val document = documents[path] ?: resource(path)?.let { bytes -> Jsoup.parse(bytes.toString(Charsets.UTF_8)).also { documents[path] = it } } ?: return null
        val element = document.getElementById(java.net.URLDecoder.decode(fragment, "UTF-8")) ?: return null
        val types = element.attr("epub:type").split(' ')
        val recognized = types.any { it == "endnote" || it == "footnote" } || element.attr("role") in listOf("doc-endnote", "doc-footnote") || (element.tagName() == "li" && element.parents().any { "endnotes" in it.attr("epub:type").split(' ') })
        if (!marked && !recognized) return null
        val clone = element.clone()
        clone.select("script, iframe, object, embed, style, link, base, meta").remove()
        clone.getAllElements().forEach { node -> node.attributes().asList().filter { it.key.startsWith("on") || it.key == "style" }.forEach { node.removeAttr(it.key) } }
        return clone.outerHtml()
    }
    fun resource(target: String): ByteArray? {
        val path = target.substringBefore('#')
        if (path !in resourceNames) return null
        return ZipFile(file).use { zip ->
            val entry = zip.getEntry(path) ?: return null
            // Prototype bound for an individual XHTML document or image, not total EPUB size.
            if (entry.size > 8 * 1024 * 1024) return null
            zip.getInputStream(entry).use { it.readBytes() }
        }
    }
}
