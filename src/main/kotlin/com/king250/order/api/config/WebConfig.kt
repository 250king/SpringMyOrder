package com.king250.order.api.config

import com.king250.order.api.common.RequestWrapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Configuration
class WebConfig {
    @Component
    class SnakeCaseFilter : OncePerRequestFilter() {
        override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
            // 只针对 GET 请求处理，或者根据需要过滤
            if (request.method == "GET") {
                chain.doFilter(RequestWrapper(request), response)
            } else {
                chain.doFilter(request, response)
            }
        }
    }
}