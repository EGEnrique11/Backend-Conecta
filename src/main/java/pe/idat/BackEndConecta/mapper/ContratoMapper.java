package pe.idat.BackEndConecta.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import pe.idat.BackEndConecta.dto.ContratoResumenDTO;
import pe.idat.BackEndConecta.entity.Contrato;

@Mapper(componentModel = "spring")
public interface ContratoMapper {
    @Mapping(source = "plan.precio", target = "tarifaMensual")
    @Mapping(source = "createdAt", target = "fechaAlta")
    @Mapping(source = "plan.nombre", target = "planNombre")
    @Mapping(source = "plan.velocidadBaseMbps", target = "planVelocidad")
    @Mapping(source = "cicloPago.diaCorte", target = "diaCierre")
    @Mapping(target = "vendedorNombres", expression = 
        "java(contrato.getEmpleadoRegistro() != null ? " +
        "contrato.getEmpleadoRegistro().getNombres() + \" \" + contrato.getEmpleadoRegistro().getApellidoPaterno() : " +
        "\"Venta Web / Sistema\")")
    ContratoResumenDTO toResumenDTO(Contrato contrato);
    
    List<ContratoResumenDTO> toResumenDTOList(List<Contrato> contratos);
}
