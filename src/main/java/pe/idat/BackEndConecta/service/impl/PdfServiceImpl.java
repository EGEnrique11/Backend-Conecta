package pe.idat.BackEndConecta.service.impl;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import pe.idat.BackEndConecta.entity.Contrato;
import pe.idat.BackEndConecta.entity.Recibo;
import pe.idat.BackEndConecta.repository.ContratoRepository;
import pe.idat.BackEndConecta.repository.ReciboRepository;
import pe.idat.BackEndConecta.service.PdfService;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;


@Service
@RequiredArgsConstructor
public class PdfServiceImpl implements PdfService {

    private final ReciboRepository reciboRepository;
    private final ContratoRepository contratoRepository;
    private final TemplateEngine templateEngine;

    @Override
    @Transactional(readOnly = true)
    public byte[] generarReciboPdf(Integer reciboId) {
        // 1. Cargar el Recibo Mapeado
        Recibo recibo = reciboRepository.findById(reciboId)
                .orElseThrow(() -> new IllegalArgumentException("El recibo con ID " + reciboId + " no existe."));

        Contrato contrato = recibo.getContrato();

        // 2. Lógica Financiera / Variables Extras para la vista
        BigDecimal igvRate = new BigDecimal("1.18");
        BigDecimal subTotalCalculado = recibo.getMontoTotal().divide(igvRate, 2, RoundingMode.HALF_UP);
        BigDecimal igvCalculado = recibo.getMontoTotal().subtract(subTotalCalculado);

        String reciboFormateado = String.format("S001-%012d", recibo.getId());

        // 3. Thymeleaf Context Binding
        Context context = new Context();
        context.setVariable("recibo", recibo);
        context.setVariable("contrato", contrato);
        context.setVariable("cliente", contrato.getCliente());
        context.setVariable("direccion", contrato.getDireccion());
        context.setVariable("detalles", recibo.getDetalles());

        // Variables Virtuales extras (Cálculos de vista)
        context.setVariable("reciboFolio", reciboFormateado);
        context.setVariable("subTotalCalculado", subTotalCalculado);
        context.setVariable("igvCalculado", igvCalculado);

        // 4. Renderizado: Thymeleaf -> HTML String
        String htmlContent = templateEngine.process("recibo-template", context);

        // 5. Conversión: HTML -> PDF Byte Array (OpenHTMLtoPDF)
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            String baseUri = new ClassPathResource("/static/").getURL().toString();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode(); // Rápido y ligero para textos transaccionales
            builder.withHtmlContent(htmlContent, baseUri);
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generador de PDF: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generarContratoPdf(Integer contratoId) {
        Contrato contrato = contratoRepository.findById(contratoId)
                .orElseThrow(() -> new RuntimeException("Contrato con ID: " + contratoId + " no fue encontrado"));
        Context context = new Context();
        context.setVariable("contrato", contrato);
        context.setVariable("cliente", contrato.getCliente());
        context.setVariable("direccion", contrato.getDireccion());
        context.setVariable("plan", contrato.getPlan());
        context.setVariable("ciclo", contrato.getCicloPago());
        String htmlContent = templateEngine.process("contrato-template", context);
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            String baseUri = new ClassPathResource("/static/").getURL().toString();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, baseUri);
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("Error al generar contrato.pdf"+ e.getMessage(), e);
        }
    }
}
