package pe.idat.BackEndConecta.service;

import pe.idat.BackEndConecta.dto.AuthResponseDTO;
import pe.idat.BackEndConecta.dto.LoginRequestDTO;
import pe.idat.BackEndConecta.dto.RefreshTokenRequestDTO;

public interface AuthService {
    AuthResponseDTO login(LoginRequestDTO request);
    AuthResponseDTO refreshToken(RefreshTokenRequestDTO request);
}
