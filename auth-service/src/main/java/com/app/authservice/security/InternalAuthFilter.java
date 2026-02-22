package com.app.authservice.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class InternalAuthFilter implements Filter {

    @Value("${internal.secret}")
    private String internalSecret;

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req =
                (HttpServletRequest) request;

        HttpServletResponse res =
                (HttpServletResponse) response;

        String gatewayKey = req.getHeader("X-GATEWAY-KEY");
        String internalKey = req.getHeader("X-INTERNAL-KEY");

        if (internalSecret.equals(gatewayKey)
                || internalSecret.equals(internalKey)) {

            chain.doFilter(request, response);
            return;
        }

        res.setStatus(HttpServletResponse.SC_FORBIDDEN);
        res.getWriter().write("Access Denied");
    }
}