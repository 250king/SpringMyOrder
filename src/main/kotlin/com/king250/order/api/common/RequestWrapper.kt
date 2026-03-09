package com.king250.order.api.common

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import java.util.*

class RequestWrapper(request: HttpServletRequest) : HttpServletRequestWrapper(request) {
    override fun getParameterNames(): Enumeration<String> {
        val names = super.getParameterNames().toList()
        return Collections.enumeration(names.map { toCamelCase(it) })
    }

    override fun getParameterValues(name: String): Array<String>? {
        // 尝试获取原始名称或转换后的名称
        return super.getParameterValues(name) ?: super.getParameterValues(toSnakeCase(name))
    }

    private fun toCamelCase(s: String) = s.replace("_([a-z])".toRegex()) { it.groupValues[1].uppercase() }
    private fun toSnakeCase(s: String) = s.replace("([a-z])([A-Z])".toRegex()) { "${it.groupValues[1]}_${it.groupValues[2].lowercase()}" }
}