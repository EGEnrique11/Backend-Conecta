package pe.idat.BackEndConecta.service;

import pe.idat.BackEndConecta.dto.*;

import java.util.List;

public interface CatalogoService {
    // Servicio CRUD
    List<ServicioDTO> listarServicios();
    ServicioDTO obtenerServicio(Integer id);
    ServicioDTO crearServicio(ServicioRequestDTO dto);
    ServicioDTO actualizarServicio(Integer id, ServicioRequestDTO dto);
    void eliminarServicio(Integer id);

    // Plan CRUD
    List<PlanDTO> listarPlanes();
    List<PlanDTO> listarPlanesPorServicio(Integer servicioId);
    PlanDTO obtenerPlan(Integer id);
    PlanDTO crearPlan(PlanRequestDTO dto);
    PlanDTO actualizarPlan(Integer id, PlanRequestDTO dto);
    void eliminarPlan(Integer id);

    // Promocion CRUD
    List<PromocionDTO> listarPromociones();
    List<PromocionDTO> listarPromocionesPorPlan(Integer planId);
    PromocionDTO obtenerPromocion(Integer id);
    PromocionDTO crearPromocion(PromocionRequestDTO dto);
    PromocionDTO actualizarPromocion(Integer id, PromocionRequestDTO dto);
    void eliminarPromocion(Integer id);

    // EfectoPromocion CRUD
    List<EfectoPromocionDTO> listarEfectosPorPromocion(Integer promocionId);
    EfectoPromocionDTO anadirEfectoAPromocion(Integer promocionId, EfectoPromocionRequestDTO dto);
    void eliminarEfecto(Integer efectoId);

    // Plan - Promocion
    void asociarPlanesAPromocion(Integer promocionId, List<Integer> planIds);
}
