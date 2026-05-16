package pe.idat.BackEndConecta.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class AvisoMoraEvent extends ApplicationEvent{
    private final Integer reciboId;
    public AvisoMoraEvent(Object source, Integer reciboId){
        super(source);
        this.reciboId = reciboId;
    }
}
