package pe.idat.BackEndConecta.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class RecordatorioPreventivoEvent extends ApplicationEvent{
    private final Integer reciboId;

    public RecordatorioPreventivoEvent(Object source, Integer reciboId){
        super(source);
        this.reciboId = reciboId;
    }
}
