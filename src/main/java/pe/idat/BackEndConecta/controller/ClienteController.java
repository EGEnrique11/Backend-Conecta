package pe.idat.BackEndConecta.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import pe.idat.BackEndConecta.dto.ClienteDTO;
import pe.idat.BackEndConecta.dto.ClienteRegistrationDTO;
import pe.idat.BackEndConecta.dto.ClienteUpdateDTO;
import pe.idat.BackEndConecta.service.ClienteService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
@org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping("/registro")
    public ResponseEntity<Map<String, Object>> registrarCliente(@Valid @RequestBody ClienteRegistrationDTO dto) {
        Map<String, Object> response = new HashMap<>();
        try {
            ClienteRegistrationDTO registrado = clienteService.registrarCliente(dto);
            response.put("mensaje", "Cliente registrado exitosamente");
            response.put("datos", registrado);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            response.put("mensaje", "Error en el registro");
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            response.put("mensaje", "Error interno en el servidor");
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<Page<ClienteDTO>> buscarClientes(
            @RequestParam(required = false) String documento,
            @RequestParam(required = false) String celular,
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) String nombres,
            @RequestParam(required = false) String apellidoPaterno,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ClienteDTO> clientes = clienteService.buscarClientes(documento, celular, id, nombres, apellidoPaterno,
                pageable);
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/dni/{dni}")
    public ResponseEntity<ClienteDTO> buscarClientePorDni(@PathVariable String dni) {
        return clienteService.buscarPorDni(dni)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> actualizarCliente(
            @PathVariable Integer id,
            @Valid @RequestBody ClienteUpdateDTO dto) {
        Map<String, Object> response = new HashMap<>();
        try {
            ClienteDTO actualizado = clienteService.actualizarCliente(id, dto);
            response.put("mensaje", "Cliente actualizado exitosamente");
            response.put("datos", actualizado);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("mensaje", "Error en la actualización");
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            response.put("mensaje", "Error interno en el servidor");
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminarCliente(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            clienteService.eliminarCliente(id);
            response.put("mensaje", "Cliente dado de baja exitosamente");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("mensaje", "Error al eliminar");
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            response.put("mensaje", "Error interno en el servidor");
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
