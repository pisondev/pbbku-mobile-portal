package id.pbbku.mobileportal.data.repository

import id.pbbku.mobileportal.core.result.AppResult
import id.pbbku.mobileportal.data.mapper.toWilayahItems
import id.pbbku.mobileportal.domain.model.ApiPayload
import id.pbbku.mobileportal.domain.model.WilayahItem
import id.pbbku.mobileportal.domain.model.WilayahLevel

class WilayahRepository(
    private val simpbbRepository: SimpbbRepository,
) : RepositoryMarker {
    private val cache = mutableMapOf<String, List<WilayahItem>>()

    suspend fun listPropinsi(): AppResult<List<WilayahItem>> {
        return cached("propinsi") {
            simpbbRepository.listPropinsi().toWilayahResult(WilayahLevel.PROPINSI)
        }
    }

    suspend fun listDati2(kdPropinsi: String): AppResult<List<WilayahItem>> {
        return cached("dati2:$kdPropinsi") {
            simpbbRepository.listDati2(kdPropinsi).toWilayahResult(WilayahLevel.DATI2)
        }
    }

    suspend fun listKecamatan(
        kdPropinsi: String,
        kdDati2: String,
    ): AppResult<List<WilayahItem>> {
        return cached("kecamatan:$kdPropinsi:$kdDati2") {
            simpbbRepository
                .listKecamatan(kdPropinsi, kdDati2)
                .toWilayahResult(WilayahLevel.KECAMATAN)
        }
    }

    suspend fun listKelurahan(
        kdPropinsi: String,
        kdDati2: String,
        kdKecamatan: String,
    ): AppResult<List<WilayahItem>> {
        return cached("kelurahan:$kdPropinsi:$kdDati2:$kdKecamatan") {
            simpbbRepository
                .listKelurahan(kdPropinsi, kdDati2, kdKecamatan)
                .toWilayahResult(WilayahLevel.KELURAHAN)
        }
    }

    suspend fun listBlok(
        kdPropinsi: String,
        kdDati2: String,
        kdKecamatan: String,
        kdKelurahan: String,
    ): AppResult<List<WilayahItem>> {
        return cached("blok:$kdPropinsi:$kdDati2:$kdKecamatan:$kdKelurahan") {
            simpbbRepository
                .listBlok(kdPropinsi, kdDati2, kdKecamatan, kdKelurahan)
                .toWilayahResult(WilayahLevel.BLOK)
        }
    }

    private suspend fun cached(
        key: String,
        load: suspend () -> AppResult<List<WilayahItem>>,
    ): AppResult<List<WilayahItem>> {
        cache[key]?.let { return AppResult.Success(it) }
        val result = load()
        if (result is AppResult.Success) cache[key] = result.data
        return result
    }

    private fun AppResult<ApiPayload>.toWilayahResult(
        level: WilayahLevel,
    ): AppResult<List<WilayahItem>> {
        return when (this) {
            AppResult.Empty -> AppResult.Empty
            is AppResult.Error -> AppResult.Error(message = message, cause = cause)
            AppResult.Loading -> AppResult.Loading
            is AppResult.Success -> {
                val rows = data.json.toWilayahItems(level)
                if (rows.isEmpty()) AppResult.Empty else AppResult.Success(rows)
            }
        }
    }
}
