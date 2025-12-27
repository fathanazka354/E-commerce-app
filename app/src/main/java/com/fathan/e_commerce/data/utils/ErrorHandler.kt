package com.fathan.e_commerce.data.utils

import android.util.Log
import io.github.jan.supabase.exceptions.BadRequestRestException
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.exceptions.UnauthorizedRestException
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.SerializationException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * Utility object untuk handle dan mapping exceptions ke ErrorException
 */
object ErrorHandler {

    private const val TAG = "ErrorHandler"

    /**
     * Main function untuk handle exception dan convert ke ErrorException
     */
    fun handleException(throwable: Throwable): ErrorException {
        Log.e(TAG, "Handling exception: ${throwable.javaClass.simpleName}", throwable)

        return when (throwable) {
            // Already ErrorException
            is ErrorException -> throwable

            // Network errors
            is UnknownHostException -> ErrorException.NetworkException.UnknownHost()
            is ConnectException -> ErrorException.NetworkException.NoInternetConnection()
            is SocketException -> ErrorException.NetworkException.NoInternetConnection()
            is SocketTimeoutException -> ErrorException.NetworkException.Timeout()
            is ConnectTimeoutException -> ErrorException.NetworkException.Timeout()
            is HttpRequestTimeoutException -> ErrorException.NetworkException.Timeout()
            is SSLException -> ErrorException.NetworkException.SSLError()
            is IOException -> ErrorException.NetworkException.NoInternetConnection()

            // Supabase-specific exceptions
            is UnauthorizedRestException -> handleUnauthorizedException(throwable)
            is BadRequestRestException -> handleBadRequestException(throwable)
            is RestException -> handleRestException(throwable)
            is HttpRequestException -> handleHttpRequestException(throwable)

            // Ktor HTTP exceptions
            is ClientRequestException -> handleClientException(throwable)
            is ServerResponseException -> handleServerException(throwable)

            // Serialization errors
            is SerializationException -> ErrorException.DataException.ParseError(throwable)

            // Generic/Unknown
            else -> ErrorException.Unknown(
                message = throwable.message ?: "Unknown error occurred",
                cause = throwable
            )
        }
    }

    /**
     * Handle Supabase UnauthorizedRestException
     */
    private fun handleUnauthorizedException(exception: UnauthorizedRestException): ErrorException {
        return when {
            exception.message?.contains("JWT", ignoreCase = true) == true ||
                    exception.message?.contains("token", ignoreCase = true) == true ->
                ErrorException.AuthException.TokenExpired()

            exception.message?.contains("expired", ignoreCase = true) == true ->
                ErrorException.AuthException.SessionExpired()

            else -> ErrorException.HttpException.Unauthorized()
        }
    }

    /**
     * Handle Supabase BadRequestRestException
     */
    private fun handleBadRequestException(exception: BadRequestRestException): ErrorException {
        val message = exception.message ?: ""

        return when {
            message.contains("conversation_id", ignoreCase = true) ->
                ErrorException.ValidationException.InvalidConversationId()

            message.contains("user_id", ignoreCase = true) ||
                    message.contains("buyer_id", ignoreCase = true) ||
                    message.contains("seller_id", ignoreCase = true) ->
                ErrorException.ValidationException.InvalidUserId()

            message.contains("message_content", ignoreCase = true) ->
                ErrorException.ValidationException.EmptyMessage()

            message.contains("message_type", ignoreCase = true) ->
                ErrorException.ValidationException.InvalidMessageType()

            else -> ErrorException.HttpException.BadRequest(message)
        }
    }

    /**
     * Handle generic Supabase RestException
     */
    private fun handleRestException(exception: RestException): ErrorException {
        return when (exception.statusCode) {
            HttpStatusCode.BadRequest.value ->
                ErrorException.HttpException.BadRequest(exception.message ?: "Bad request")

            HttpStatusCode.Unauthorized.value ->
                ErrorException.HttpException.Unauthorized()

            HttpStatusCode.Forbidden.value ->
                ErrorException.HttpException.Forbidden()

            HttpStatusCode.NotFound.value -> {
                // Check if it's a specific resource not found
                val message = exception.message ?: ""
                when {
                    message.contains("conversation", ignoreCase = true) ->
                        ErrorException.DataException.ConversationNotFound()
                    message.contains("message", ignoreCase = true) ->
                        ErrorException.DataException.MessageNotFound()
                    message.contains("user", ignoreCase = true) ->
                        ErrorException.DataException.UserNotFound()
                    else ->
                        ErrorException.HttpException.NotFound()
                }
            }

            HttpStatusCode.RequestTimeout.value ->
                ErrorException.HttpException.RequestTimeout()

            HttpStatusCode.Conflict.value ->
                ErrorException.HttpException.Conflict()

            429 -> // Too Many Requests
                ErrorException.HttpException.TooManyRequests()

            HttpStatusCode.InternalServerError.value ->
                ErrorException.HttpException.InternalServerError()

            HttpStatusCode.BadGateway.value ->
                ErrorException.HttpException.BadGateway()

            HttpStatusCode.ServiceUnavailable.value ->
                ErrorException.HttpException.ServiceUnavailable()

            HttpStatusCode.GatewayTimeout.value ->
                ErrorException.HttpException.GatewayTimeout()

            else ->
                ErrorException.HttpException.Unknown(
                    code = exception.statusCode,
                    message = exception.message ?: "HTTP error"
                )
        }
    }

    /**
     * Handle Supabase HttpRequestException
     */
    private fun handleHttpRequestException(exception: HttpRequestException): ErrorException {
        return ErrorException.Unknown(exception.message ?: "HTTP request failed", exception)
    }

    /**
     * Handle Ktor ClientRequestException (4xx errors)
     */
    private fun handleClientException(exception: ClientRequestException): ErrorException {
        return handleClientError(
            code = exception.response.status.value,
            message = exception.message
        )
    }

    /**
     * Handle Ktor ServerResponseException (5xx errors)
     */
    private fun handleServerException(exception: ServerResponseException): ErrorException {
        return handleServerError(
            code = exception.response.status.value,
            message = exception.message
        )
    }

    /**
     * Handle 4xx client errors
     */
    private fun handleClientError(code: Int, message: String?): ErrorException {
        return when (code) {
            400 -> ErrorException.HttpException.BadRequest(message ?: "Bad request")
            401 -> ErrorException.HttpException.Unauthorized()
            403 -> ErrorException.HttpException.Forbidden()
            404 -> ErrorException.HttpException.NotFound()
            408 -> ErrorException.HttpException.RequestTimeout()
            409 -> ErrorException.HttpException.Conflict()
            429 -> ErrorException.HttpException.TooManyRequests()
            else -> ErrorException.HttpException.Unknown(code, message ?: "Client error")
        }
    }

    /**
     * Handle 5xx server errors
     */
    private fun handleServerError(code: Int, message: String?): ErrorException {
        return when (code) {
            500 -> ErrorException.HttpException.InternalServerError()
            502 -> ErrorException.HttpException.BadGateway()
            503 -> ErrorException.HttpException.ServiceUnavailable()
            504 -> ErrorException.HttpException.GatewayTimeout()
            else -> ErrorException.HttpException.Unknown(code, message ?: "Server error")
        }
    }
}

/**
 * Extension function untuk wrap suspending function dengan error handling
 */
suspend fun <T> safeApiCall(
    apiCall: suspend () -> T
): Result<T> {
    return try {
        Result.success(apiCall())
    } catch (e: Exception) {
        val ErrorException = ErrorHandler.handleException(e)
        Result.failure(ErrorException)
    }
}

/**
 * Extension function untuk Result dengan automatic error mapping
 */
inline fun <T, R> Result<T>.mapWithErrorHandling(
    transform: (T) -> R
): Result<R> {
    return try {
        this.map(transform)
    } catch (e: Exception) {
        val ErrorException = ErrorHandler.handleException(e)
        Result.failure(ErrorException)
    }
}

/**
 * Extension function untuk validate dan throw ErrorException jika invalid
 */
fun String.validateConversationId(): String {
    if (this.isBlank()) {
        throw ErrorException.ValidationException.InvalidConversationId()
    }
    return this
}

fun String.validateUserId(): String {
    if (this.isBlank()) {
        throw ErrorException.ValidationException.InvalidUserId()
    }
    return this
}

fun String.validateMessageContent(maxLength: Int = 4000): String {
    when {
        this.isBlank() -> throw ErrorException.ValidationException.EmptyMessage()
        this.length > maxLength -> throw ErrorException.ValidationException.MessageTooLong(maxLength)
    }
    return this
}

fun String.validateMessageType(): String {
    val validTypes = listOf("text", "image", "audio", "video", "document", "product_card", "system")
    if (!validTypes.contains(this.lowercase())) {
        throw ErrorException.ValidationException.InvalidMessageType()
    }
    return this
}