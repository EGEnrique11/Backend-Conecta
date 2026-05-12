package pe.idat.BackEndConecta.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import pe.idat.BackEndConecta.dto.EmpleadoListDTO;
import pe.idat.BackEndConecta.dto.EmpleadoRegistroDTO;
import pe.idat.BackEndConecta.dto.TecnicoResumenDTO;
import pe.idat.BackEndConecta.entity.Empleado;
import pe.idat.BackEndConecta.entity.Role;
import pe.idat.BackEndConecta.entity.enums.TipoDocumento;
import pe.idat.BackEndConecta.entity.enums.TipoPersona;
import pe.idat.BackEndConecta.repository.EmpleadoRepository;
import pe.idat.BackEndConecta.repository.RoleRepository;
import pe.idat.BackEndConecta.service.EmpleadoService;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EmpleadoServiceImpl implements EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public String registrarEmpleado(EmpleadoRegistroDTO dto) {

        // 1. Validaciones de Negocio y Restricciones
        if (dto.getTipoDocumento() != null && dto.getTipoDocumento().name().equals("RUC")) {
            throw new IllegalArgumentException("Un empleado no puede registrarse con RUC. Solo se permite DNI o CE.");
        }

        if (Boolean.TRUE.equals(empleadoRepository.existsByDocumento(dto.getDocumento()))) {
            throw new IllegalArgumentException("Ya existe un empleado con el documento: " + dto.getDocumento());
        }

        if (Boolean.TRUE.equals(empleadoRepository.existsByUsername(dto.getUsername()))) {
            throw new IllegalArgumentException("El username ya está en uso: " + dto.getUsername());
        }

        // 2. Mapeo de Atributos de Persona
        Empleado empleado = new Empleado();
        empleado.setTipoPersona(TipoPersona.EMPLEADO);
        empleado.setTipoDocumento(dto.getTipoDocumento());
        empleado.setDocumento(dto.getDocumento());
        empleado.setNombres(dto.getNombres());
        empleado.setApellidoPaterno(dto.getApellidoPaterno());
        empleado.setApellidoMaterno(dto.getApellidoMaterno());
        empleado.setCorreo(dto.getCorreo());

        // 3. Atributos propios de Empleado
        empleado.setUsername(dto.getUsername());
        empleado.setPassword(passwordEncoder.encode(dto.getPassword()));

        // Default Flags
        empleado.setIsEnabled(true);
        empleado.setAccountNoExpired(true);
        empleado.setAccountNoLocked(true);
        empleado.setCredentialNoExpired(true);

        // 4. Asignación de Roles y Validación de Seguridad
        Authentication auth = SecurityContextHolder
                .getContext().getAuthentication();
        boolean isDeveloper = false;
        boolean isAdmin = false;

        if (auth != null) {
            isDeveloper = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DEVELOPER"));
            isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        }

        Set<Role> rolesTarget = new HashSet<>();
        for (String roleName : dto.getRoles()) {
            if (isAdmin && !isDeveloper && (roleName.equals("ROLE_ADMIN") || roleName.equals("ROLE_DEVELOPER"))) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "Un administrador no puede crear perfiles con el rol " + roleName);
            }

            Role role = roleRepository.findByRoleName(roleName)
                    .orElseThrow(
                            () -> new IllegalArgumentException("El rol " + roleName + " no existe en el sistema."));
            rolesTarget.add(role);
        }
        empleado.setRoles(rolesTarget);

        // 5. Persistencia
        empleado = empleadoRepository.save(empleado);

        return "Empleado registrado exitosamente con ID: " + empleado.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<java.util.Map<String, Object>> obtenerTecnicos() {
        java.util.List<Empleado> tecnicos = empleadoRepository.findAllByRoleName("ROLE_TECNICO");
        return tecnicos.stream().map(emp -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", emp.getId());
            map.put("nombre", emp.getNombres() + " " + emp.getApellidoPaterno());
            return map;
        }).collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmpleadoListDTO> obtenerEmpleadosPaginados(
            int page, int size, String rol, TipoDocumento tipoDocumento,
            String criterio, String valor) {

        Pageable pageable = PageRequest.of(page, size);

        return empleadoRepository.buscarEmpleadosEstricto(rol, tipoDocumento, criterio, valor, pageable)
                .map(emp -> EmpleadoListDTO.builder()
                        .id(emp.getId())
                        .documento(emp.getDocumento())
                        .tipoDocumento(emp.getTipoDocumento())
                        .nombresCompletos(emp.getNombres() + " " + emp.getApellidoPaterno() + " "
                                + (emp.getApellidoMaterno() != null ? emp.getApellidoMaterno() : ""))
                        .username(emp.getUsername())
                        .celular(emp.getCelular())
                        .correo(emp.getCorreo())
                        .roles(emp.getRoles().stream().map(Role::getRoleName)
                                .collect(java.util.stream.Collectors.toSet()))
                        .build());
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<TecnicoResumenDTO> buscarTecnicosResumen(String term) {
        if (term == null || term.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }

        java.util.List<Empleado> empleados = empleadoRepository.buscarTecnicosPorTermino(term);
        return empleados.stream().map(emp -> {
            TecnicoResumenDTO dto = new TecnicoResumenDTO();
            dto.setId(emp.getId());
            dto.setNombreCompleto(emp.getNombres() + " " + emp.getApellidoPaterno());
            dto.setDocumento(emp.getDocumento());
            dto.setCelular(emp.getCelular());

            if (emp.getTurno() != null) {
                dto.setTurnoId(emp.getTurno().getId());
                dto.setTurnoNombre(emp.getTurno().getNombre());
            }
            return dto;
        }).collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TecnicoResumenDTO> obtenerTecnicosPaginados(int page, int size,
            String term) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Empleado> empleados;

        if (term != null && !term.trim().isEmpty()) {
            empleados = empleadoRepository.buscarTecnicosPaginadoPorTermino(term, pageable);
        } else {
            empleados = empleadoRepository.findByRolesRoleName("ROLE_TECNICO", pageable);
        }

        return empleados.map(emp -> {
            TecnicoResumenDTO dto = new TecnicoResumenDTO();
            dto.setId(emp.getId());
            dto.setNombreCompleto(emp.getNombres() + " " + emp.getApellidoPaterno());
            dto.setDocumento(emp.getDocumento());
            dto.setCelular(emp.getCelular());

            if (emp.getTurno() != null) {
                dto.setTurnoId(emp.getTurno().getId());
                dto.setTurnoNombre(emp.getTurno().getNombre());
            } else {
                dto.setTurnoNombre("Sin Turno");
            }
            return dto;
        });
    }
}
