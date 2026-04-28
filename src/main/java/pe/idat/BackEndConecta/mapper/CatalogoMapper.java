package pe.idat.BackEndConecta.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.idat.BackEndConecta.dto.*;
import pe.idat.BackEndConecta.entity.EfectoPromocion;
import pe.idat.BackEndConecta.entity.Plan;
import pe.idat.BackEndConecta.entity.Promocion;
import pe.idat.BackEndConecta.entity.Servicio;

@Mapper(componentModel = "spring")
public interface CatalogoMapper {

    ServicioDTO toServicioDTO(Servicio servicio);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    Servicio toServicioEntity(ServicioRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    void updateServicioFromDto(ServicioRequestDTO dto, @org.mapstruct.MappingTarget Servicio servicio);

    @Mapping(source = "servicio.id", target = "servicioId")
    @Mapping(source = "servicio.nombre", target = "nombreServicio")
    PlanDTO toPlanDTO(Plan plan);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "servicio", ignore = true)
    @Mapping(target = "activo", ignore = true)
    Plan toPlanEntity(PlanRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "servicio", ignore = true)
    @Mapping(target = "activo", ignore = true)
    void updatePlanFromDto(PlanRequestDTO dto, @org.mapstruct.MappingTarget Plan plan);

    PromocionDTO toPromocionDTO(Promocion promocion);

    @Mapping(target = "id", ignore = true)
    Promocion toPromocionEntity(PromocionRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    void updatePromocionFromDto(PromocionRequestDTO dto, @org.mapstruct.MappingTarget Promocion promocion);

    @Mapping(source = "promocion.id", target = "promocionId")
    EfectoPromocionDTO toEfectoPromocionDTO(EfectoPromocion efecto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "promocion", ignore = true)
    EfectoPromocion toEfectoPromocionEntity(EfectoPromocionRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "promocion", ignore = true)
    void updateEfectoPromocionFromDto(EfectoPromocionRequestDTO dto, @org.mapstruct.MappingTarget EfectoPromocion efecto);
}
