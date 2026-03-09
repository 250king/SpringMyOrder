package com.king250.order.api.config

import com.king250.order.api.common.QueryFilter
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class FilterConfig : OncePerRequestFilter() {
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        if (request.method == "GET") {
            chain.doFilter(QueryFilter(request), response)
        } else {
            chain.doFilter(request, response)
        }
    }
}