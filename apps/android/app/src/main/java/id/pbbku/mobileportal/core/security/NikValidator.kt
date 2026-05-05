package id.pbbku.mobileportal.core.security

fun String.isValidNik(): Boolean {
    return length == 16 && all(Char::isDigit)
}
