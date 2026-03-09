package com.king250.order.api.config

import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.ConstraintViolationException
import org.jooq.exception.IntegrityConstraintViolationException
import org.jooq.exception.NoDataFoundException
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException

@RestControllerAdvice
class ExceptionConfig {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(IntegrityConstraintViolationException::class)
    fun handleJooqIntegrityException(e: IntegrityConstraintViolationException, response: HttpServletResponse) {
        val sqlState = e.sqlState()
        val (status, userMessage) = when (sqlState) {
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
        response.sendError(status, userMessage)
    }

    @ExceptionHandler(NoDataFoundException::class)
    fun handleJooqNoDataFoundException(response: HttpServletResponse) {
        response.sendError(404, "The record is not found.")
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleValidationException(e: ConstraintViolationException, response: HttpServletResponse) {
        response.sendError(400, e.message)
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