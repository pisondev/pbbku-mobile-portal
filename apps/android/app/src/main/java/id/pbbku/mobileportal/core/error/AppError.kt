package id.pbbku.mobileportal.core.error

sealed interface AppError {
    data object NetworkUnavailable : AppError
    data object EmptyResponse : AppError
    data class Unknown(val message: String) : AppError
}
