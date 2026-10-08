package dev.reader.ldx

import java.io.*
import java.security.MessageDigest
import java.util.Properties
import java.util.zip.GZIPInputStream
import java.util.zip.ZipInputStream

/** Public import and lookup boundary. No approximate or insertion-point lookup is used. */
data class DictionaryInfo(val id: String, val name: String, val source: String, val target: String, val directory: File)
data class DictionaryMatch(val selected: String, val headword: String?, val definition: String?, val kind: String, val format: String = "h")
class DictionaryStore(private val root: File) {
    fun installed(): List<DictionaryInfo> = root.listFiles().orEmpty().filter { it.name.matches(Regex("[0-9a-f]{64}")) && File(it, "installed.properties").isFile }.map { dir ->
        val p = Properties().apply { File(dir, "installed.properties").inputStream().use { load(it) } }
        DictionaryInfo(dir.name, p.getProperty("name"), p.getProperty("source"), p.getProperty("target"), dir)
    }.sortedBy { it.name }

    fun importPackage(zip: File, source: String, target: String): DictionaryInfo {
        require(source in listOf("es", "en") && target in listOf("es", "en")) { "Assign Spanish or English source and target languages" }
        root.mkdirs()
        val id = zip.inputStream().use { input -> val digest = MessageDigest.getInstance("SHA-256"); val buffer = ByteArray(65536); while (true) { val n = input.read(buffer); if (n < 0) break; digest.update(buffer, 0, n) }; digest.digest() }.joinToString("") { "%02x".format(it) }
        val staged = File(root, ".staging-$id").apply { deleteRecursively(); mkdirs() }
        try {
            var total = 0L
            ZipInputStream(zip.inputStream().buffered()).use { input ->
                while (true) {
                    val entry = input.nextEntry ?: break
                    if (entry.isDirectory) continue
                    val name = entry.name.substringAfterLast('/')
                    require(name.isNotBlank() && name !in listOf(".", "..", "installed.properties")) { "Invalid package filename" }
                    val out = File(staged, name)
                    require(!out.exists()) { "Duplicate package filename" }
                    out.outputStream().use { output ->
                        val buffer = ByteArray(65536)
                        while (true) { val n = input.read(buffer); if (n < 0) break; total += n; require(total <= 512L * 1024 * 1024) { "Package exceeds 512 MiB" }; output.write(buffer, 0, n) }
                    }
                }
            }
            val ifos = staged.listFiles().orEmpty().filter { it.extension == "ifo" }
            require(ifos.size == 1) { "Package must contain exactly one StarDict .ifo" }
            val ifo = ifos.single()
            val lines = ifo.readLines()
            require(lines.firstOrNull() == "StarDict's dict ifo file") { "Invalid StarDict header" }
            val properties = lines.drop(1).mapNotNull { line -> line.indexOf('=').takeIf { it > 0 }?.let { line.substring(0, it) to line.substring(it + 1) } }.toMap()
            require(properties["version"] in listOf("2.4.2", "3.0.0")) { "Unsupported StarDict version" }
            require(properties["idxoffsetbits"] != "64") { "64-bit indices are unsupported" }
            require(properties["sametypesequence"] in listOf("m", "h", "x")) { "Supported entry formats are single m, h or x text fields" }
            val stem = ifo.nameWithoutExtension
            fun expand(plain: String, compressed: String) {
                val dest = File(staged, plain); val gz = File(staged, compressed)
                if (!dest.exists() && gz.exists()) GZIPInputStream(gz.inputStream()).use { input -> dest.outputStream().use { output ->
                    val buffer = ByteArray(65536); var expanded = 0L
                    while (true) { val n = input.read(buffer); if (n < 0) break; expanded += n; require(expanded <= 512L * 1024 * 1024) { "Expanded dictionary exceeds 512 MiB" }; output.write(buffer, 0, n) }
                } }
                require(dest.isFile && dest.length() <= 512L * 1024 * 1024) { "Missing or oversized $plain" }
            }
            expand("$stem.idx", "$stem.idx.gz")
            expand("$stem.dict", "$stem.dict.dz")
            val idx = File(staged, "$stem.idx"); val dict = File(staged, "$stem.dict")
            require(idx.length() == properties["idxfilesize"]?.toLongOrNull()) { "Index size does not match metadata" }
            var count = 0
            entries(idx) { _, offset, length -> require(offset + length <= dict.length()) { "Entry exceeds dictionary data" }; count++ }
            require(count == properties["wordcount"]?.toIntOrNull()) { "Word count does not match metadata" }
            require(count > 0) { "Empty dictionary" }
            val syn = File(staged, "$stem.syn")
            var aliases = 0
            if (syn.exists()) synonyms(syn) { _, index -> require(index in 0 until count) { "Synonym points outside index" }; aliases++ }
            require(aliases == (properties["synwordcount"]?.toIntOrNull() ?: 0)) { "Synonym count does not match metadata" }
            val p = Properties().apply {
                setProperty("name", properties["bookname"] ?: stem); setProperty("source", source); setProperty("target", target)
                setProperty("stem", stem); setProperty("format", properties.getValue("sametypesequence"))
            }
            File(staged, "installed.properties").outputStream().use { p.store(it, "User-assigned languages are a prototype hypothesis") }
            val destination = File(root, id)
            if (destination.exists()) { staged.deleteRecursively(); return installed().first { it.id == id } }
            require(staged.renameTo(destination)) { "Unable to install dictionary" }
            return DictionaryInfo(id, p.getProperty("name"), source, target, destination)
        } catch (e: Exception) { staged.deleteRecursively(); throw IllegalArgumentException("Dictionary rejected: ${e.message}", e) }
    }
    fun lookup(info: DictionaryInfo, selected: String): DictionaryMatch {
        val p = Properties().apply { File(info.directory, "installed.properties").inputStream().use { load(it) } }
        val stem = p.getProperty("stem")
        val found = mutableListOf<Triple<String, Long, Int>>()
        var kind = "Exact match"
        entries(File(info.directory, "$stem.idx")) { word, offset, length -> if (word == selected) found += Triple(word, offset, length) }
        if (found.isEmpty()) {
            val syn = File(info.directory, "$stem.syn")
            val targets = mutableSetOf<Int>()
            if (syn.exists()) synonyms(syn) { alias, index -> if (alias == selected) targets += index }
            if (targets.isNotEmpty()) {
                var index = 0
                entries(File(info.directory, "$stem.idx")) { word, offset, length -> if (index++ in targets) found += Triple(word, offset, length) }
                kind = "Package synonym / base form"
            }
        }
        if (found.isEmpty()) return DictionaryMatch(selected, null, null, "No entry. No supported package base form.")
        val definitions = RandomAccessFile(File(info.directory, "$stem.dict"), "r").use { data ->
            found.map { entry -> val bytes = ByteArray(entry.third); data.seek(entry.second); data.readFully(bytes); bytes.toString(Charsets.UTF_8) }
        }
        return DictionaryMatch(selected, found.map { it.first }.distinct().joinToString(", "), definitions.joinToString(if (p.getProperty("format") == "m") "\n\n" else "<hr/>"), kind, p.getProperty("format"))
    }
    private fun synonyms(file: File, action: (String, Int) -> Unit) {
        DataInputStream(file.inputStream().buffered()).use { input ->
            while (true) {
                val first = input.read(); if (first < 0) break
                val word = ByteArrayOutputStream(); var b = first
                while (b != 0) { require(b >= 0 && word.size() < 4096) { "Truncated synonym" }; word.write(b); b = input.read() }
                action(Charsets.UTF_8.newDecoder().decode(java.nio.ByteBuffer.wrap(word.toByteArray())).toString(), input.readInt())
            }
        }
    }
    private fun entries(file: File, action: (String, Long, Int) -> Unit) {
        DataInputStream(file.inputStream().buffered()).use { input ->
            while (true) {
                val first = input.read(); if (first < 0) break
                val word = ByteArrayOutputStream(); var b = first
                while (b != 0) { require(b >= 0 && word.size() < 4096) { "Truncated or oversized index headword" }; word.write(b); b = input.read() }
                val offset = input.readInt().toLong() and 0xffffffffL
                val length = input.readInt(); require(length in 0..(4 * 1024 * 1024)) { "Unsupported entry size" }
                action(Charsets.UTF_8.newDecoder().decode(java.nio.ByteBuffer.wrap(word.toByteArray())).toString(), offset, length)
            }
        }
    }
}
