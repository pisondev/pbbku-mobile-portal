package id.pbbku.mobileportal.core.result

sealed interface AppResult<out T> {
    data object Loading : AppResult<Nothing>
    data class Success<T>(val data: T) : AppResult<T>
    data object Empty : AppResult<Nothing>
    data class Error(val message: String, val cause: Throwable? = null) : AppResult<Nothing>
}
