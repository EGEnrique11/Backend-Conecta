package pe.idat.BackEndConecta.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class ContratoActivoEvent extends ApplicationEvent{
    private final Integer contratoId;
    public ContratoActivoEvent(Object source, Integer contratoId){
        super(source);
        this.contratoId = contratoId;
    }
}
