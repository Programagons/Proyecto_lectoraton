package org.iesalixar.daw2.lectoraton.config;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.iesalixar.daw2.lectoraton.services.CustomUserDetailsService;
import org.iesalixar.daw2.lectoraton.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Inyección de dependencias
    @Autowired
    private JwtUtil jwtUtil; // Utilidad para generar, extraer y validar tokens JWT

    @Autowired
    private CustomUserDetailsService userDetailsService; // Servicio personalizado para cargar detalles del usuario

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return "/api/v1/authenticate".equals(path) || "/api/v1/register".equals(path);
    }

    /**
     * Método principal del filtro que intercepta cada solicitud HTTP entrante
     * y valida el token JWT si está presente en el encabezado de autorización.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Extraer el encabezado Authorization de la solicitud
        final String authHeader = request.getHeader("Authorization");
        final String jwt;        // Variable para almacenar el token JWT
        final String username;   // Variable para almacenar el nombre de usuario extraído del token

        // Enlace público del correo (aprobar editor): no intentar validar JWT aquí
        String path = request.getServletPath();
        if ("GET".equalsIgnoreCase(request.getMethod())
                && path != null
                && path.startsWith("/api/v1/editor-requests/approve")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Verificar si el encabezado Authorization está presente y tiene un token válido
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Si el encabezado no está presente o no comienza con "Bearer", pasa la solicitud al siguiente filtro
            filterChain.doFilter(request, response);
            return; // Termina la ejecución de este filtro
        }

        try {
            // 3. Extraer el token JWT del encabezado (sin el prefijo "Bearer ")
            jwt = authHeader.substring(7); // Elimina los primeros 7 caracteres ("Bearer ")
            // 4. Extraer el nombre de usuario (claim "sub") del token
            username = jwtUtil.extractUsername(jwt);

            // 5. Verificar si:
            // - EL nombre de usuario extraído no es nulo
            // - No hay una autenticación existente en el contexto de seguridad
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 6. Cargar los detalles del usuario desde el servicio personalizado
                var userDetails = userDetailsService.loadUserByUsername(username);

                // 7. Validar el token JWT con el nombre de usuario del usuario cargado
                if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {

                    // 8. Extraer los claims del token (como los roles)
                    Claims claims = jwtUtil.extractAllClaims(jwt);

                    // 9. Extraer los roles del claim "roles" y convertirlos en GrantedAuthority
                    List<String> roles = claims.get("roles", List.class); // Obtiene la lista de roles del token
                    List<SimpleGrantedAuthority> authorities = roles.stream()
                            .map(SimpleGrantedAuthority::new) // Convierte cada rol en SimpleGrantedAuthority
                            .toList();

                    // 10. Crear un objeto UsernamePasswordAuthenticationToken con los detalles del usuario y sus roles
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

                    // 11. Configurar los detalles adicionales de la solicitud actual (por ejemplo, dirección IP)
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 12. Establecer la autenticación en el contexto de seguridad de Spring
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (JwtException | IllegalArgumentException ignored) {
            // Si el token es inválido o está mal formado, no autenticamos al usuario.
            // Dejamos continuar la petición para que Spring Security responda con 401
            // cuando el endpoint requiera autenticación.
            SecurityContextHolder.clearContext();
        }

        // 13. Continuar con el siguiente filtro en la cadena de filtros
        filterChain.doFilter(request, response);
    }

}