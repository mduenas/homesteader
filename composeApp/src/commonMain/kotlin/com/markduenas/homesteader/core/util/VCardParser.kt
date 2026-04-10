package com.markduenas.homesteader.core.util

data class ContactImportData(
    val name: String,
    val phone: String?,
    val email: String?,
    val address: String?
)

/**
 * Minimal vCard 3.0 parser — handles what iOS and Android share from the Contacts app.
 * Extracts FN (full name), TEL (first phone), EMAIL (first email), ADR (first address).
 */
object VCardParser {

    fun parse(vcard: String): ContactImportData? {
        val lines = unfoldLines(vcard)
        if (lines.none { it.trim().uppercase() == "BEGIN:VCARD" }) return null

        var name: String? = null
        var phone: String? = null
        var email: String? = null
        var address: String? = null

        for (line in lines) {
            val upper = line.uppercase()
            when {
                upper.startsWith("FN:") || upper.startsWith("FN;") -> {
                    if (name == null) name = extractValue(line)
                }
                upper.startsWith("TEL") -> {
                    if (phone == null) phone = extractValue(line).trim()
                }
                upper.startsWith("EMAIL") -> {
                    if (email == null) email = extractValue(line).trim()
                }
                upper.startsWith("ADR") -> {
                    if (address == null) address = parseAdr(extractValue(line))
                }
            }
        }

        val resolvedName = name?.takeIf { it.isNotBlank() } ?: return null
        return ContactImportData(
            name = resolvedName,
            phone = phone?.takeIf { it.isNotBlank() },
            email = email?.takeIf { it.isNotBlank() },
            address = address?.takeIf { it.isNotBlank() }
        )
    }

    /** vCard lines can be folded (continued) with CRLF + whitespace — unfold them first. */
    private fun unfoldLines(vcard: String): List<String> {
        val normalized = vcard.replace("\r\n", "\n").replace("\r", "\n")
        val unfolded = normalized.replace(Regex("\n[ \t]"), "")
        return unfolded.lines()
    }

    /** Get the value part after the first colon in a property line. */
    private fun extractValue(line: String): String {
        val colonIdx = line.indexOf(':')
        return if (colonIdx >= 0) line.substring(colonIdx + 1).trim() else ""
    }

    /**
     * ADR value is semicolon-separated: PO Box; Extended; Street; City; State; Postal; Country
     * Join non-blank components into a readable address string.
     */
    private fun parseAdr(value: String): String {
        return value.split(";")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .joinToString(", ")
    }
}
