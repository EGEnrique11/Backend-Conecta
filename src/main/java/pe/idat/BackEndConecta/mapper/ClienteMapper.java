package pe.idat.BackEndConecta.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import pe.idat.BackEndConecta.dto.ClienteDTO;
import pe.idat.BackEndConecta.dto.ClienteRegistrationDTO;
import pe.idat.BackEndConecta.dto.ClienteUpdateDTO;
import pe.idat.BackEndConecta.entity.Cliente;
import pe.idat.BackEndConecta.entity.Direccion;

@Mapper(componentModel = "spring")
public interface ClienteMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "tipoPersona", constant = "CLIENTE")
    Cliente toClienteEntity(ClienteRegistrationDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "distrito", ignore = true)
    @Mapping(target = "direccionCompleta", ignore = true)
    @Mapping(target = "activo", ignore = true)
    Direccion toDireccionEntity(ClienteRegistrationDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", constant = "PRECLIENTE")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "tipoPersona", constant = "CLIENTE")
    Cliente toClienteEntity(pe.idat.BackEndConecta.dto.ClienteDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "distrito", ignore = true)
    @Mapping(target = "direccionCompleta", ignore = true)
    @Mapping(target = "activo", ignore = true)
    Direccion toDireccionEntity(pe.idat.BackEndConecta.dto.DireccionDTO dto);

    ClienteDTO toClienteDTO(Cliente cliente);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "tipoPersona", ignore = true)
    @Mapping(target = "tipoDocumento", ignore = true)
    @Mapping(target = "documento", ignore = true)
    @Mapping(target = "fechaNacimiento", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateClienteFromDto(ClienteUpdateDTO dto, @org.mapstruct.MappingTarget Cliente cliente);
}
