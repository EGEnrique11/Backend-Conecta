package pe.idat.BackEndConecta.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.idat.BackEndConecta.entity.Departamento;
import pe.idat.BackEndConecta.entity.Distrito;
import pe.idat.BackEndConecta.entity.Provincia;
import pe.idat.BackEndConecta.repository.DepartamentoRepository;
import pe.idat.BackEndConecta.repository.DistritoRepository;
import pe.idat.BackEndConecta.repository.ProvinciaRepository;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ubicaciones")
@RequiredArgsConstructor
public class UbicacionController {

    private final DepartamentoRepository departamentoRepository;
    private final ProvinciaRepository provinciaRepository;
    private final DistritoRepository distritoRepository;

    @GetMapping("/departamentos")
    public ResponseEntity<List<Departamento>> getDepartamentos() {
        return ResponseEntity.ok(departamentoRepository.findAll());
    }

    @GetMapping("/provincias/{departamentoId}")
    public ResponseEntity<List<Provincia>> getProvincias(@PathVariable Integer departamentoId) {
        return ResponseEntity.ok(provinciaRepository.findByDepartamentoId(departamentoId));
    }

    @GetMapping("/distritos/{provinciaId}")
    public ResponseEntity<List<Distrito>> getDistritos(@PathVariable Integer provinciaId) {
        return ResponseEntity.ok(distritoRepository.findByProvinciaId(provinciaId));
    }
}
