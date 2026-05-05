package id.pbbku.mobileportal.data.dto

import id.pbbku.mobileportal.data.api.SimpbbApiClient
import id.pbbku.mobileportal.domain.model.Nop
import kotlinx.serialization.encodeToString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OrpcEnvelopeTest {
    @Test
    fun nopRequest_serializesWithJsonWrapperAndStringSegments() {
        val nop = requireNotNull(Nop.parseOrNull("32.04.010.001.001.0001.0"))
        val request = OrpcRequest(NopRequest.fromDomain(nop))

        val encoded = SimpbbApiClient.json.encodeToString(request)

        assertTrue(encoded.contains("\"json\""))
        assertTrue(encoded.contains("\"kdKecamatan\":\"010\""))
        assertTrue(encoded.contains("\"kdKelurahan\":\"001\""))
        assertTrue(encoded.contains("\"kdBlok\":\"001\""))
        assertTrue(encoded.contains("\"noUrut\":\"0001\""))
    }

    @Test
    fun emptyRequest_serializesAsEmptyJsonObjectInsideWrapper() {
        val encoded = SimpbbApiClient.json.encodeToString(OrpcRequest(EmptyRequest()))

        assertEquals("{\"json\":{}}", encoded)
    }
}
