package xyz.blobnom.blobnomkotlin.auth.infra.external

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AtCoderBioFetcherTest {
    @DisplayName("Extracts the affiliation from AtCoder profile HTML")
    @Test
    fun extractsAffiliation() {
        val html = """
            <table>
                <tr><th class="no-break">Country/Region</th><td>Japan</td></tr>
                <tr><th class="no-break">Affiliation</th><td class="break-all"> token&amp;value </td></tr>
            </table>
        """.trimIndent()

        assertEquals("token&value", AtCoderBioFetcher.extractAffiliation(html))
    }

    @DisplayName("Returns null when the affiliation is absent or blank")
    @Test
    fun returnsNullWhenAffiliationIsUnavailable() {
        assertNull(AtCoderBioFetcher.extractAffiliation("<html></html>"))
        assertNull(
            AtCoderBioFetcher.extractAffiliation(
                "<tr><th>Affiliation</th><td class=\"break-all\"> </td></tr>",
            ),
        )
    }
}
