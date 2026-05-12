package pe.idat.BackEndConecta.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.idat.BackEndConecta.dto.AsignarTecnicoDTO;
import pe.idat.BackEndConecta.entity.BloqueHorario;
import pe.idat.BackEndConecta.entity.Empleado;
import pe.idat.BackEndConecta.entity.Instalacion;
import pe.idat.BackEndConecta.entity.Role;
import pe.idat.BackEndConecta.entity.enums.EstadoInstalacion;
import pe.idat.BackEndConecta.repository.BloqueHorarioRepository;
import pe.idat.BackEndConecta.repository.EmpleadoRepository;
import pe.idat.BackEndConecta.repository.InstalacionRepository;
import pe.idat.BackEndConecta.service.impl.DespachoServiceImpl;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DespachoServiceTest {

    @Mock
    private InstalacionRepository instalacionRepository;
    @Mock
    private EmpleadoRepository empleadoRepository;
    @Mock
    private BloqueHorarioRepository bloqueRepository;

    @InjectMocks
    private DespachoServiceImpl despachoService;

    @Test
    @DisplayName("GIVEN Instalación PENDIENTE y Técnico válido " +
            "WHEN se asigna técnico al bloque " +
            "THEN el estado muta a EN_RUTA y el técnico es asignado exitosamente")
    void testAsignarTecnicoYMutarEstado() {
        // GIVEN: Preparación de Entidades
        Integer instalacionId = 10;
        Integer tecnicoId = 20;
        Integer bloqueId = 30;

        Instalacion instalacionPendiente = new Instalacion();
        instalacionPendiente.setId(instalacionId);
        instalacionPendiente.setEstado(EstadoInstalacion.PENDIENTE);
        instalacionPendiente.setFechaProgramada(LocalDate.now().plusDays(1));

        Role rolTecnico = new Role();
        rolTecnico.setRoleName("ROLE_TECNICO");

        Empleado tecnico = new Empleado();
        tecnico.setId(tecnicoId);
        tecnico.setRoles(Set.of(rolTecnico));

        BloqueHorario bloque = new BloqueHorario();
        bloque.setId(bloqueId);

        AsignarTecnicoDTO dto = new AsignarTecnicoDTO();
        dto.setTecnicoId(tecnicoId);
        dto.setBloqueId(bloqueId);

        // Simulaciones (Mocks)
        when(instalacionRepository.findById(instalacionId)).thenReturn(Optional.of(instalacionPendiente));
        when(empleadoRepository.findById(tecnicoId)).thenReturn(Optional.of(tecnico));
        when(bloqueRepository.findById(bloqueId)).thenReturn(Optional.of(bloque));
        when(instalacionRepository.existsByTecnicoIdAndBloqueId(tecnicoId, instalacionPendiente.getFechaProgramada(),
                bloqueId))
                .thenReturn(false); // Bloque libre

        // WHEN: Ejecución de Lógica de Negocio
        Map<String, String> response = despachoService.asignarTecnicoABloque(instalacionId, dto);

        // THEN: Verificaciones
        assertNotNull(response, "La respuesta no debe ser nula");
        assertEquals("Técnico asignado correctamente al bloque.", response.get("message"));

        // Regla de Negocio: Mutación de Estado Atómica
        assertEquals(EstadoInstalacion.EN_RUTA, instalacionPendiente.getEstado(),
                "El estado de la instalación debió cambiar a EN_RUTA de forma automática");
        assertEquals(tecnico, instalacionPendiente.getTecnico(),
                "El técnico debe estar inyectado en la instalación");

        // Verificación de Persistencia
        verify(instalacionRepository, times(1)).save(instalacionPendiente);
    }
}
