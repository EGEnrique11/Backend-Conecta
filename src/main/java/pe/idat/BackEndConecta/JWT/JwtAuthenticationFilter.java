package pe.idat.BackEndConecta.JWT;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        System.out.println("Procesando solicitud: "+request.getRequestURI());
        final String token = getTokenFromRequest(request);
        if (token == null){
            System.out.println("No se encontro token, pasa solicitud");
            filterChain.doFilter(request, response);
            return;
        }
        String username = null;
        try {
            username = jwtService.getUsernameFromToken(token);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("El token JWT ha expirado");
            // DEVOLVEMOS 401 INMEDIATAMENTE Y CORTAMOS EL FILTRO
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Token expirado\"}");
            return; 
        } catch (Exception e) {
            log.error("Error desconocido al parsear el token JWT {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null){
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (jwtService.isTokenValid(token, userDetails)){
                List<String> roles = jwtService.getRolesFromToken(token);
                List<String> permissions = jwtService.getPermissionsFromToken(token);
                List<GrantedAuthority> authorities = new ArrayList<>();
                if (roles != null) {
                    roles.forEach(role -> authorities.add(new SimpleGrantedAuthority(role)));
                }
                if (permissions != null) {
                    permissions.forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));
                }
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, authorities
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
    private String getTokenFromRequest(HttpServletRequest request){
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer")){
            return authHeader.substring(7);
        }
        return null;
    }
}
