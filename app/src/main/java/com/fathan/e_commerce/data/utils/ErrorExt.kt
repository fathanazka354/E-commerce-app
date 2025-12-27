package com.fathan.e_commerce.data.utils

/**
 * Base sealed class untuk semua Chat-related exceptions
 */
sealed class ErrorException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {

    /**
     * Network-related errors
     */
    sealed class NetworkException(message: String, cause: Throwable? = null) : ErrorException(message, cause) {
        class NoInternetConnection : NetworkException("No internet connection available")
        class Timeout : NetworkException("Request timeout. Please try again")
        class UnknownHost : NetworkException("Cannot reach server. Please check your connection")
        class SSLError : NetworkException("SSL/TLS error occurred")
    }

    /**
     * HTTP errors
     */
    sealed class HttpException(
        val code: Int,
        message: String,
        cause: Throwable? = null
    ) : ErrorException(message, cause) {

        // 4xx Client errors
        class BadRequest(message: String = "Invalid request") : HttpException(400, message)
        class Unauthorized(message: String = "Authentication required") : HttpException(401, message)
        class Forbidden(message: String = "You don't have permission to access this") : HttpException(403, message)
        class NotFound(message: String = "Resource not found") : HttpException(404, message)
        class RequestTimeout(message: String = "Request timeout") : HttpException(408, message)
        class Conflict(message: String = "Conflict occurred") : HttpException(409, message)
        class TooManyRequests(message: String = "Too many requests. Please try again later") : HttpException(429, message)

        // 5xx Server errors
        class InternalServerError(message: String = "Server error. Please try again later") : HttpException(500, message)
        class BadGateway(message: String = "Bad gateway") : HttpException(502, message)
        class ServiceUnavailable(message: String = "Service unavailable. Please try again later") : HttpException(503, message)
        class GatewayTimeout(message: String = "Gateway timeout") : HttpException(504, message)

        // Generic
        class Unknown(code: Int, message: String = "HTTP error $code occurred") : HttpException(code, message)
    }

    /**
     * Authentication & Authorization errors
     */
    sealed class AuthException(message: String, cause: Throwable? = null) : ErrorException(message, cause) {
        class NotAuthenticated : AuthException("User not authenticated. Please login")
        class SessionExpired : AuthException("Session expired. Please login again")
        class InvalidCredentials : AuthException("Invalid credentials")
        class TokenExpired : AuthException("Token expired. Please login again")
    }

    /**
     * Data/Business logic errors
     */
    sealed class DataException(message: String, cause: Throwable? = null) : ErrorException(message, cause) {
        class EmptyResponse : DataException("No data received from server")
        class InvalidData : DataException("Invalid data format")
        class ParseError(cause: Throwable? = null) : DataException("Failed to parse data", cause)
        class ConversationNotFound : DataException("Conversation not found")
        class MessageNotFound : DataException("Message not found")
        class UserNotFound : DataException("User not found")
    }

    /**
     * Validation errors
     */
    sealed class ValidationException(message: String) : ErrorException(message) {
        class EmptyMessage : ValidationException("Message cannot be empty")
        class MessageTooLong(maxLength: Int) : ValidationException("Message too long. Maximum $maxLength characters")
        class InvalidConversationId : ValidationException("Invalid conversation ID")
        class InvalidUserId : ValidationException("Invalid user ID")
        class InvalidMessageType : ValidationException("Invalid message type")
    }

    /**
     * Realtime errors
     */
    sealed class RealtimeException(message: String, cause: Throwable? = null) : ErrorException(message, cause) {
        class ConnectionFailed : RealtimeException("Failed to connect to realtime server")
        class SubscriptionFailed : RealtimeException("Failed to subscribe to channel")
        class ChannelClosed : RealtimeException("Channel was closed")
        class MessageDeliveryFailed : RealtimeException("Failed to deliver message")
    }

    /**
     * Storage errors (untuk file upload jika diperlukan)
     */
    sealed class StorageException(message: String, cause: Throwable? = null) : ErrorException(message, cause) {
        class UploadFailed : StorageException("Failed to upload file")
        class DownloadFailed : StorageException("Failed to download file")
        class FileNotFound : StorageException("File not found")
        class FileTooLarge(maxSize: Long) : StorageException("File too large. Maximum size: ${maxSize / 1024 / 1024}MB")
        class InvalidFileType : StorageException("Invalid file type")
    }

    /**
     * Generic/Unknown error
     */
    class Unknown(message: String = "An unknown error occurred", cause: Throwable? = null) : ErrorException(message, cause)
}

/**
 * Extension function untuk mendapatkan user-friendly error message
 */
fun ErrorException.getUserMessage(): String {
    return when (this) {
        // Network
        is ErrorException.NetworkException.NoInternetConnection ->
            "Tidak ada koneksi internet. Periksa koneksi Anda"
        is ErrorException.NetworkException.Timeout ->
            "Koneksi timeout. Silakan coba lagi"
        is ErrorException.NetworkException.UnknownHost ->
            "Tidak dapat terhubung ke server"
        is ErrorException.NetworkException.SSLError ->
            "Terjadi kesalahan keamanan koneksi"

        // HTTP
        is ErrorException.HttpException.BadRequest ->
            "Permintaan tidak valid"
        is ErrorException.HttpException.Unauthorized ->
            "Sesi Anda telah berakhir. Silakan login kembali"
        is ErrorException.HttpException.Forbidden ->
            "Anda tidak memiliki akses ke fitur ini"
        is ErrorException.HttpException.NotFound ->
            "Data tidak ditemukan"
        is ErrorException.HttpException.RequestTimeout ->
            "Permintaan timeout. Silakan coba lagi"
        is ErrorException.HttpException.Conflict ->
            "Terjadi konflik. Silakan refresh dan coba lagi"
        is ErrorException.HttpException.TooManyRequests ->
            "Terlalu banyak permintaan. Tunggu sebentar"
        is ErrorException.HttpException.InternalServerError ->
            "Terjadi kesalahan pada server. Silakan coba lagi"
        is ErrorException.HttpException.ServiceUnavailable ->
            "Layanan sedang tidak tersedia. Silakan coba lagi nanti"

        // Auth
        is ErrorException.AuthException.NotAuthenticated ->
            "Anda belum login. Silakan login terlebih dahulu"
        is ErrorException.AuthException.SessionExpired ->
            "Sesi Anda telah berakhir. Silakan login kembali"
        is ErrorException.AuthException.InvalidCredentials ->
            "Kredensial tidak valid"
        is ErrorException.AuthException.TokenExpired ->
            "Token telah kadaluarsa. Silakan login kembali"

        // Data
        is ErrorException.DataException.EmptyResponse ->
            "Tidak ada data"
        is ErrorException.DataException.InvalidData ->
            "Format data tidak valid"
        is ErrorException.DataException.ParseError ->
            "Gagal memproses data"
        is ErrorException.DataException.ConversationNotFound ->
            "Percakapan tidak ditemukan"
        is ErrorException.DataException.MessageNotFound ->
            "Pesan tidak ditemukan"
        is ErrorException.DataException.UserNotFound ->
            "Pengguna tidak ditemukan"

        // Validation
        is ErrorException.ValidationException.EmptyMessage ->
            "Pesan tidak boleh kosong"
        is ErrorException.ValidationException.MessageTooLong ->
            this.message ?: "Pesan terlalu panjang"
        is ErrorException.ValidationException.InvalidConversationId ->
            "ID percakapan tidak valid"
        is ErrorException.ValidationException.InvalidUserId ->
            "ID pengguna tidak valid"
        is ErrorException.ValidationException.InvalidMessageType ->
            "Tipe pesan tidak valid"

        // Realtime
        is ErrorException.RealtimeException.ConnectionFailed ->
            "Gagal terhubung ke server realtime"
        is ErrorException.RealtimeException.SubscriptionFailed ->
            "Gagal berlangganan channel"
        is ErrorException.RealtimeException.ChannelClosed ->
            "Koneksi terputus"
        is ErrorException.RealtimeException.MessageDeliveryFailed ->
            "Gagal mengirim pesan"

        // Storage
        is ErrorException.StorageException.UploadFailed ->
            "Gagal mengunggah file"
        is ErrorException.StorageException.DownloadFailed ->
            "Gagal mengunduh file"
        is ErrorException.StorageException.FileNotFound ->
            "File tidak ditemukan"
        is ErrorException.StorageException.FileTooLarge ->
            this.message ?: "File terlalu besar"
        is ErrorException.StorageException.InvalidFileType ->
            "Tipe file tidak didukung"

        // Unknown
        is ErrorException.Unknown ->
            this.message ?: "Terjadi kesalahan"

        else -> this.message ?: "Terjadi kesalahan"
    }
}

/**
 * Extension function untuk checking error type
 */
fun ErrorException.isNetworkError(): Boolean {
    return this is ErrorException.NetworkException
}

fun ErrorException.isAuthError(): Boolean {
    return this is ErrorException.AuthException
}

fun ErrorException.isServerError(): Boolean {
    return this is ErrorException.HttpException &&
            (this is ErrorException.HttpException.InternalServerError ||
                    this is ErrorException.HttpException.BadGateway ||
                    this is ErrorException.HttpException.ServiceUnavailable ||
                    this is ErrorException.HttpException.GatewayTimeout)
}

fun ErrorException.shouldRetry(): Boolean {
    return when (this) {
        is ErrorException.NetworkException.Timeout,
        is ErrorException.HttpException.RequestTimeout,
        is ErrorException.HttpException.TooManyRequests,
        is ErrorException.HttpException.InternalServerError,
        is ErrorException.HttpException.BadGateway,
        is ErrorException.HttpException.ServiceUnavailable,
        is ErrorException.HttpException.GatewayTimeout -> true
        else -> false
    }
}