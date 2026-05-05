package id.pbbku.mobileportal.data.api

import id.pbbku.mobileportal.data.dto.BuildingRequest
import id.pbbku.mobileportal.data.dto.EmptyRequest
import id.pbbku.mobileportal.data.dto.NopRequest
import id.pbbku.mobileportal.data.dto.ObjekPajakListDetailsRequest
import id.pbbku.mobileportal.data.dto.ObjekPajakSearchRequest
import id.pbbku.mobileportal.data.dto.OrpcRequest
import id.pbbku.mobileportal.data.dto.OrpcResponse
import id.pbbku.mobileportal.data.dto.SpptGetRequest
import id.pbbku.mobileportal.data.dto.SpptListRequest
import id.pbbku.mobileportal.data.dto.WilayahBlokRequest
import id.pbbku.mobileportal.data.dto.WilayahDati2Request
import id.pbbku.mobileportal.data.dto.WilayahKecamatanRequest
import id.pbbku.mobileportal.data.dto.WilayahKelurahanRequest
import kotlinx.serialization.json.JsonElement
import retrofit2.http.Body
import retrofit2.http.POST

interface SimpbbApiService {
    @POST(SimpbbApiConfig.Endpoint.OBJEK_PAJAK_SEARCH)
    suspend fun searchObjekPajak(
        @Body body: OrpcRequest<ObjekPajakSearchRequest>,
    ): OrpcResponse<JsonElement>

    @POST(SimpbbApiConfig.Endpoint.OBJEK_PAJAK_LIST_DETAILS)
    suspend fun listObjekPajakDetails(
        @Body body: OrpcRequest<ObjekPajakListDetailsRequest>,
    ): OrpcResponse<JsonElement>

    @POST(SimpbbApiConfig.Endpoint.OBJEK_PAJAK_GET_BY_NOP)
    suspend fun getObjekPajakByNop(
        @Body body: OrpcRequest<NopRequest>,
    ): OrpcResponse<JsonElement>

    @POST(SimpbbApiConfig.Endpoint.OBJEK_PAJAK_GET_SPPT_HISTORY)
    suspend fun getSpptHistoryByNop(
        @Body body: OrpcRequest<NopRequest>,
    ): OrpcResponse<JsonElement>

    @POST(SimpbbApiConfig.Endpoint.OBJEK_PAJAK_GET_TUNGGAKAN)
    suspend fun getTunggakanByNop(
        @Body body: OrpcRequest<NopRequest>,
    ): OrpcResponse<JsonElement>

    @POST(SimpbbApiConfig.Endpoint.LSPOP_LIST_BY_NOP)
    suspend fun listBangunanByNop(
        @Body body: OrpcRequest<NopRequest>,
    ): OrpcResponse<JsonElement>

    @POST(SimpbbApiConfig.Endpoint.LSPOP_GET_BUILDING)
    suspend fun getBuilding(
        @Body body: OrpcRequest<BuildingRequest>,
    ): OrpcResponse<JsonElement>

    @POST(SimpbbApiConfig.Endpoint.LSPOP_LIST_FASILITAS)
    suspend fun listFasilitas(
        @Body body: OrpcRequest<BuildingRequest>,
    ): OrpcResponse<JsonElement>

    @POST(SimpbbApiConfig.Endpoint.SPPT_LIST_BY_NOP)
    suspend fun listSpptByNop(
        @Body body: OrpcRequest<NopRequest>,
    ): OrpcResponse<JsonElement>

    @POST(SimpbbApiConfig.Endpoint.SPPT_GET)
    suspend fun getSppt(
        @Body body: OrpcRequest<SpptGetRequest>,
    ): OrpcResponse<JsonElement>

    @POST(SimpbbApiConfig.Endpoint.SPPT_LIST)
    suspend fun listSppt(
        @Body body: OrpcRequest<SpptListRequest>,
    ): OrpcResponse<JsonElement>

    @POST(SimpbbApiConfig.Endpoint.WILAYAH_LIST_PROPINSI)
    suspend fun listPropinsi(
        @Body body: OrpcRequest<EmptyRequest>,
    ): OrpcResponse<JsonElement>

    @POST(SimpbbApiConfig.Endpoint.WILAYAH_LIST_DATI2)
    suspend fun listDati2(
        @Body body: OrpcRequest<WilayahDati2Request>,
    ): OrpcResponse<JsonElement>

    @POST(SimpbbApiConfig.Endpoint.WILAYAH_LIST_KECAMATAN)
    suspend fun listKecamatan(
        @Body body: OrpcRequest<WilayahKecamatanRequest>,
    ): OrpcResponse<JsonElement>

    @POST(SimpbbApiConfig.Endpoint.WILAYAH_LIST_KELURAHAN)
    suspend fun listKelurahan(
        @Body body: OrpcRequest<WilayahKelurahanRequest>,
    ): OrpcResponse<JsonElement>

    @POST(SimpbbApiConfig.Endpoint.WILAYAH_LIST_BLOK)
    suspend fun listBlok(
        @Body body: OrpcRequest<WilayahBlokRequest>,
    ): OrpcResponse<JsonElement>
}
