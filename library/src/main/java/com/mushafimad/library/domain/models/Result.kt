package com.mushafimad.library.domain.models

/**
 * A generic wrapper for handling success and error states
 * Public API - exposed to library consumers
 */
sealed class Result<out T> {
    /**
     * Success state with data
     */
    data class Success<T>(val data: T) : Result<T>()

    /**
     * Error state with exception and optional message
     */
    data class Error(
        val exception: Throwable,
        val message: String = exception.message ?: "Unknown error"
    ) : Result<Nothing>()

    /**
     * Loading state (optional, for UI states)
     */
    data object Loading : Result<Nothing>()

    /**
     * Check if result is successful
     */
    val isSuccess: Boolean
        get() = this is Success

    /**
     * Check if result is error
     */
    val isError: Boolean
        get() = this is Error

    /**
     * Check if result is loading
     */
    val isLoading: Boolean
        get() = this is Loading

    /**
     * Get data if success, null otherwise
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    /**
     * Get data if success, or throw exception if error
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
        is Loading -> throw IllegalStateException("Cannot get data while loading")
    }

    /**
     * Get data if success, or return default value
     */
    fun getOrDefault(default: @UnsafeVariance T): T = when (this) {
        is Success -> data
        else -> default
    }

    /**
     * Map success data to another type
     */
    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
        is Loading -> this
    }

    /**
     * Flat map success data to another Result
     */
    inline fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
        is Success -> transform(data)
        is Error -> this
        is Loading -> this
    }

    /**
     * Execute action if success
     */
    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }

    /**
     * Execute action if error
     */
    inline fun onError(action: (Throwable) -> Unit): Result<T> {
        if (this is Error) action(exception)
        return this
    }

    /**
     * Execute action if loading
     */
    inline fun onLoading(action: () -> Unit): Result<T> {
        if (this is Loading) action()
        return this
    }

    companion object {
        /**
         * Create a success result
         */
        fun <T> success(data: T): Result<T> = Success(data)

        /**
         * Create an error result
         */
        fun error(exception: Throwable, message: String? = null): Result<Nothing> =
            Error(exception, message ?: exception.message ?: "Unknown error")

        /**
         * Create a loading result
         */
        fun loading(): Result<Nothing> = Loading

        /**
         * Wrap a suspending function call in Result
         */
        suspend inline fun <T> runCatching(block: suspend () -> T): Result<T> = try {
            Success(block())
        } catch (e: Exception) {
            Error(e)
        }
    }
}
