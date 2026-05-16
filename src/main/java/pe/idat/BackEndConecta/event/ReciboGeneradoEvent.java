package pe.idat.BackEndConecta.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class ReciboGeneradoEvent extends ApplicationEvent{
    private final Integer reciboId;
    public ReciboGeneradoEvent(Object source, Integer reciboId){
        super(source);
        this.reciboId = reciboId;
    }
    
}
