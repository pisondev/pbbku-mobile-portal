package id.pbbku.mobileportal.data.mapper

import id.pbbku.mobileportal.data.api.SimpbbApiClient
import id.pbbku.mobileportal.domain.model.WilayahLevel
import kotlinx.serialization.json.JsonElement
import org.junit.Assert.assertEquals
import org.junit.Test

class WilayahMapperTest {
    @Test
    fun toWilayahItems_mapsRowsPayloadAndKeepsLeadingZero() {
        val json = SimpbbApiClient.json.decodeFromString<JsonElement>(
            """
            {
              "rows": [
                {
                  "kdKecamatan": "010",
                  "nmKecamatan": "CIBEBER"
                },
                {
                  "kdKecamatan": "002",
                  "nmKecamatan": "CIANJUR"
                }
              ]
            }
            """.trimIndent(),
        )

        val rows = json.toWilayahItems(WilayahLevel.KECAMATAN)

        assertEquals(2, rows.size)
        assertEquals("002", rows.first().code)
        assertEquals("CIANJUR", rows.first().name)
        assertEquals("010", rows.last().code)
    }

    @Test
    fun toWilayahItems_mapsDataPayloadWithFallbackName() {
        val json = SimpbbApiClient.json.decodeFromString<JsonElement>(
            """
            {
              "data": [
                {
                  "kdBlok": "001"
                }
              ]
            }
            """.trimIndent(),
        )

        val rows = json.toWilayahItems(WilayahLevel.BLOK)

        assertEquals(1, rows.size)
        assertEquals("001", rows.first().code)
        assertEquals("Kode 001", rows.first().name)
    }
}
