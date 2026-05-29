package com.poc.ms_wallet_digital.configs.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(1)
public class TraceContextFilter extends OncePerRequestFilter {

    // Nomes das chaves padrão do nosso ADR
    private static final String CORRELATION_ID_KEY = "correlation_id";
    private static final String FLOW_KEY = "flow";
    private static final String HEADER_CORRELATION_ID = "X-Correlation-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {

            String correlationId = request.getHeader(HEADER_CORRELATION_ID);
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = UUID.randomUUID().toString();
            }


            String uri = request.getRequestURI();
            String method = request.getMethod();
            String flow = dequecerFlow(uri, method);

            MDC.put(CORRELATION_ID_KEY, correlationId);
            MDC.put(FLOW_KEY, flow);

            response.setHeader(HEADER_CORRELATION_ID, correlationId);

            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    private String dequecerFlow(String uri, String method) {
        if (uri.contains("/deposit")) return "wallet_deposit_v1";
        if (uri.contains("/withdraw")) return "wallet_withdraw_v1";
        if (uri.contains("/transfers") || uri.contains("/balance")) return "wallet_transfer_p2p_v1";
        if (method.equals("POST") && uri.equals("/wallets")) return "wallet_create_v1";
        return "wallet_query_v1";
    }
}
