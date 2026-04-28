package pe.idat.BackEndConecta.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import pe.idat.BackEndConecta.JWT.JwtService;
import pe.idat.BackEndConecta.dto.AuthResponseDTO;
import pe.idat.BackEndConecta.dto.LoginRequestDTO;
import pe.idat.BackEndConecta.dto.RefreshTokenRequestDTO;
import pe.idat.BackEndConecta.entity.Empleado;
import pe.idat.BackEndConecta.repository.EmpleadoRepository;
import pe.idat.BackEndConecta.service.AuthService;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final EmpleadoRepository empleadoRepository;
    private final JwtService jwtService;

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        
        String accessToken = jwtService.getToken(userDetails);
        String refreshToken = jwtService.getRefreshToken(userDetails);
        
        Empleado empleado = empleadoRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));

        return buildResponse(accessToken, refreshToken, empleado, userDetails);
    }

    @Override
    public AuthResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        String token = request.getRefreshToken();
        String username = jwtService.getUsernameFromToken(token);

        if (username == null) {
            throw new IllegalArgumentException("Refresh token inválido");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!jwtService.isTokenValid(token, userDetails)) {
            throw new IllegalArgumentException("El refresh token ha expirado o es inválido");
        }

        String newAccessToken = jwtService.getToken(userDetails);
        String newRefreshToken = jwtService.getRefreshToken(userDetails);

        Empleado empleado = empleadoRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));

        return buildResponse(newAccessToken, newRefreshToken, empleado, userDetails);
    }

    private AuthResponseDTO buildResponse(String accToken, String refToken, Empleado empleado, UserDetails user) {
        return AuthResponseDTO.builder()
                .accessToken(accToken)
                .refreshToken(refToken)
                .usuario(AuthResponseDTO.UsuarioDTO.builder()
                        .id(empleado.getId())
                        .username(empleado.getUsername())
                        .roles(user.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.toList()))
                        .build())
                .build();
    }
}
