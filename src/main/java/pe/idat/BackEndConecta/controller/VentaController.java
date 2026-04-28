package pe.idat.BackEndConecta.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.idat.BackEndConecta.dto.VentaCompletaRequestDTO;
import pe.idat.BackEndConecta.dto.VentaResponseDTO;
import pe.idat.BackEndConecta.service.VentaService;

@RestController
@RequestMapping("/api/v1/ventas")
@RequiredArgsConstructor
@org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
public class VentaController {

    private final VentaService ventaService;

    @PostMapping
    public ResponseEntity<VentaResponseDTO> generarVenta(@Valid @RequestBody VentaCompletaRequestDTO request) {
        VentaResponseDTO response = ventaService.generarVenta(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
