package pe.idat.BackEndConecta.event.listener;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import pe.idat.BackEndConecta.event.ReciboGeneradoEvent;
import pe.idat.BackEndConecta.service.NotificacionService;

@Component
@RequiredArgsConstructor
public class ReciboEventListener {
    private final NotificacionService notificacionService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReciboGenerado(ReciboGeneradoEvent event){
        notificacionService.enviarReciboMensualAsync(event.getReciboId());
    }
}
