package pe.idat.BackEndConecta.event.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pe.idat.BackEndConecta.event.AvisoMoraEvent;
import pe.idat.BackEndConecta.event.RecordatorioPreventivoEvent;
import pe.idat.BackEndConecta.service.NotificacionService;

@Component
@RequiredArgsConstructor
public class RecordatorioEventListener {
    private final NotificacionService notificacionService;

    @EventListener
    public void onRecordatorioPreventivo(RecordatorioPreventivoEvent event){
        notificacionService.enviarRecordatorioPreventivoAsync(event.getReciboId());
    }

    @EventListener
    public void onAvisoMora(AvisoMoraEvent event) {
        notificacionService.enviarAvisoMoraAsync(event.getReciboId());
    }
}
