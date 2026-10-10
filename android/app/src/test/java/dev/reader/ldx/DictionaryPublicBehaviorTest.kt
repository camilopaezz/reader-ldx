package dev.reader.ldx

import org.junit.Test
import org.junit.Assert.*
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** Public import/lookup checks using complete real packages and authored collision fixtures. */
class DictionaryPublicBehaviorTest {
    @Test fun explicitCandidateRequiresPackageEntryAndRespectsPrecedenceAndLanguage() {
        val root = kotlin.io.path.createTempDirectory("reader-base-form-test-").toFile()
        try {
            val store = DictionaryStore(File(root, "installed"))
            // Authored tiny packages isolate collisions and missing candidate targets.
            // Real-package lookup is exercised separately below.
            fun install(label: String, words: List<String>, alias: Boolean = false, source: String = "en"): DictionaryInfo {
                val index = java.io.ByteArrayOutputStream()
                val data = java.io.ByteArrayOutputStream()
                java.io.DataOutputStream(index).use { idx ->
                    for (word in words) {
                        val definition = "Definition of $word".toByteArray()
                        idx.write(word.toByteArray()); idx.writeByte(0)
                        idx.writeInt(data.size()); idx.writeInt(definition.size); data.write(definition)
                    }
                }
                val zip = File(root, "$label.zip")
                ZipOutputStream(zip.outputStream()).use { out ->
                    fun entry(name: String, bytes: ByteArray) {
                        out.putNextEntry(ZipEntry(name)); out.write(bytes); out.closeEntry()
                    }
                    entry("test.ifo", ("StarDict's dict ifo file\nversion=3.0.0\nbookname=$label\nwordcount=${words.size}\nidxfilesize=${index.size()}\nsametypesequence=m\nsynwordcount=${if (alias) 1 else 0}\n").toByteArray())
                    entry("test.idx", index.toByteArray()); entry("test.dict", data.toByteArray())
                    if (alias) entry("test.syn", "flowers".toByteArray() + byteArrayOf(0, 0, 0, 0, 0))
                }
                return store.importPackage(zip, source, "en")
            }
            val cases = listOf(
                Triple(install("exact", listOf("flower", "flowers")), "flowers", "Exact match"),
                Triple(install("alias", listOf("blossom", "flower"), alias = true), "blossom", "Package synonym / base form"),
                Triple(install("missing-target", listOf("flowering")), null, "No entry. No supported base form."),
                Triple(install("non-English", listOf("flower"), source = "es"), null, "No entry. No supported base form.")
            )
            for ((info, headword, kind) in cases) {
                val result = store.lookup(info, "flowers")
                println("PUBLIC_LOOKUP controlled=${info.name} selected=${result.selected} matched=${result.headword} kind=${result.kind}")
                assertEquals("flowers", result.selected); assertEquals(headword, result.headword); assertEquals(kind, result.kind)
                if (headword == null) assertNull(result.definition)
                else assertEquals("Definition of $headword", result.definition)
            }
        } finally { root.deleteRecursively() }
    }

    @Test fun realImportsExactAliasesMissingAndRejectedPackages() {
        val fixtures = File(System.getenv("READER_DICTIONARY_FIXTURES") ?: "../../fixtures/dictionaries/downloaded")
        require(File(fixtures, "es-es.zip").isFile) { "Run fixtures/dictionaries/acquire.py or set READER_DICTIONARY_FIXTURES" }
        val root = kotlin.io.path.createTempDirectory("reader-dictionary-test-").toFile()
        try {
            val store = DictionaryStore(root)
            val es = store.importPackage(File(fixtures, "es-es.zip"), "es", "es")
            val esEn = store.importPackage(File(fixtures, "es-en.zip"), "es", "en")
            val enEs = store.importPackage(File(fixtures, "en-es.zip"), "en", "es")
            val en = store.importPackage(File(fixtures, "gcide.zip"), "en", "en")
            fun check(info: DictionaryInfo, token: String, head: String?, kind: String, contains: String? = null) {
                val result = store.lookup(info, token)
                println("PUBLIC_LOOKUP ${info.name} ${info.source}->${info.target} selected=$token matched=${result.headword} kind=${result.kind}")
                assertEquals(token, result.selected); assertEquals(head, result.headword); assertEquals(kind, result.kind)
                contains?.let { assertTrue("Definition must contain $it", result.definition.orEmpty().contains(it, ignoreCase = true)) }
            }
            check(es, "canción", "canción", "Exact match", "música")
            check(esEn, "canción", "canción", "Exact match", "song")
            check(en, "garden", "garden", "Exact match", "plants")
            check(enEs, "garden", "garden", "Exact match", "jardín")
            // abandoned also exists as an alias to abandon: the actual exact entry wins.
            check(enEs, "abandoned", "abandoned", "Exact match")
            check(es, "afecten", "afectar, afectarse", "Package synonym / base form (ambiguous: multiple headwords)")
            check(esEn, "afecten", "afectar", "Package synonym / base form", "affect")
            check(enEs, "flowers", "flower", "Package synonym / base form", "flor")
            check(en, "flowers", "flower", "Explicit English base form", "plant")
            check(en, "flower", "flower", "Exact match", "plant")
            for (token in listOf("Flowers", "flówers", "flowers ", "gardens", "readerldxabsentzzs")) {
                check(en, token, null, "No entry. No supported base form.")
            }
            for (info in listOf(es, esEn, enEs, en)) {
                check(info, "readerldxabsentzz", null, "No entry. No supported base form.")
                check(info, "this phrase has no dictionary entry", null, "No entry. No supported base form.")
            }
            val bad = File(root.parentFile, "malformed.zip")
            for ((label, mutate) in listOf<Pair<String, (String, ByteArray) -> ByteArray?>>( 
                "missing-index" to { name, bytes -> if (name.endsWith(".idx")) null else bytes },
                "truncated-index" to { name, bytes -> if (name.endsWith(".idx")) bytes.copyOf(3) else bytes },
                "unsupported-version" to { name, bytes -> if (name.endsWith(".ifo")) bytes.toString(Charsets.UTF_8).replace("version=3.0.0", "version=9.0.0").toByteArray() else bytes },
                "missing-data" to { name, bytes -> if (name.endsWith(".dz") || name.endsWith(".dict")) null else bytes },
                "entry-outside-data" to { name, bytes -> if (name.endsWith(".idx")) bytes.copyOf().apply { val end = indexOf(0); for (i in end + 1..end + 4) this[i] = 0xff.toByte() } else bytes },
                "alias-outside-index" to { name, bytes -> if (name.endsWith(".syn")) bytes.copyOf().apply { val end = indexOf(0); for (i in end + 1..end + 4) this[i] = 0xff.toByte() } else bytes }
            )) {
                java.util.zip.ZipFile(File(fixtures, "en-es.zip")).use { input -> ZipOutputStream(bad.outputStream()).use { output ->
                    input.entries().asSequence().forEach { entry -> mutate(entry.name, input.getInputStream(entry).readBytes())?.let { data -> output.putNextEntry(ZipEntry(entry.name)); output.write(data); output.closeEntry() } }
                } }
                try { store.importPackage(bad, "en", "es"); fail("Expected rejection: $label") } catch (e: IllegalArgumentException) { println("PUBLIC_IMPORT $label ${e.message}") }
                assertEquals(4, store.installed().size)
                assertEquals(setOf(es, esEn, enEs, en).map { it.id }.toSet(), store.installed().map { it.id }.toSet())
                assertEquals("garden", store.lookup(enEs, "garden").headword)
            }
            bad.delete()
        } finally { root.deleteRecursively() }
    }
}
