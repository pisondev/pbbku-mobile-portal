package id.pbbku.mobileportal.data.repository

import id.pbbku.mobileportal.core.error.AppError
import id.pbbku.mobileportal.core.error.toAppError
import id.pbbku.mobileportal.core.error.toUserMessage
import id.pbbku.mobileportal.core.result.AppResult
import id.pbbku.mobileportal.data.api.SimpbbApiConfig
import id.pbbku.mobileportal.data.api.SimpbbApiService
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
import id.pbbku.mobileportal.domain.model.ApiPayload
import id.pbbku.mobileportal.domain.model.Nop
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull

class SimpbbRepository(
    private val apiService: SimpbbApiService,
) : RepositoryMarker {
    suspend fun searchObjekPajak(
        query: String,
        limit: Int? = null,
    ): AppResult<ApiPayload> {
        return execute(SimpbbApiConfig.Endpoint.OBJEK_PAJAK_SEARCH) {
            apiService.searchObjekPajak(
                OrpcRequest(ObjekPajakSearchRequest(query = query, limit = limit)),
            )
        }
    }

    suspend fun listObjekPajakDetails(
        kdPropinsi: String? = null,
        kdDati2: String? = null,
        limit: Int,
        offset: Int,
        search: String? = null,
    ): AppResult<ApiPayload> {
        return execute(SimpbbApiConfig.Endpoint.OBJEK_PAJAK_LIST_DETAILS) {
            apiService.listObjekPajakDetails(
                OrpcRequest(
                    ObjekPajakListDetailsRequest(
                        kdPropinsi = kdPropinsi,
                        kdDati2 = kdDati2,
                        limit = limit,
                        offset = offset,
                        search = search,
                    ),
                ),
            )
        }
    }

    suspend fun getObjekPajakByNop(nop: Nop): AppResult<ApiPayload> {
        return executeNop(SimpbbApiConfig.Endpoint.OBJEK_PAJAK_GET_BY_NOP, nop) {
            apiService.getObjekPajakByNop(OrpcRequest(NopRequest.fromDomain(nop)))
        }
    }

    suspend fun getSpptHistoryByNop(nop: Nop): AppResult<ApiPayload> {
        return executeNop(SimpbbApiConfig.Endpoint.OBJEK_PAJAK_GET_SPPT_HISTORY, nop) {
            apiService.getSpptHistoryByNop(OrpcRequest(NopRequest.fromDomain(nop)))
        }
    }

    suspend fun getTunggakanByNop(nop: Nop): AppResult<ApiPayload> {
        return executeNop(SimpbbApiConfig.Endpoint.OBJEK_PAJAK_GET_TUNGGAKAN, nop) {
            apiService.getTunggakanByNop(OrpcRequest(NopRequest.fromDomain(nop)))
        }
    }

    suspend fun listBangunanByNop(nop: Nop): AppResult<ApiPayload> {
        return executeNop(SimpbbApiConfig.Endpoint.LSPOP_LIST_BY_NOP, nop) {
            apiService.listBangunanByNop(OrpcRequest(NopRequest.fromDomain(nop)))
        }
    }

    suspend fun getBuilding(nop: Nop, noBng: String): AppResult<ApiPayload> {
        return execute(SimpbbApiConfig.Endpoint.LSPOP_GET_BUILDING) {
            apiService.getBuilding(OrpcRequest(BuildingRequest.fromDomain(nop, noBng)))
        }
    }

    suspend fun listFasilitas(nop: Nop, noBng: String): AppResult<ApiPayload> {
        return execute(SimpbbApiConfig.Endpoint.LSPOP_LIST_FASILITAS) {
            apiService.listFasilitas(OrpcRequest(BuildingRequest.fromDomain(nop, noBng)))
        }
    }

    suspend fun listSpptByNop(nop: Nop): AppResult<ApiPayload> {
        return executeNop(SimpbbApiConfig.Endpoint.SPPT_LIST_BY_NOP, nop) {
            apiService.listSpptByNop(OrpcRequest(NopRequest.fromDomain(nop)))
        }
    }

    suspend fun getSppt(nop: Nop, thnPajakSppt: Int): AppResult<ApiPayload> {
        return execute(SimpbbApiConfig.Endpoint.SPPT_GET) {
            apiService.getSppt(OrpcRequest(SpptGetRequest.fromDomain(nop, thnPajakSppt)))
        }
    }

    suspend fun listSppt(
        thnPajak: Int? = null,
        kdPropinsi: String? = null,
        statusPembayaran: String? = null,
        limit: Int,
        offset: Int,
    ): AppResult<ApiPayload> {
        return execute(SimpbbApiConfig.Endpoint.SPPT_LIST) {
            apiService.listSppt(
                OrpcRequest(
                    SpptListRequest(
                        thnPajak = thnPajak,
                        kdPropinsi = kdPropinsi,
                        statusPembayaran = statusPembayaran,
                        limit = limit,
                        offset = offset,
                    ),
                ),
            )
        }
    }

    suspend fun listPropinsi(): AppResult<ApiPayload> {
        return execute(SimpbbApiConfig.Endpoint.WILAYAH_LIST_PROPINSI) {
            apiService.listPropinsi(OrpcRequest(EmptyRequest()))
        }
    }

    suspend fun listDati2(kdPropinsi: String): AppResult<ApiPayload> {
        return execute(SimpbbApiConfig.Endpoint.WILAYAH_LIST_DATI2) {
            apiService.listDati2(OrpcRequest(WilayahDati2Request(kdPropinsi)))
        }
    }

    suspend fun listKecamatan(
        kdPropinsi: String,
        kdDati2: String,
    ): AppResult<ApiPayload> {
        return execute(SimpbbApiConfig.Endpoint.WILAYAH_LIST_KECAMATAN) {
            apiService.listKecamatan(OrpcRequest(WilayahKecamatanRequest(kdPropinsi, kdDati2)))
        }
    }

    suspend fun listKelurahan(
        kdPropinsi: String,
        kdDati2: String,
        kdKecamatan: String,
    ): AppResult<ApiPayload> {
        return execute(SimpbbApiConfig.Endpoint.WILAYAH_LIST_KELURAHAN) {
            apiService.listKelurahan(
                OrpcRequest(WilayahKelurahanRequest(kdPropinsi, kdDati2, kdKecamatan)),
            )
        }
    }

    suspend fun listBlok(
        kdPropinsi: String,
        kdDati2: String,
        kdKecamatan: String,
        kdKelurahan: String,
    ): AppResult<ApiPayload> {
        return execute(SimpbbApiConfig.Endpoint.WILAYAH_LIST_BLOK) {
            apiService.listBlok(
                OrpcRequest(WilayahBlokRequest(kdPropinsi, kdDati2, kdKecamatan, kdKelurahan)),
            )
        }
    }

    private suspend fun executeNop(
        endpoint: String,
        nop: Nop,
        block: suspend () -> OrpcResponse<JsonElement>,
    ): AppResult<ApiPayload> {
        val result = execute(endpoint, block)
        return if (nop.asDisplayText().length == Nop.FULL_LENGTH) {
            result
        } else {
            AppResult.Error("Format NOP tidak valid.")
        }
    }

    private suspend fun execute(
        endpoint: String,
        block: suspend () -> OrpcResponse<JsonElement>,
    ): AppResult<ApiPayload> {
        return try {
            val response = block()
            val payload = response.json
            if (payload == null || payload is JsonNull) {
                AppResult.Empty
            } else {
                AppResult.Success(ApiPayload(endpoint = endpoint, json = payload))
            }
        } catch (throwable: Throwable) {
            val appError = throwable.toAppError()
            val message = if (appError == AppError.EmptyResponse) {
                AppError.EmptyResponse.toUserMessage()
            } else {
                appError.toUserMessage()
            }
            AppResult.Error(message = message, cause = throwable)
        }
    }
}
