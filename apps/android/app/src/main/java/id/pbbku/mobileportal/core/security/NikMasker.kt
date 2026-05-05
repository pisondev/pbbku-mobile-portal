package id.pbbku.mobileportal.core.security

fun String.maskNik(): String {
    val digits = filter(Char::isDigit)
    if (digits.length < 4) return "****"
    val prefix = digits.take(2)
    val suffix = digits.takeLast(2)
    return prefix + "*".repeat((digits.length - 4).coerceAtLeast(0)) + suffix
}
