package pe.idat.BackEndConecta.dto.projections;

public interface ProductividadTecnicaProjection {
    String getTecnico();
    Long getCompletadas();
    Long getCanceladas();
    Long getReprogramadas();
}
