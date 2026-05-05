package id.pbbku.mobileportal.data.mapper

import id.pbbku.mobileportal.data.api.SimpbbApiClient
import id.pbbku.mobileportal.domain.model.Nop
import id.pbbku.mobileportal.domain.model.PaymentStatus
import java.time.LocalDate
import kotlinx.serialization.json.JsonElement
import org.junit.Assert.assertEquals
import org.junit.Test

class SpptMapperTest {
    private val nop = requireNotNull(Nop.parseOrNull("32.04.010.001.001.0001.0"))

    @Test
    fun toTaxBillSummaries_mapsRowsPayloadAndSortsDescending() {
        val json = SimpbbApiClient.json.decodeFromString<JsonElement>(
            """
            {
              "rows": [
                {
                  "thnPajakSppt": "2022",
                  "pbbYgHarusDibayarSppt": "125000",
                  "statusPembayaran": "lunas",
                  "tglPembayaranSppt": "2022-08-01",
                  "tglJatuhTempoSppt": "2022-09-30"
                },
                {
                  "thnPajakSppt": 2024,
                  "jumlahTagihan": 150000,
                  "statusPembayaran": "belum lunas",
                  "tglJatuhTempoSppt": "2099-09-30"
                }
              ]
            }
            """.trimIndent(),
        )

        val bills = json.toTaxBillSummaries(nop)

        assertEquals(2, bills.size)
        assertEquals(2024, bills.first().taxYear)
        assertEquals(150000.0, bills.first().amount ?: 0.0, 0.0)
        assertEquals(PaymentStatus.UNPAID, bills.first().status)
        assertEquals(LocalDate.of(2099, 9, 30), bills.first().dueDate)
        assertEquals(2022, bills.last().taxYear)
        assertEquals(PaymentStatus.PAID, bills.last().status)
    }

    @Test
    fun toTaxBillSummaries_marksUnpaidPastDueDateAsOverdue() {
        val json = SimpbbApiClient.json.decodeFromString<JsonElement>(
            """
            [
              {
                "tahunPajak": "2020",
                "nominal": "100000",
                "isPaid": "false",
                "jatuhTempo": "2020-09-30"
              }
            ]
            """.trimIndent(),
        )

        val bills = json.toTaxBillSummaries(nop)

        assertEquals(1, bills.size)
        assertEquals(PaymentStatus.OVERDUE, bills.first().status)
        assertEquals(true, bills.first().isPayable)
    }

    @Test
    fun toTaxBillDetailOrNull_mapsDataObjectAndFallbackTaxYear() {
        val json = SimpbbApiClient.json.decodeFromString<JsonElement>(
            """
            {
              "data": {
                "statusBayar": "0",
                "spptYgHarusDibayar": "220000",
                "tglJatuhTempo": "31/12/2099",
                "dendaPbb": "5000",
                "njopBumiSppt": "500000000",
                "njopBangunanSppt": 125000000,
                "njopSppt": 625000000,
                "njoptkpSppt": 10000000,
                "tarifPbb": "0.1",
                "pbbTerhutang": "220000"
              }
            }
            """.trimIndent(),
        )

        val detail = json.toTaxBillDetailOrNull(nop, fallbackTaxYear = 2025)

        requireNotNull(detail)
        assertEquals(2025, detail.taxYear)
        assertEquals(PaymentStatus.UNPAID, detail.status)
        assertEquals(220000.0, detail.amount ?: 0.0, 0.0)
        assertEquals(LocalDate.of(2099, 12, 31), detail.dueDate)
        assertEquals(5000.0, detail.fine ?: 0.0, 0.0)
        assertEquals(500000000.0, detail.njopBumi ?: 0.0, 0.0)
        assertEquals(125000000.0, detail.njopBangunan ?: 0.0, 0.0)
        assertEquals(625000000.0, detail.njopTotal ?: 0.0, 0.0)
        assertEquals(10000000.0, detail.njoptkp ?: 0.0, 0.0)
        assertEquals(0.1, detail.tarif ?: 0.0, 0.0)
        assertEquals(220000.0, detail.pbbTerutang ?: 0.0, 0.0)
    }
}
