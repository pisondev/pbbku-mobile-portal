package id.pbbku.mobileportal.feature.nonfunctional

import id.pbbku.mobileportal.core.error.AppError
import id.pbbku.mobileportal.core.error.toUserMessage
import id.pbbku.mobileportal.core.format.toIndonesianDateText
import id.pbbku.mobileportal.core.format.toPaymentStatusText
import id.pbbku.mobileportal.core.format.toRupiahText
import id.pbbku.mobileportal.data.api.SimpbbApiClient
import id.pbbku.mobileportal.data.api.SimpbbApiConfig
import id.pbbku.mobileportal.data.dto.ObjekPajakListDetailsRequest
import id.pbbku.mobileportal.data.dto.ObjekPajakSearchRequest
import id.pbbku.mobileportal.data.dto.OrpcRequest
import id.pbbku.mobileportal.data.dto.SpptListRequest
import id.pbbku.mobileportal.data.mapper.toBuildingSummaries
import id.pbbku.mobileportal.data.mapper.toObjekPajakSearchRows
import id.pbbku.mobileportal.data.mapper.toTaxBillSummaries
import id.pbbku.mobileportal.domain.model.Nop
import id.pbbku.mobileportal.domain.model.PaymentStatus
import id.pbbku.mobileportal.feature.search.SearchConfig
import java.time.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonElement
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NonFunctionalContractTest {
    private val demoNop = requireNotNull(Nop.parseOrNull("32.04.010.001.001.0001.0"))

    @Test
    fun nf_searchUsesDebounceMinimumLengthAndBoundedLimit() {
        assertEquals(500L, SearchConfig.SEARCH_DEBOUNCE_MS)
        assertEquals(3, SearchConfig.MIN_QUERY_LENGTH)
        assertEquals(10, SearchConfig.PAGE_SIZE)
    }

    @Test
    fun nf_searchAndListRequestsUseLimitAndPagination() {
        val searchEncoded = SimpbbApiClient.json.encodeToString(
            OrpcRequest(ObjekPajakSearchRequest(query = "BUDI", limit = SearchConfig.PAGE_SIZE)),
        )
        val listEncoded = SimpbbApiClient.json.encodeToString(
            OrpcRequest(
                ObjekPajakListDetailsRequest(
                    kdPropinsi = "32",
                    kdDati2 = "04",
                    limit = SearchConfig.PAGE_SIZE,
                    offset = 20,
                    search = "BUDI",
                ),
            ),
        )

        assertTrue(searchEncoded.contains("\"limit\":10"))
        assertTrue(listEncoded.contains("\"limit\":10"))
        assertTrue(listEncoded.contains("\"offset\":20"))
    }

    @Test
    fun nf_spptListRequestSupportsPaginationForLargeData() {
        val encoded = SimpbbApiClient.json.encodeToString(
            OrpcRequest(
                SpptListRequest(
                    thnPajak = 2025,
                    kdPropinsi = "32",
                    statusPembayaran = "belum_lunas",
                    limit = 10,
                    offset = 30,
                ),
            ),
        )

        assertTrue(encoded.contains("\"limit\":10"))
        assertTrue(encoded.contains("\"offset\":30"))
        assertTrue(encoded.contains("\"thnPajak\":2025"))
    }

    @Test
    fun nf_baseUrlAndEndpointsAreCentralizedInConfig() {
        assertTrue(SimpbbApiConfig.BASE_URL.endsWith("/api/rpc/"))
        assertEquals("http://10.0.2.2:8080/api/rpc/", SimpbbApiConfig.DEFAULT_LOCAL_BASE_URL)
        assertEquals("https://pbbku-api.tierratie.com/api/rpc/", SimpbbApiConfig.DEFAULT_PRODUCTION_BASE_URL)
        assertEquals("objekPajak/search", SimpbbApiConfig.Endpoint.OBJEK_PAJAK_SEARCH)
        assertEquals("sppt/get", SimpbbApiConfig.Endpoint.SPPT_GET)
        assertEquals("wilayah/listPropinsi", SimpbbApiConfig.Endpoint.WILAYAH_LIST_PROPINSI)
    }

    @Test
    fun nf_androidRpcEndpointsMatchInternalApiRoutes() {
        val endpoints = listOf(
            "objekPajak/search",
            "objekPajak/listDetails",
            "objekPajak/getByNop",
            "objekPajak/getSpptHistory",
            "objekPajak/getTunggakan",
            "lspop/listByNop",
            "lspop/getBuilding",
            "lspop/listFasilitas",
            "sppt/listByNop",
            "sppt/get",
            "sppt/list",
            "wilayah/listPropinsi",
            "wilayah/listDati2",
            "wilayah/listKecamatan",
            "wilayah/listKelurahan",
            "wilayah/listBlok",
        )

        assertEquals(
            endpoints,
            listOf(
                SimpbbApiConfig.Endpoint.OBJEK_PAJAK_SEARCH,
                SimpbbApiConfig.Endpoint.OBJEK_PAJAK_LIST_DETAILS,
                SimpbbApiConfig.Endpoint.OBJEK_PAJAK_GET_BY_NOP,
                SimpbbApiConfig.Endpoint.OBJEK_PAJAK_GET_SPPT_HISTORY,
                SimpbbApiConfig.Endpoint.OBJEK_PAJAK_GET_TUNGGAKAN,
                SimpbbApiConfig.Endpoint.LSPOP_LIST_BY_NOP,
                SimpbbApiConfig.Endpoint.LSPOP_GET_BUILDING,
                SimpbbApiConfig.Endpoint.LSPOP_LIST_FASILITAS,
                SimpbbApiConfig.Endpoint.SPPT_LIST_BY_NOP,
                SimpbbApiConfig.Endpoint.SPPT_GET,
                SimpbbApiConfig.Endpoint.SPPT_LIST,
                SimpbbApiConfig.Endpoint.WILAYAH_LIST_PROPINSI,
                SimpbbApiConfig.Endpoint.WILAYAH_LIST_DATI2,
                SimpbbApiConfig.Endpoint.WILAYAH_LIST_KECAMATAN,
                SimpbbApiConfig.Endpoint.WILAYAH_LIST_KELURAHAN,
                SimpbbApiConfig.Endpoint.WILAYAH_LIST_BLOK,
            ),
        )
    }

    @Test
    fun nf_debugLoggingNeverUsesBodyLevel() {
        assertEquals(HttpLoggingInterceptor.Level.BASIC, SimpbbApiClient.loggingLevelFor(debug = true))
        assertEquals(HttpLoggingInterceptor.Level.NONE, SimpbbApiClient.loggingLevelFor(debug = false))
        assertFalse(SimpbbApiClient.loggingLevelFor(debug = true) == HttpLoggingInterceptor.Level.BODY)
    }

    @Test
    fun nf_rupiahDateAndStatusFormattingUseIndonesianDisplay() {
        assertEquals("Rp175.000,00", 175000L.toRupiahText())
        assertEquals("30 September 2025", LocalDate.of(2025, 9, 30).toIndonesianDateText())
        assertEquals("Lunas", PaymentStatus.PAID.toPaymentStatusText())
        assertEquals("Jatuh Tempo", PaymentStatus.OVERDUE.toPaymentStatusText())
    }

    @Test
    fun nf_emptyAndNullPayloadsDoNotCrashMappers() {
        val emptyArray = jsonElement("[]")
        val nullLikeObject = jsonElement("""{ "rows": null, "data": null }""")

        assertTrue(emptyArray.toObjekPajakSearchRows().isEmpty())
        assertTrue(nullLikeObject.toObjekPajakSearchRows().isEmpty())
        assertTrue(emptyArray.toTaxBillSummaries(demoNop).isEmpty())
        assertTrue(nullLikeObject.toBuildingSummaries(demoNop).isEmpty())
    }

    @Test
    fun nf_internetUnavailableMessageIsUserReadable() {
        assertEquals(
            "Koneksi internet tidak tersedia atau server tidak dapat dijangkau.",
            AppError.NetworkUnavailable.toUserMessage(),
        )
        assertEquals("Request terlalu lama. Coba ulang beberapa saat lagi.", AppError.Timeout.toUserMessage())
    }

    @Test
    fun nf_demoAndPrototypeLabelsStayExplicitInUserVisibleText() {
        val simulationLabel = "PROTOTIPE - bukan bukti pembayaran resmi."
        val reminderNote = "Pengingat prioritas ditampilkan sambil menunggu data jatuh tempo SPPT lengkap."
        val reportNote = "Draft prototipe lokal. Perubahan data resmi tetap memerlukan verifikasi petugas Bapenda."

        assertTrue(simulationLabel.contains("PROTOTIPE"))
        assertTrue(reminderNote.contains("prioritas"))
        assertTrue(reportNote.contains("prototipe lokal"))
        assertTrue(reportNote.contains("verifikasi petugas Bapenda"))
    }

    private fun jsonElement(value: String): JsonElement {
        return SimpbbApiClient.json.decodeFromString(value)
    }
}
