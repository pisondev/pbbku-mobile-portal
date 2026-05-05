package id.pbbku.mobileportal.data.mapper

import id.pbbku.mobileportal.data.api.SimpbbApiClient
import id.pbbku.mobileportal.domain.model.Nop
import kotlinx.serialization.json.JsonElement
import org.junit.Assert.assertEquals
import org.junit.Test

class LspopMapperTest {
    private val nop = requireNotNull(Nop.parseOrNull("32.04.010.001.001.0001.0"))

    @Test
    fun toBuildingSummaries_mapsArrayPayload() {
        val json = SimpbbApiClient.json.decodeFromString<JsonElement>(
            """
            [
              {
                "noBng": 1,
                "luasBng": "120",
                "jmlLantaiBng": "2",
                "jnsBangunan": "Rumah tinggal",
                "jpb": { "nmJpb": "Perumahan" },
                "nilaiSistemBng": 250000000
              }
            ]
            """.trimIndent(),
        )

        val buildings = json.toBuildingSummaries(nop)

        assertEquals(1, buildings.size)
        assertEquals("1", buildings.first().noBng)
        assertEquals(120.0, buildings.first().luasBangunan ?: 0.0, 0.0)
        assertEquals(2, buildings.first().jumlahLantai)
        assertEquals("Perumahan", buildings.first().jpb)
    }

    @Test
    fun toBuildingDetailOrNull_mapsDetailFields() {
        val json = SimpbbApiClient.json.decodeFromString<JsonElement>(
            """
            {
              "noBng": 1,
              "luasBng": 120,
              "jmlLantaiBng": 2,
              "kdJpb": "01",
              "thnDibangunBng": 2015,
              "thnRenovasiBng": null,
              "kondisiBng": "Baik",
              "konstruksiBng": "Beton",
              "atapBng": "Genteng",
              "dindingBng": "Tembok",
              "lantaiBng": "Keramik",
              "langitLangitBng": "Gypsum",
              "nilaiSistemBng": "250000000"
            }
            """.trimIndent(),
        )

        val detail = json.toBuildingDetailOrNull(nop, fallbackNoBng = "1")

        requireNotNull(detail)
        assertEquals("1", detail.noBng)
        assertEquals(120.0, detail.luasBangunan ?: 0.0, 0.0)
        assertEquals(2, detail.jumlahLantai)
        assertEquals(2015, detail.tahunDibangun)
        assertEquals("Beton", detail.konstruksi)
        assertEquals(250000000.0, detail.nilaiSistemBangunan ?: 0.0, 0.0)
    }

    @Test
    fun toBuildingFacilities_mapsRowsPayload() {
        val json = SimpbbApiClient.json.decodeFromString<JsonElement>(
            """
            {
              "rows": [
                {
                  "namaFasilitas": "AC",
                  "jumlah": "2",
                  "satuan": "unit",
                  "keterangan": "Split"
                }
              ]
            }
            """.trimIndent(),
        )

        val facilities = json.toBuildingFacilities()

        assertEquals(1, facilities.size)
        assertEquals("AC", facilities.first().name)
        assertEquals(2.0, facilities.first().quantity ?: 0.0, 0.0)
        assertEquals("unit", facilities.first().unit)
    }
}
