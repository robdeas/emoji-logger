package tech.robd.emojilogger

import jakarta.servlet.*
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.MDC
import java.util.*
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter

open class EmojiMdcWebFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            MDC.put("requestId", UUID.randomUUID().toString())
            MDC.put("path", request.requestURI)
            MDC.put("method", request.method)
            MDC.put("remoteAddr", request.remoteAddr)
            filterChain.doFilter(request, response)
        } finally {
            MDC.clear()
        }
    }

}
