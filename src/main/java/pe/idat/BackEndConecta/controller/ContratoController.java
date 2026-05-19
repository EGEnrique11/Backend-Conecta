package pe.idat.BackEndConecta.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pe.idat.BackEndConecta.dto.ContratoResumenDTO;
import pe.idat.BackEndConecta.service.ContratoService;
import pe.idat.BackEndConecta.service.SuspensionService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/v1/contratos")
@RequiredArgsConstructor
public class ContratoController {
    private final ContratoService contratoService;
    private final SuspensionService suspensionService;

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<ContratoResumenDTO>> obtenerContratosPorCliente(@PathVariable Integer clienteId) {
        List<ContratoResumenDTO> contratos = contratoService.buscarPorClienteId(clienteId);
        return ResponseEntity.ok(contratos);
    }

    @PostMapping("/procesar-suspensiones")
    public ResponseEntity<Map<String, Object>> procesarSuspensiones() {
        return ResponseEntity.ok(suspensionService.procesarSuspensionesPorMora(LocalDate.now()));
    }
}
