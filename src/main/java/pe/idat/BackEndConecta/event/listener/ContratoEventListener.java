package pe.idat.BackEndConecta.event.listener;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import pe.idat.BackEndConecta.event.ContratoActivoEvent;
import pe.idat.BackEndConecta.service.NotificacionService;

@Component
@RequiredArgsConstructor
public class ContratoEventListener {
    private final NotificacionService notificacionService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onContratoActivado(ContratoActivoEvent event){
        notificacionService.enviarContratoAsync(event.getContratoId());
    }
}
