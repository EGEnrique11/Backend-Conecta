package pe.idat.BackEndConecta.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponseDTO {
    private String accessToken;
    private String refreshToken;
    private UsuarioDTO usuario;

    @Data
    @Builder
    public static class UsuarioDTO {
        private Integer id;
        private String username;
        private java.util.List<String> roles;
    }
}
