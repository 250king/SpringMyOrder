package com.king250.order.api.common

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import java.util.*

class QueryFilter(request: HttpServletRequest) : HttpServletRequestWrapper(request) {
    private val convertedMap: Map<String, Array<String>> by lazy {
        val newMap = mutableMapOf<String, Array<String>>()
        super.getParameterMap().forEach { (key, value) ->
            newMap[toCamelCase(key)] = value
        }
        newMap
    }

    override fun getParameterNames(): Enumeration<String> {
        return Collections.enumeration(convertedMap.keys)
    }

    override fun getParameterValues(name: String): Array<String>? {
        return convertedMap[name]
    }

    override fun getParameter(name: String): String? {
        return convertedMap[name]?.firstOrNull()
    }

    override fun getParameterMap(): Map<String, Array<String>> {
        return convertedMap
    }

    private fun toCamelCase(s: String): String {
        return s.replace("_([a-z])".toRegex()) { it.groupValues[1].uppercase() }
    }
}