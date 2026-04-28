package pe.idat.BackEndConecta.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.idat.BackEndConecta.service.SuspensionService;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/contratos")
@RequiredArgsConstructor
public class ContratoController {

    private final SuspensionService suspensionService;

    @PostMapping("/procesar-suspensiones")
    public ResponseEntity<Map<String, Object>> procesarSuspensiones() {
        return ResponseEntity.ok(suspensionService.procesarSuspensionesPorMora(LocalDate.now()));
    }
}
