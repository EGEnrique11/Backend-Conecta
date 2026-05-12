package pe.idat.BackEndConecta.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.idat.BackEndConecta.dto.ClienteDTO;
import pe.idat.BackEndConecta.dto.DireccionDTO;
import pe.idat.BackEndConecta.dto.VentaCompletaRequestDTO;
import pe.idat.BackEndConecta.dto.VentaResponseDTO;
import pe.idat.BackEndConecta.entity.Cliente;
import pe.idat.BackEndConecta.entity.Contrato;
import pe.idat.BackEndConecta.entity.Direccion;
import pe.idat.BackEndConecta.entity.Distrito;
import pe.idat.BackEndConecta.entity.Instalacion;
import pe.idat.BackEndConecta.entity.Plan;
import pe.idat.BackEndConecta.entity.enums.EstadoInstalacion;
import pe.idat.BackEndConecta.entity.enums.TipoVia;
import pe.idat.BackEndConecta.mapper.ClienteMapper;
import pe.idat.BackEndConecta.repository.ClienteRepository;
import pe.idat.BackEndConecta.repository.ContratoRepository;
import pe.idat.BackEndConecta.repository.DireccionRepository;
import pe.idat.BackEndConecta.repository.DistritoRepository;
import pe.idat.BackEndConecta.repository.EmpleadoRepository;
import pe.idat.BackEndConecta.repository.InstalacionRepository;
import pe.idat.BackEndConecta.repository.PlanRepository;
import pe.idat.BackEndConecta.service.impl.VentaServiceImpl;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VentaServiceTest {

    @Mock
    private ClienteRepository clienteRepository; // ¡Te faltaba este mock!
    @Mock
    private DireccionRepository direccionRepository;
    @Mock
    private ContratoRepository contratoRepository;
    @Mock
    private InstalacionRepository instalacionRepository;
    @Mock
    private PlanRepository planRepository;
    @Mock
    private EmpleadoRepository empleadoRepository;
    @Mock
    private ClienteMapper clienteMapper;
    @Mock
    private DistritoRepository distritoRepository;

    @InjectMocks
    private VentaServiceImpl ventaService;

    @Test
    @DisplayName("GIVEN una solicitud de venta completa " +
            "WHEN se ejecuta generarVenta " +
            "THEN se guarda el contrato e instalación atómicamente con estado PENDIENTE")
    void testGenerarVentaExitosa() {
        // GIVEN: Datos de la solicitud
        VentaCompletaRequestDTO request = new VentaCompletaRequestDTO();

        // ¡Líneas corregidas aquí!
        ClienteDTO clienteDTO = new ClienteDTO();
        clienteDTO.setDocumento("76543219");
        request.setDatosCliente(clienteDTO);

        DireccionDTO direccionDTO = new DireccionDTO();
        direccionDTO.setDistritoId(1);
        direccionDTO.setTipoVia(TipoVia.AVENIDA);
        direccionDTO.setNombreVia("Los Pinos");
        direccionDTO.setNumero("150");
        request.setDatosDireccion(direccionDTO);

        request.setPlanId(1);
        request.setFechaProgramada(LocalDate.now().plusDays(2));

        Cliente clienteGuardado = new Cliente();
        clienteGuardado.setId(100);

        Direccion direccionGuardada = new Direccion();
        direccionGuardada.setId(200);

        Distrito distrito = new Distrito();
        distrito.setId(1);

        Plan plan = new Plan();
        plan.setId(1);

        Instalacion instalacionGuardada = new Instalacion();
        instalacionGuardada.setId(400);
        instalacionGuardada.setEstado(EstadoInstalacion.PENDIENTE);
        
        Contrato contratoGuardado = new Contrato();
        contratoGuardado.setId(300);

        // Simulaciones (Mocks)
        when(empleadoRepository.count()).thenReturn(10L);
        when(instalacionRepository.countInstalacionesEnFecha(any(LocalDate.class))).thenReturn(0L);
        when(instalacionRepository.save(any(Instalacion.class))).thenReturn(instalacionGuardada);
        when(clienteRepository.findByDocumento("76543219")).thenReturn(Optional.empty());
        when(clienteMapper.toClienteEntity(any(ClienteDTO.class))).thenReturn(clienteGuardado);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteGuardado);
        when(distritoRepository.findById(any())).thenReturn(Optional.of(distrito));
        when(clienteMapper.toDireccionEntity(any(DireccionDTO.class))).thenReturn(direccionGuardada);
        when(direccionRepository.save(any(Direccion.class))).thenReturn(direccionGuardada);
        when(planRepository.findById(any(Integer.class))).thenReturn(Optional.of(plan));
        when(contratoRepository.save(any(Contrato.class))).thenReturn(contratoGuardado);

        // WHEN: Ejecución del servicio principal
        VentaResponseDTO response = ventaService.generarVenta(request);

        // THEN: Verificaciones BDD
        assertNotNull(response, "La respuesta generada no debe ser nula");
        assertEquals(300, response.getContratoId(), "El contrato ID retornado debe coincidir con el Mock");

        // 1. Verificar que se persistió el Contrato y la Instalación exactamente una
        // vez
        verify(contratoRepository, times(1)).save(any(Contrato.class));

        // 2. Capturar la Instalación guardada para validar reglas de negocio (Estado
        // Inicial)
        ArgumentCaptor<Instalacion> instalacionCaptor = ArgumentCaptor.forClass(Instalacion.class);
        verify(instalacionRepository, times(1)).save(instalacionCaptor.capture());

        Instalacion instalacionGenerada = instalacionCaptor.getValue();
        assertEquals(EstadoInstalacion.PENDIENTE, instalacionGenerada.getEstado(),
                "Toda instalación nueva debe nacer estrictamente en estado PENDIENTE");
    }
}