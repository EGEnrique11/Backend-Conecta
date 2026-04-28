package pe.idat.BackEndConecta.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.idat.BackEndConecta.dto.PagoRequestDTO;
import pe.idat.BackEndConecta.service.PagoService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> registrarPago(@Valid @RequestBody PagoRequestDTO request) {
        return new ResponseEntity<>(pagoService.registrarPago(request), HttpStatus.CREATED);
    }
}
