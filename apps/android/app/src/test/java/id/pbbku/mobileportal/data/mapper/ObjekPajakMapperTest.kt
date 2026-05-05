package id.pbbku.mobileportal.data.mapper

import id.pbbku.mobileportal.data.api.SimpbbApiClient
import kotlinx.serialization.json.JsonElement
import org.junit.Assert.assertEquals
import org.junit.Test

class ObjekPajakMapperTest {
    @Test
    fun toObjekPajakSearchRows_mapsSearchArrayAndKeepsLeadingZero() {
        val json = SimpbbApiClient.json.decodeFromString<JsonElement>(
            """
            [
              {
                "kdPropinsi": "32",
                "kdDati2": "04",
                "kdKecamatan": "010",
                "kdKelurahan": "001",
                "kdBlok": "001",
                "noUrut": "0001",
                "kdJnsOp": "0",
                "nmWp": "BUDI EMBER BOCOR",
                "jalanOp": "JL. KENANGAN PAHIT NO. 45"
              }
            ]
            """.trimIndent(),
        )

        val rows = json.toObjekPajakSearchRows()

        assertEquals(1, rows.size)
        assertEquals("320401000100100010", rows.first().nopDisplay)
        assertEquals("010", rows.first().nop.kdKecamatan)
        assertEquals("0001", rows.first().nop.noUrut)
    }

    @Test
    fun toObjekPajakPage_mapsRowsAndTotal() {
        val json = SimpbbApiClient.json.decodeFromString<JsonElement>(
            """
            {
              "rows": [
                {
                  "kdPropinsi": "32",
                  "kdDati2": "04",
                  "kdKecamatan": "010",
                  "kdKelurahan": "001",
                  "kdBlok": "001",
                  "noUrut": "0001",
                  "kdJnsOp": "0",
                  "nmWp": "BUDI EMBER BOCOR",
                  "jalanOp": "JL. KENANGAN PAHIT NO. 45",
                  "luasBumi": 250,
                  "njopBumi": 500000000,
                  "totalLuasBng": "0",
                  "totalNilaiBng": "0"
                }
              ],
              "total": 1
            }
            """.trimIndent(),
        )

        val page = json.toObjekPajakPage()

        assertEquals(1, page.total)
        assertEquals(1, page.rows.size)
        assertEquals(250.0, page.rows.first().luasBumi ?: 0.0, 0.0)
    }
}
