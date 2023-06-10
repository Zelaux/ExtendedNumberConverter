package com.zelaux.numberconverter

import java.lang.Exception

sealed class Result<S, out E> {
    class Success<S, E>(val value: S) : Result<S, E>()
    class Error<S, E>(override val error: E) : Result<S, E>()

    fun isError() = this is Error
    open val error: E? get() = null
    fun unwrap(): S? = when (this) {
        is Error -> null
        is Success -> value
//        else -> throw UnsupportedOperationException("")
    }
}