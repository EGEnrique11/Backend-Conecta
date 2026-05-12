package pe.idat.BackEndConecta.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import pe.idat.BackEndConecta.dto.ClienteDTO;
import pe.idat.BackEndConecta.dto.DireccionDTO;
import pe.idat.BackEndConecta.dto.VentaCompletaRequestDTO;
import pe.idat.BackEndConecta.entity.enums.*;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Revierte cambios al finalizar para mantener la BD limpia
public class VentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "VENDEDOR") // Simula sesión activa de vendedor
    @DisplayName("GIVEN un payload JSON válido de venta " +
                 "WHEN se ejecuta POST /api/v1/ventas " +
                 "THEN retorna HTTP 201 y el ID del contrato generado")
    void testRegistrarVentaIntegral() throws Exception {
        // GIVEN: Payload de Venta Completa
        VentaCompletaRequestDTO dto = new VentaCompletaRequestDTO();
        dto.setPlanId(1);
        dto.setFechaProgramada(LocalDate.now().plusDays(3));

        ClienteDTO cliente = new ClienteDTO();
        cliente.setTipoDocumento(TipoDocumento.DNI);
        cliente.setDocumento("76543219");
        cliente.setNombres("Juan");
        cliente.setApellidoPaterno("Perez");
        cliente.setApellidoMaterno("Gomez");
        cliente.setCelular("987654321");
        cliente.setCorreo("juan.test@gmail.com");
        dto.setDatosCliente(cliente);

        DireccionDTO direccion = new DireccionDTO();
        direccion.setDistritoId(1);
        direccion.setTipoVia(TipoVia.AVENIDA);
        direccion.setNombreVia("Los Pinos");
        dto.setDatosDireccion(direccion);

        String jsonPayload = objectMapper.writeValueAsString(dto);

        // WHEN: Petición Integral via WebMvc
        mockMvc.perform(post("/api/v1/ventas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                
        // THEN: Validaciones HTTP y de Contrato
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensaje").exists())
                .andExpect(jsonPath("$.contratoId").isNumber());
    }
}
