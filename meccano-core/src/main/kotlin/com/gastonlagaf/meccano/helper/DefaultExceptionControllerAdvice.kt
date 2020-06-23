package com.gastonlagaf.meccano.helper

import com.gastonlagaf.meccano.annotation.ControllerAdvice
import com.gastonlagaf.meccano.annotation.ExceptionHandler
import com.gastonlagaf.meccano.annotation.ResponseStatus
import com.gastonlagaf.meccano.api.HttpSession
import com.gastonlagaf.meccano.api.WrappedHttpRequest
import com.gastonlagaf.meccano.api.exceptions.*
import com.gastonlagaf.meccano.api.model.ExceptionResponse
import com.gastonlagaf.meccano.api.model.HttpResponseStatus
import com.gastonlagaf.meccano.api.model.MimeType
import com.gastonlagaf.meccano.api.model.ValidationError
import org.slf4j.LoggerFactory
import javax.validation.ConstraintViolationException

@ControllerAdvice
class DefaultExceptionControllerAdvice {

    @ResponseStatus(HttpResponseStatus.BAD_REQUEST)
    @ExceptionHandler([ConstraintViolationException::class])
    fun handleValidationException(ex: ConstraintViolationException): List<ValidationError> {
        return ex.constraintViolations.map { ValidationError(it.propertyPath.toString(), it.message) }
    }

    @ResponseStatus(HttpResponseStatus.NOT_FOUND)
    @ExceptionHandler(values = [HandlerMethodNotFoundException::class], produces = MimeType.TEXT_HTML)
    fun handleMethodHandlerNotFoundException(): String {
        return "handler_not_found"
    }

    @ResponseStatus(HttpResponseStatus.FORBIDDEN)
    @ExceptionHandler(values = [PreconditionNotSatisfiedException::class], produces = MimeType.TEXT_HTML)
    fun handlePreconditionNotSatisfiedException(ex: PreconditionNotSatisfiedException, request: WrappedHttpRequest, session: HttpSession): String {
        return if (ex.viewLogin) {
            session.setAttribute("override_referrer", request.getPath())
            "redirect:/login"
        } else {
            "forbidden"
        }
    }

    @ResponseStatus(HttpResponseStatus.FORBIDDEN)
    @ExceptionHandler(values = [PreconditionNotSatisfiedException::class], produces = MimeType.APPLICATION_JSON)
    fun handlePreconditionNotSatisfiedExceptionAsJson(ex: PreconditionNotSatisfiedException): ExceptionResponse {
        return ExceptionResponse(HttpResponseStatus.FORBIDDEN.code, ex.message)
    }

    @ResponseStatus(HttpResponseStatus.FORBIDDEN)
    @ExceptionHandler(values = [InvalidRememberMeTokenException::class], produces = MimeType.TEXT_HTML)
    fun handleInvalidRememberMeTokenException(ex: InvalidRememberMeTokenException): String {
        log.warn(ex.message)
        return "redirect:/login"
    }

    @ResponseStatus(HttpResponseStatus.UNAUTHORIZED)
    @ExceptionHandler(values = [AuthenticationFailedException::class], produces = MimeType.TEXT_HTML)
    fun handleAuthenticationFailedExceptionAsHtml(ex: AuthenticationFailedException): String {
        log.warn(ex.message)
        return "unauthorized"
    }

    @ResponseStatus(HttpResponseStatus.UNAUTHORIZED)
    @ExceptionHandler(values = [AuthenticationFailedException::class], produces = MimeType.APPLICATION_JSON)
    fun handleAuthenticationFailedExceptionAsJson(ex: AuthenticationFailedException): ExceptionResponse {
        log.warn(ex.message)
        return ExceptionResponse(HttpResponseStatus.UNAUTHORIZED.code, ex.message)
    }

    @ResponseStatus(HttpResponseStatus.BAD_REQUEST)
    @ExceptionHandler(values = [HandlerParameterInstantiationException::class], produces = MimeType.APPLICATION_JSON)
    fun handleAuthenticationFailedExceptionAsJson(ex: HandlerParameterInstantiationException): ExceptionResponse {
        log.warn(ex.message)
        return ExceptionResponse(HttpResponseStatus.BAD_REQUEST.code, ex.message)
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

}