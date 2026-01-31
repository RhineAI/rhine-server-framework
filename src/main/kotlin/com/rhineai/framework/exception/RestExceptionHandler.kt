package com.rhineai.framework.exception

import com.rhineai.framework.constant.CommonErrorCode
import com.rhineai.framework.entity.vo.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.validation.BindException

import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.security.core.AuthenticationException

@RestControllerAdvice
class RestExceptionHandler {
    private val log = LoggerFactory.getLogger(RestExceptionHandler::class.java)

    @ExceptionHandler(value = [InternalAuthenticationServiceException::class, AuthenticationException::class])
    fun handleAuthenticationException(ex: Exception, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        var cause: Throwable? = ex
        while (cause != null) {
            if (cause is BusinessException) {
                // Return 401 with the business exception message instead of delegating to handleBusinessException (which returns 400)
                log.warn("Authentication error (BusinessException): {}", cause.message)
                val body = ErrorResponse(code = cause.code, message = cause.message ?: CommonErrorCode.AUTHENTICATION_FAILED.message, path = request.requestURI)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body)
            }
            cause = cause.cause
        }
        log.warn("Authentication error: {}", ex.message)
        val body = ErrorResponse(code = CommonErrorCode.AUTHENTICATION_FAILED.code, message = ex.message ?: CommonErrorCode.AUTHENTICATION_FAILED.message, path = request.requestURI)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body)
    }

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(ex: BusinessException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        log.warn("BusinessException: code={}, message={}", ex.code, ex.message)
        val body = ErrorResponse(code = ex.code, message = ex.message ?: CommonErrorCode.INTERNAL_ERROR.message, path = request.requestURI)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    @ExceptionHandler(value = [MethodArgumentNotValidException::class, BindException::class, ConstraintViolationException::class, MissingServletRequestParameterException::class])
    fun handleValidationExceptions(ex: Exception, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val message = when (ex) {
            is MethodArgumentNotValidException -> ex.bindingResult.allErrors.firstOrNull()?.defaultMessage ?: CommonErrorCode.INVALID_PARAMETER.message
            is BindException -> ex.bindingResult.allErrors.firstOrNull()?.defaultMessage ?: CommonErrorCode.INVALID_PARAMETER.message
            is ConstraintViolationException -> ex.constraintViolations.firstOrNull()?.message ?: CommonErrorCode.INVALID_PARAMETER.message
            is MissingServletRequestParameterException -> "缺少必要的参数: ${ex.parameterName}"
            else -> CommonErrorCode.INVALID_PARAMETER.message
        }
        log.warn("Validation error: {}", message)
        val body = ErrorResponse(code = CommonErrorCode.INVALID_PARAMETER.code, message = message, path = request.requestURI)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    @ExceptionHandler(NoPermissionException::class)
    fun handleNoPermissionException(ex: NoPermissionException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        log.warn("NoPermissionException: 权限不足")
        val body = ErrorResponse(code = CommonErrorCode.NO_PERMISSION.code, message = CommonErrorCode.NO_PERMISSION.message, path = request.requestURI)
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        log.error("Unhandled exception", ex)
         var cause: Throwable? = ex
        while (cause != null) {
            if (cause is BusinessException) {
                return handleBusinessException(cause, request)
            }
            cause = cause.cause
        }
        val body = ErrorResponse(code = CommonErrorCode.INTERNAL_ERROR.code, message = CommonErrorCode.INTERNAL_ERROR.message, path = request.requestURI)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body)
    }
}