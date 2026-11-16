package io.github.korppujauho.pocketts.ui

/**
 * Interne DG (Digitale Gateway) regiogroepen, GEEN officiële DMR-talkgroups.
 * De code hoort bij de TeamSpeak-nickname en verandert zelf het spraakkanaal niet.
 */
data class DgRegion(val code: Int, val province: String) {
    val label: String get() = "DG-$code"
    val fullLabel: String get() = "$label · $province"
}

object DgRegions {
    const val SERVER = "heerlen.mijnts3.nl"
    const val OTHER_CODE = 999

    val all: List<DgRegion> = listOf(
        DgRegion(100, "Noord-Holland"),
        DgRegion(101, "Utrecht"),
        DgRegion(102, "Zeeland"),
        DgRegion(103, "Zuid-Holland"),
        DgRegion(104, "Noord-Brabant"),
        DgRegion(105, "Limburg"),
        DgRegion(106, "Gelderland"),
        DgRegion(107, "Overijssel"),
        DgRegion(108, "Drenthe"),
        DgRegion(109, "Groningen"),
        DgRegion(110, "Friesland"),
        DgRegion(111, "Flevoland"),
    )

    fun byCode(code: Int): DgRegion? = all.firstOrNull { it.code == code }

    /** A user without a valid DG prefix falls into DG-999 on the future Windows viewer. */
    fun regionOf(nickname: String): DgRegion? {
        val code = Regex("^DG-(\\d{3})\\s+", RegexOption.IGNORE_CASE)
            .find(nickname.trim())?.groupValues?.get(1)?.toIntOrNull()
        return code?.let(::byCode)
    }

    fun nickname(region: DgRegion, displayName: String): String {
        val clean = displayName.trim().replace(Regex("\\s+"), " ").take(22)
        return "${region.label} $clean"
    }
}
