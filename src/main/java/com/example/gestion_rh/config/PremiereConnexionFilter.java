package com.example.gestion_rh.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1) // Exécutez ce filtre avant les autres
public class PremiereConnexionFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requestURI = httpRequest.getRequestURI();

        // Logique simplifiée : si l'URL contient "premiere-connexion" dans la session
        // ou une autre méthode pour détecter la première connexion
        if (httpRequest.getSession().getAttribute("premiereConnexion") != null
                && !requestURI.contains("/auth/premiere-connexion")
                && !requestURI.contains("/auth/changer-mot-de-passe")
                && !requestURI.contains("/auth/logout")
                && !requestURI.contains("/css/")
                && !requestURI.contains("/js/")
                && !requestURI.contains("/images/")
                && !requestURI.contains("/webjars/")) {

            httpResponse.sendRedirect("/auth/premiere-connexion");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialisation si nécessaire
    }

    @Override
    public void destroy() {
        // Nettoyage si nécessaire
    }
}