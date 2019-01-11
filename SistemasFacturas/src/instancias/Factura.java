package instancias;

import java.util.List;

public class Factura 
{
    private String serie;
    private String folio;
    private String forma_pago;
    private String subtotal;
    private String dcto;
    private String total;
    private String cod_postal;
    private String rfc_emisor;
    private String nombre_emisor;
    private String reg_fiscal;
    
    private String rfc_receptor;
    private String nombre_recptor;
    private String metodo_pago;
    private String uso_cfdi;
    
    private List<Items> conceptos;

    public Factura(String serie, String folio, String forma_pago, String subtotal, String dcto, String total, String cod_postal, String rfc_emisor, String nombre_emisor, String reg_fiscal, String rfc_receptor, String nombre_recptor, String metodo_pago, String uso_cfdi, List<Items> conceptos) {
        this.serie = serie;
        this.folio = folio;
        this.forma_pago = forma_pago;
        this.subtotal = subtotal;
        this.dcto = dcto;
        this.total = total;
        this.cod_postal = cod_postal;
        this.rfc_emisor = rfc_emisor;
        this.nombre_emisor = nombre_emisor;
        this.reg_fiscal = reg_fiscal;
        this.rfc_receptor = rfc_receptor;
        this.nombre_recptor = nombre_recptor;
        this.metodo_pago = metodo_pago;
        this.uso_cfdi = uso_cfdi;
        this.conceptos = conceptos;
    }

    public String getSerie() {
        return serie;
    }

    public String getFolio() {
        return folio;
    }

    public String getForma_pago() {
        return forma_pago;
    }

    public String getSubtotal() {
        return subtotal;
    }

    public String getDcto() {
        return dcto;
    }

    public String getTotal() {
        return total;
    }
    
    public String getCodPostal(){
        return cod_postal;
    }

    public String getRfc_emisor() {
        return rfc_emisor;
    }

    public String getNombre_emisor() {
        return nombre_emisor;
    }

    public String getReg_fiscal() {
        return reg_fiscal;
    }

    public String getRfc_receptor() {
        return rfc_receptor;
    }

    public String getNombre_recptor() {
        return nombre_recptor;
    }
    
    public String getMetodo_pago() {
        return metodo_pago;
    }

    public String getUso_cfdi() {
        return uso_cfdi;
    }
    
    public List<Items> getConceptos() {
        return conceptos;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public void setFolio(String folio) {
        this.folio = folio;
    }

    public void setForma_pago(String forma_pago) {
        this.forma_pago = forma_pago;
    }

    public void setSubtotal(String subtotal) {
        this.subtotal = subtotal;
    }

    public void setDcto(String dcto) {
        this.dcto = dcto;
    }

    public void setTotal(String total) {
        this.total = total;
    }
    
    public void setCodPostal(String cod_postal)
    {
        this.cod_postal = cod_postal;
    }

    public void setRfc_emisor(String rfc_emisor) {
        this.rfc_emisor = rfc_emisor;
    }

    public void setNombre_emisor(String nombre_emisor) {
        this.nombre_emisor = nombre_emisor;
    }

    public void setReg_fiscal(String reg_fiscal) {
        this.reg_fiscal = reg_fiscal;
    }

    public void setRfc_receptor(String rfc_receptor) {
        this.rfc_receptor = rfc_receptor;
    }

    public void setNombre_recptor(String nombre_recptor) {
        this.nombre_recptor = nombre_recptor;
    }

    public void setMetodo_pago(String metodo_pago) {
        this.metodo_pago = metodo_pago;
    }

    public void setUso_cfdi(String uso_cfdi) {
        this.uso_cfdi = uso_cfdi;
    }

    public void setConceptos(List<Items> conceptos) {
        this.conceptos = conceptos;
    }
}