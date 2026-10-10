package dev.reader.ldx

/**
 * Reviewed lexical pair for the issue-9 CC0 English fixture, not a suffix rule.
 * Exact entries and package aliases win. Callers must validate this candidate
 * against the selected package; unsupported spellings are left unchanged.
 * Expanding this list or adopting a morphology resource requires owner review.
 */
internal object EnglishBaseForms {
    fun candidates(selected: String): List<String> = when (selected) {
        "flowers" -> listOf("flower")
        else -> emptyList()
    }
}
