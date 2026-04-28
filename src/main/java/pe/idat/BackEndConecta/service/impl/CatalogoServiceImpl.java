package pe.idat.BackEndConecta.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.idat.BackEndConecta.dto.*;
import pe.idat.BackEndConecta.entity.*;
import pe.idat.BackEndConecta.mapper.CatalogoMapper;
import pe.idat.BackEndConecta.repository.*;
import pe.idat.BackEndConecta.service.CatalogoService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CatalogoServiceImpl implements CatalogoService {

    private final ServicioRepository servicioRepository;
    private final PlanRepository planRepository;
    private final PromocionRepository promocionRepository;
    private final EfectoPromocionRepository efectoPromocionRepository;
    private final CatalogoMapper catalogoMapper;

    // --- SERVICIO CRUD ---
    @Override
    @Transactional(readOnly = true)
    public List<ServicioDTO> listarServicios() {
        return servicioRepository.findAll().stream()
                .map(catalogoMapper::toServicioDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ServicioDTO obtenerServicio(Integer id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado con ID: " + id));
        return catalogoMapper.toServicioDTO(servicio);
    }

    @Override
    @Transactional
    public ServicioDTO crearServicio(ServicioRequestDTO dto) {
        Servicio servicio = catalogoMapper.toServicioEntity(dto);
        servicio = servicioRepository.save(servicio);
        return catalogoMapper.toServicioDTO(servicio);
    }

    @Override
    @Transactional
    public ServicioDTO actualizarServicio(Integer id, ServicioRequestDTO dto) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado con ID: " + id));
        catalogoMapper.updateServicioFromDto(dto, servicio);
        servicio = servicioRepository.save(servicio);
        return catalogoMapper.toServicioDTO(servicio);
    }

    @Override
    @Transactional
    public void eliminarServicio(Integer id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado con ID: " + id));
        // Soft Delete
        servicio.setActivo(false);
        servicioRepository.save(servicio);
    }

    // --- PLAN CRUD ---
    @Override
    @Transactional(readOnly = true)
    public List<PlanDTO> listarPlanes() {
        return planRepository.findAll().stream()
                .map(catalogoMapper::toPlanDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanDTO> listarPlanesPorServicio(Integer servicioId) {
        return planRepository.findByServicioId(servicioId).stream()
                .map(catalogoMapper::toPlanDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PlanDTO obtenerPlan(Integer id) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Plan no encontrado con ID: " + id));
        return catalogoMapper.toPlanDTO(plan);
    }

    @Override
    @Transactional
    public PlanDTO crearPlan(PlanRequestDTO dto) {
        Servicio servicio = servicioRepository.findById(dto.getServicioId())
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado con ID: " + dto.getServicioId()));

        Plan plan = catalogoMapper.toPlanEntity(dto);
        plan.setServicio(servicio);
        plan = planRepository.save(plan);
        return catalogoMapper.toPlanDTO(plan);
    }

    @Override
    @Transactional
    public PlanDTO actualizarPlan(Integer id, PlanRequestDTO dto) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Plan no encontrado con ID: " + id));
        
        Servicio servicio = servicioRepository.findById(dto.getServicioId())
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado con ID: " + dto.getServicioId()));

        catalogoMapper.updatePlanFromDto(dto, plan);
        plan.setServicio(servicio);
        
        plan = planRepository.save(plan);
        return catalogoMapper.toPlanDTO(plan);
    }

    @Override
    @Transactional
    public void eliminarPlan(Integer id) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Plan no encontrado con ID: " + id));
        // Soft Delete
        plan.setActivo(false);
        planRepository.save(plan);
    }

    // --- PROMOCION CRUD ---
    @Override
    @Transactional(readOnly = true)
    public List<PromocionDTO> listarPromociones() {
        return promocionRepository.findAll().stream()
                .map(catalogoMapper::toPromocionDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromocionDTO> listarPromocionesPorPlan(Integer planId) {
        return promocionRepository.findByPlanes_Id(planId).stream()
                .map(catalogoMapper::toPromocionDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PromocionDTO obtenerPromocion(Integer id) {
        Promocion promocion = promocionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Promoción no encontrada con ID: " + id));
        return catalogoMapper.toPromocionDTO(promocion);
    }

    @Override
    @Transactional
    public PromocionDTO crearPromocion(PromocionRequestDTO dto) {
        Promocion promocion = catalogoMapper.toPromocionEntity(dto);
        promocion = promocionRepository.save(promocion);
        return catalogoMapper.toPromocionDTO(promocion);
    }

    @Override
    @Transactional
    public PromocionDTO actualizarPromocion(Integer id, PromocionRequestDTO dto) {
        Promocion promocion = promocionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Promoción no encontrada con ID: " + id));
        catalogoMapper.updatePromocionFromDto(dto, promocion);
        promocion = promocionRepository.save(promocion);
        return catalogoMapper.toPromocionDTO(promocion);
    }

    @Override
    @Transactional
    public void eliminarPromocion(Integer id) {
        // En este caso, promoción se podría borrar de forma física si no tiene contratos atados.
        // Pero para estándar usamos repository.deleteById (Asumiendo que no causará constraints rotos si no hay planes_promocion aún)
        // Ya que la tabla promoción no tiene un campo 'activo' en el SQL provisto por el usuario.
        promocionRepository.deleteById(id);
    }

    // --- EFECTO PROMOCION CRUD ---
    @Override
    @Transactional(readOnly = true)
    public List<EfectoPromocionDTO> listarEfectosPorPromocion(Integer promocionId) {
        return efectoPromocionRepository.findByPromocionId(promocionId).stream()
                .map(catalogoMapper::toEfectoPromocionDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EfectoPromocionDTO anadirEfectoAPromocion(Integer promocionId, EfectoPromocionRequestDTO dto) {
        Promocion promocion = promocionRepository.findById(promocionId)
                .orElseThrow(() -> new IllegalArgumentException("Promoción no encontrada con ID: " + promocionId));
        
        EfectoPromocion efecto = catalogoMapper.toEfectoPromocionEntity(dto);
        efecto.setPromocion(promocion);
        
        efecto = efectoPromocionRepository.save(efecto);
        return catalogoMapper.toEfectoPromocionDTO(efecto);
    }

    @Override
    @Transactional
    public void eliminarEfecto(Integer efectoId) {
        efectoPromocionRepository.deleteById(efectoId);
    }

    @Override
    @Transactional
    public void asociarPlanesAPromocion(Integer promocionId, List<Integer> planIds) {
        Promocion promocion = promocionRepository.findById(promocionId)
                .orElseThrow(() -> new IllegalArgumentException("Promoción no encontrada con ID: " + promocionId));

        List<Plan> planes = planRepository.findAllById(planIds);
        if (planes.isEmpty() && planIds != null && !planIds.isEmpty()) {
            throw new IllegalArgumentException("No se encontraron los planes especificados.");
        }

        promocion.getPlanes().addAll(planes);
        promocionRepository.save(promocion);
    }
}
