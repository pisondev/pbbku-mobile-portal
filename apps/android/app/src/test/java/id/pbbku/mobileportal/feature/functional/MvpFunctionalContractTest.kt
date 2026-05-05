package id.pbbku.mobileportal.feature.functional

import id.pbbku.mobileportal.core.security.isValidNik
import id.pbbku.mobileportal.core.security.maskNik
import id.pbbku.mobileportal.core.error.AppError
import id.pbbku.mobileportal.core.error.toAppError
import id.pbbku.mobileportal.core.error.toUserMessage
import id.pbbku.mobileportal.data.api.SimpbbApiClient
import id.pbbku.mobileportal.data.dto.BuildingRequest
import id.pbbku.mobileportal.data.dto.NopRequest
import id.pbbku.mobileportal.data.dto.ObjekPajakSearchRequest
import id.pbbku.mobileportal.data.dto.OrpcRequest
import id.pbbku.mobileportal.data.dto.SpptGetRequest
import id.pbbku.mobileportal.data.mapper.toBuildingDetailOrNull
import id.pbbku.mobileportal.data.mapper.toBuildingFacilities
import id.pbbku.mobileportal.data.mapper.toBuildingSummaries
import id.pbbku.mobileportal.data.mapper.toObjekPajakDetailOrNull
import id.pbbku.mobileportal.data.mapper.toObjekPajakSearchRows
import id.pbbku.mobileportal.data.mapper.toTaxBillDetailOrNull
import id.pbbku.mobileportal.data.mapper.toTaxBillSummaries
import id.pbbku.mobileportal.domain.model.Nop
import id.pbbku.mobileportal.domain.model.PaymentStatus
import id.pbbku.mobileportal.feature.auth.AuthViewModel
import id.pbbku.mobileportal.feature.report.ReportDraftFormValidator
import java.io.IOException
import java.time.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonElement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MvpFunctionalContractTest {
    private val demoNop = requireNotNull(Nop.parseOrNull("32.04.010.001.001.0001.0"))

    @Test
    fun tc001_tc004_loginNikAndOtpRules_areDeterministicForDemo() {
        assertTrue("3404123456789012".isValidNik())
        assertEquals(AuthViewModel.DEMO_OTP, "123456")
    }

    @Test
    fun tc002_tc003_invalidNikInputs_areRejected() {
        assertFalse("340412345678901".isValidNik())
        assertFalse("34041234567890123".isValidNik())
        assertFalse("34041234567890AB".isValidNik())
    }

    @Test
    fun tc005_tc016_nopLeadingZeroAndApiWrapper_arePreserved() {
        assertEquals("010", demoNop.kdKecamatan)
        assertEquals("001", demoNop.kdKelurahan)
        assertEquals("001", demoNop.kdBlok)
        assertEquals("0001", demoNop.noUrut)
        assertEquals("32.04.010.001.001.0001.0", demoNop.asGroupedText())

        val encoded = SimpbbApiClient.json.encodeToString(
            OrpcRequest(NopRequest.fromDomain(demoNop)),
        )

        assertTrue(encoded.contains("\"json\""))
        assertTrue(encoded.contains("\"kdKecamatan\":\"010\""))
        assertTrue(encoded.contains("\"kdKelurahan\":\"001\""))
        assertTrue(encoded.contains("\"kdBlok\":\"001\""))
        assertTrue(encoded.contains("\"noUrut\":\"0001\""))
    }

    @Test
    fun tc003_searchRequest_usesJsonWrapperQueryAndLimit() {
        val encoded = SimpbbApiClient.json.encodeToString(
            OrpcRequest(ObjekPajakSearchRequest(query = "BUDI", limit = 10)),
        )

        assertEquals("{\"json\":{\"query\":\"BUDI\",\"limit\":10}}", encoded)
    }

    @Test
    fun tc003_tc004_searchAndDetail_mapsPartialDataWithoutCrash() {
        val searchJson = jsonElement(
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
                "nmWp": "BUDI DEMO"
              }
            ]
            """.trimIndent(),
        )

        val searchRows = searchJson.toObjekPajakSearchRows()

        assertEquals(1, searchRows.size)
        assertEquals("BUDI DEMO", searchRows.first().namaWajibPajak)
        assertNull(searchRows.first().alamatObjekPajak)

        val detailJson = jsonElement(
            """
            {
              "kdPropinsi": "32",
              "kdDati2": "04",
              "kdKecamatan": "010",
              "kdKelurahan": "001",
              "kdBlok": "001",
              "noUrut": "0001",
              "kdJnsOp": "0",
              "subjekPajak": { "nmWp": "BUDI DEMO" }
            }
            """.trimIndent(),
        )

        val detail = detailJson.toObjekPajakDetailOrNull()

        assertNotNull(detail)
        requireNotNull(detail)
        assertEquals("BUDI DEMO", detail.namaWajibPajak)
        assertNull(detail.alamatObjekPajak)
        assertNull(detail.luasBumi)
    }

    @Test
    fun tc006_tc007_tc008_spptHistoryDetailAndTunggakanStatus_areMapped() {
        val historyJson = jsonElement(
            """
            [
              {
                "tahunPajak": 2025,
                "nominal": 175000,
                "statusPembayaran": "belum lunas",
                "jatuhTempo": "2099-09-30",
                "denda": 0
              },
              {
                "tahunPajak": 2024,
                "nominal": 150000,
                "statusPembayaran": "lunas",
                "tanggalPembayaran": "2024-08-01"
              }
            ]
            """.trimIndent(),
        )

        val bills = historyJson.toTaxBillSummaries(demoNop)

        assertEquals(2, bills.size)
        assertEquals(2025, bills.first().taxYear)
        assertEquals(PaymentStatus.UNPAID, bills.first().status)
        assertTrue(bills.first().isPayable)
        assertEquals(175000.0, bills.filter { it.isPayable }.sumOf { it.amount ?: 0.0 }, 0.0)
        assertEquals(PaymentStatus.PAID, bills.last().status)
        assertFalse(bills.last().isPayable)

        val detailJson = jsonElement(
            """
            {
              "data": {
                "tahunPajak": 2025,
                "statusPembayaran": "belum lunas",
                "nominal": 175000,
                "jatuhTempo": "2099-09-30",
                "denda": 0,
                "njopBumi": 400000000,
                "njopBangunan": 100000000,
                "njopTotal": 500000000,
                "njoptkp": 10000000,
                "tarif": 0.1,
                "pbbTerutang": 175000
              }
            }
            """.trimIndent(),
        )

        val detail = detailJson.toTaxBillDetailOrNull(demoNop, fallbackTaxYear = 2025)

        assertNotNull(detail)
        requireNotNull(detail)
        assertEquals(2025, detail.taxYear)
        assertEquals(PaymentStatus.UNPAID, detail.status)
        assertEquals(LocalDate.of(2099, 9, 30), detail.dueDate)
        assertEquals(500000000.0, detail.njopTotal ?: 0.0, 0.0)
        assertTrue(detail.isPayable)
    }

    @Test
    fun tc009_tc010_tc011_buildingListDetailAndFacilities_areMapped() {
        val listJson = jsonElement(
            """
            {
              "rows": [
                {
                  "noBng": 1,
                  "luasBng": 120,
                  "jmlLantaiBng": 2,
                  "jnsBangunan": "Rumah tinggal"
                }
              ]
            }
            """.trimIndent(),
        )

        val buildings = listJson.toBuildingSummaries(demoNop)

        assertEquals(1, buildings.size)
        assertEquals("1", buildings.first().noBng)
        assertEquals(120.0, buildings.first().luasBangunan ?: 0.0, 0.0)

        val detailJson = jsonElement(
            """
            {
              "noBng": 1,
              "luasBng": 120,
              "jmlLantaiBng": 2,
              "kondisiBng": "Baik"
            }
            """.trimIndent(),
        )

        val detail = detailJson.toBuildingDetailOrNull(demoNop, fallbackNoBng = "1")

        assertNotNull(detail)
        requireNotNull(detail)
        assertEquals("1", detail.noBng)
        assertEquals("Baik", detail.kondisi)

        val facilityJson = jsonElement(
            """
            { "rows": [ { "namaFasilitas": "AC", "jumlah": 2, "satuan": "unit" } ] }
            """.trimIndent(),
        )

        val facilities = facilityJson.toBuildingFacilities()

        assertEquals(1, facilities.size)
        assertEquals("AC", facilities.first().name)
        assertEquals("unit", facilities.first().unit)
    }

    @Test
    fun tc014_tc020_reportDraftValidation_allowsDraftButRequiresDescriptionForSimulation() {
        val draftValidation = ReportDraftFormValidator.validate(
            newBuildingAreaText = "135",
            newFloorCountText = "2",
            description = "",
            requireDescription = false,
        )

        assertTrue(draftValidation.isValid)

        val simulationValidation = ReportDraftFormValidator.validate(
            newBuildingAreaText = "135",
            newFloorCountText = "2",
            description = "",
            requireDescription = true,
        )

        assertFalse(simulationValidation.isValid)
        assertEquals("Deskripsi perubahan wajib diisi.", simulationValidation.descriptionError)
    }

    @Test
    fun tc015_maskingNik_neverReturnsFullNik() {
        val masked = "3404123456789012".maskNik()

        assertEquals("34************12", masked)
        assertFalse(masked.contains("041234567890"))
    }

    @Test
    fun tc017_paymentInfoRequest_keepsSpptYearAndDoesNotUseWriteEndpointPayload() {
        val encoded = SimpbbApiClient.json.encodeToString(
            OrpcRequest(SpptGetRequest.fromDomain(demoNop, thnPajakSppt = 2025)),
        )

        assertTrue(encoded.contains("\"thnPajakSppt\":2025"))
        assertTrue(encoded.contains("\"noUrut\":\"0001\""))
        assertFalse(encoded.contains("objekPajak/save"))
        assertFalse(encoded.contains("paymentGateway"))
        assertFalse(encoded.contains("qr"))
    }

    @Test
    fun tc009_buildingRequest_sendsNoBngAsNumberForLiveApiCompatibility() {
        val encoded = SimpbbApiClient.json.encodeToString(
            OrpcRequest(BuildingRequest.fromDomain(demoNop, noBng = "1")),
        )

        assertTrue(encoded.contains("\"noBng\":1"))
        assertFalse(encoded.contains("\"noBng\":\"1\""))
    }

    @Test
    fun tc023_tc024_apiErrorAndNoConnection_mapToUserReadableMessages() {
        val noConnection = IOException("host unreachable").toAppError()
        val empty = AppError.EmptyResponse.toUserMessage()

        assertEquals(AppError.NetworkUnavailable, noConnection)
        assertEquals(
            "Koneksi internet tidak tersedia atau server tidak dapat dijangkau.",
            noConnection.toUserMessage(),
        )
        assertEquals("Data tidak tersedia.", empty)
    }

    private fun jsonElement(value: String): JsonElement {
        return SimpbbApiClient.json.decodeFromString(value)
    }
}
