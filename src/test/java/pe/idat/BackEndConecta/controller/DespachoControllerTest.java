package pe.idat.BackEndConecta.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import pe.idat.BackEndConecta.dto.AsignarTecnicoDTO;
import pe.idat.BackEndConecta.service.DespachoService;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class DespachoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DespachoService despachoService; // Usamos MockBean para aislar el Controller

    @Test
    @WithMockUser(roles = "ADMIN") // Inyectando contexto de seguridad
    @DisplayName("GIVEN Admin y payload de asignación válido " +
                 "WHEN se ejecuta PUT /api/v1/despacho/asignar/{instalacionId} " +
                 "THEN retorna HTTP 200 y mensaje de confirmación")
    void testAsignarTecnicoEndpoint() throws Exception {
        // GIVEN: Preparación de la petición simulada
        Integer instalacionId = 5;
        
        AsignarTecnicoDTO dto = new AsignarTecnicoDTO();
        dto.setTecnicoId(20);
        dto.setBloqueId(30);

        String jsonPayload = objectMapper.writeValueAsString(dto);
        
        // Mockeando respuesta del servicio para aislar la prueba integral del Controller
        Map<String, String> serviceResponse = Map.of("message", "Técnico asignado correctamente al bloque.");
        when(despachoService.asignarTecnicoABloque(eq(instalacionId), any(AsignarTecnicoDTO.class)))
                .thenReturn(serviceResponse);

        // WHEN: Ejecución de petición REST con MockMvc
        mockMvc.perform(put("/api/v1/despacho/asignar/{instalacionId}", instalacionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                
        // THEN: Verificación de Status Code y Mapeo JSON
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Técnico asignado correctamente al bloque."));
    }
}
