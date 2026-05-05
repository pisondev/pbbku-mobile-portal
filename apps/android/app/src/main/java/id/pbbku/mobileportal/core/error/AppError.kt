package id.pbbku.mobileportal.core.error

import java.io.IOException
import java.net.SocketTimeoutException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

sealed interface AppError {
    data object NetworkUnavailable : AppError
    data object Timeout : AppError
    data object EmptyResponse : AppError
    data class Http(val code: Int, val rawMessage: String?) : AppError
    data class Parse(val rawMessage: String?) : AppError
    data class Unknown(val rawMessage: String?) : AppError
}

fun Throwable.toAppError(): AppError {
    return when (this) {
        is SocketTimeoutException -> AppError.Timeout
        is IOException -> AppError.NetworkUnavailable
        is HttpException -> AppError.Http(code(), message())
        is SerializationException -> AppError.Parse(message)
        else -> AppError.Unknown(message)
    }
}

fun AppError.toUserMessage(): String {
    return when (this) {
        AppError.NetworkUnavailable -> "Koneksi internet tidak tersedia atau server tidak dapat dijangkau."
        AppError.Timeout -> "Request terlalu lama. Coba ulang beberapa saat lagi."
        AppError.EmptyResponse -> "Data tidak tersedia."
        is AppError.Http -> "Server mengembalikan error ${code}."
        is AppError.Parse -> "Format data dari server belum dapat dibaca aplikasi."
        is AppError.Unknown -> rawMessage ?: "Terjadi kesalahan tidak diketahui."
    }
}
