package instancias;

public class Items 
{
    private String clave;
    private int cantidad;
    private String unidad;
    private String descripcion;
    private String lote;
    private String caducidad;
    private String precio_unitario;
    private String importe;

    public Items(String clave, int cantidad, String unidad, String descripcion, String lote, String caducidad, String precio_unitario, String importe) {
        this.clave = clave;
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.descripcion = descripcion;
        this.lote = lote;
        this.caducidad = caducidad;
        this.precio_unitario = precio_unitario;
        this.importe = importe;
    }

    public String getClave() {
        return clave;
    }

    public int getCantidad() {
        return cantidad;
    }

    public String getUnidad() {
        return unidad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getPrecio_unitario() {
        return precio_unitario;
    }

    public String getImporte() {
        return importe;
    }

    public String getLote() {
        return lote;
    }

    public String getCaducidad() {
        return caducidad;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setPrecio_unitario(String precio_unitario) {
        this.precio_unitario = precio_unitario;
    }

    public void setImporte(String importe) {
        this.importe = importe;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public void setCaducidad(String caducidad) {
        this.caducidad = caducidad;
    }
}