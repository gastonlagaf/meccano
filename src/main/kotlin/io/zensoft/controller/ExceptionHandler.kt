package io.zensoft.controller

import io.zensoft.web.annotation.ControllerAdvice
import io.zensoft.web.annotation.ExceptionHandler
import io.zensoft.web.annotation.ResponseStatus
import io.zensoft.web.api.model.HttpStatus

@ControllerAdvice
class ExceptionHandler {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler([IllegalStateException::class, IllegalArgumentException::class])
    fun handleException(ex: Exception): String {
        return "Something went wrong: ${ex.message}"
    }

}