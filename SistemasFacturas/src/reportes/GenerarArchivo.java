package reportes;

import instancias.Factura;
import instancias.Items;
import java.io.BufferedReader;
import java.util.List;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class GenerarArchivo 
{
    Factura f;
    String t_base;
    String t_importe;
    
    public GenerarArchivo(Factura f, String t_base, String t_importe)
    {
        this.f = f;
        this.t_base = t_base;
        this.t_importe = t_importe;
    }
    
    public String genEncabezado()
    {
        String encabezado = "[Comprobante]\n" +
                            "Version=3.3\n" +
                            "Serie=" + f.getSerie() + "\n" +
                            "Folio=" + f.getFolio() + "\n" +
                            "xxFecha=2011-02-10T12:23:45 //Vamos a deshabilitar este dato para que la libreria le inserte la fecha actual\n" +
                            "FormaPago=" + f.getForma_pago() + "\n" +
                            "CondicionesDePago=Contado\n" +
                            "SubTotal=" + f.getSubtotal() + "\n" +
                            "Descuento=" + f.getDcto() + "\n" +
                            "Moneda=MXN\n" +
                            "TipoCambio=1\n" +
                            "Total=" + f.getTotal() + "\n" +
                            "TipoDeComprobante=I\n" +
                            "MetodoPago=" + f.getMetodo_pago() + "\n" +
                            "LugarExpedicion=" + f.getCodPostal() + "\n" +
                            "Confirmacion=\n\n";
        return encabezado;
    }
    
    public String emisorReceptor()
    {
        String personas = "[Emisor]\n" +
                        "Rfc=" + f.getRfc_emisor() + "\n" +
                        "Nombre=" + f.getNombre_emisor() + "\n" +
                        "RegimenFiscal=" + f.getReg_fiscal() + "\n" +
                        "\n" +
                        "[Receptor]\n" +
                        "Rfc=" + f.getRfc_receptor() + "\n" +
                        "Nombre=" + f.getNombre_recptor() + "\n" +
                        "ResidenciaFiscal=\n" +
                        "NumRegIdTrib=\n" +
                        "UsoCFDI=" + f.getUso_cfdi() + "\n\n";
        return personas;
    }
    
    public String genConceptos()
    {
        String conceptos = "";
        String unidad = "";
        List<Items> items = f.getConceptos();
        for(int i = 0 ; i < items.size() ; i++)
        {
            if(items.get(i).getUnidad().equals("*")) unidad = "PZA";
            else unidad = items.get(i).getUnidad();
            
            conceptos = conceptos + "[Concepto" + (i+1) + "]\n" +
                                    "ClaveProdServ=" + items.get(i).getClave() + "\n" +
                                    "Cantidad=" + items.get(i).getCantidad() + "\n" +
                                    "Unidad=" + unidad + "\n" +
                                    "ClaveUnidad=H87\n" +
                                    "Descripcion=" + items.get(i).getDescripcion() + " Lote= " + items.get(i).getLote() + " Caducidad= " + items.get(i).getCaducidad() + "\n" +
                                    "ValorUnitario=" + items.get(i).getPrecio_unitario() + "\n" +
                                    "Importe=" + items.get(i).getImporte() + "\n" +
                                    "Descuento=0.00\n\n";
        }
        
        return conceptos;
    }
    
    public String getImpuestos()
    {
        String impuestos = "TrasladoBase1=" + t_base + "\n" +
                    "TrasladoImpuesto1=002\n" +
                    "TrasladoTipoFactor1=Tasa\n" +
                    "TrasladoTasaOCuota1=0.160000\n" +
                    "TrasladoImporte1=" + t_importe + "\n\n";
        
        String imp = "[Impuestos]\n" +
                    "TotalImpuestosRetenidos=\n" +
                    "TotalImpuestosTrasladados=" + t_importe + "\n" +
                    "\n" +
                    "TrasladoImpuesto1=002\n" +
                    "TrasladoTipoFactor1=Tasa\n" +
                    "TrasladoTasaOCuota1=0.160000\n" +
                    "TrasladoImporte1=" + t_importe;
        
        return impuestos + imp;
    }
    
    public boolean creaTXT()
    {
        String filename = f.getSerie() + f.getFolio() + ".txt";
        String cad = "";
        try
        {
            File archivo = new File("C:\\SistemaFactura3.3\\Facturar\\" + filename);
            FileWriter escribe = new FileWriter(archivo, true);
            
            if(t_base.equals("0.00")) cad = genEncabezado() + emisorReceptor() + genConceptos();
            else cad = genEncabezado() + emisorReceptor() + genConceptos() + getImpuestos();
            
            escribe.write(cad);
            escribe.close();
            
            moverArchivo(filename);
            return true;
        }catch(Exception e){
            return false;
        }
    }
    
    public static void moverArchivo(String filename)
    {
        Path origenPath = FileSystems.getDefault().getPath("C:\\SistemaFactura3.3\\Facturar\\" + filename);
	Path destinoPath = FileSystems.getDefault().getPath("C:\\SistemaFactura3.3\\Archivos");
        //Path destinoPath = FileSystems.getDefault().getPath("C:\\SistemaFactura3.3\\FacturasCreadas");
        try
        {
            Files.move(origenPath, destinoPath.resolve(origenPath.getFileName()));
        }catch(Exception ex){}
    }
}