package com.king250.order.api.config

import com.king250.order.api.integration.auth.AuthService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.ConstraintViolationException
import org.jooq.exception.IntegrityConstraintViolationException
import org.jooq.exception.NoDataFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.util.AntPathMatcher
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException

@RestControllerAdvice
class ExceptionConfig(
    private val auth: AuthService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(IntegrityConstraintViolationException::class)
    fun handleJooqIntegrityException(e: IntegrityConstraintViolationException, response: HttpServletResponse) {
        val sqlState = e.sqlState()
        val (status, message) = when (sqlState) {
            "23505" -> {
                409 to "The record already exists."
            }
            "23503" -> {
                400 to "The action is forbidden because this data is still referenced or its dependency is missing."
            }
            else -> {
                400 to "Database integrity violation"
            }
        }
        response.sendError(status, message)
    }

    @ExceptionHandler(NoDataFoundException::class)
    fun handleJooqNoDataFoundException(response: HttpServletResponse) {
        response.sendError(404, "The record is not found.")
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleValidationException(e: ConstraintViolationException, response: HttpServletResponse) {
        response.sendError(400, e.message)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleValidationException(e: HttpMessageNotReadableException, response: HttpServletResponse) {
        response.sendError(400, e.message)
    }

    @ExceptionHandler(AuthorizationDeniedException::class)
    fun handleAuthorizationException(
        e: AuthorizationDeniedException,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        val uri = request.requestURI
        val matcher = AntPathMatcher()
        var status = 404
        var message = "No static resource item."
        when {
            matcher.match("/groups/{groupId}/**", uri) -> {
                val variables = matcher.extractUriTemplateVariables("/groups/{groupId}/**", uri)
                val groupId = variables["groupId"]?.toLongOrNull()
                if (groupId != null) {
                    if (auth.isMember(groupId)) {
                        status = 403
                        message = e.message ?: "You don't have permission to perform this action."
                    }
                }
            }
        }
        response.sendError(status, message)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(e: Exception, response: HttpServletResponse) {
        if (e is ResponseStatusException || e is org.springframework.web.ErrorResponse) {
            throw e
        }
        log.error("Unexpected exception: ${e.message}", e)
        response.sendError(500, "The server has gone to buy some coffee.")
    }
}