package instancias;

public class Cliente 
{
    private int num_cliente;
    private String nombre;
    private String rfc;

    public Cliente(int num_cliente, String nombre, String rfc) {
        this.num_cliente = num_cliente;
        this.nombre = nombre;
        this.rfc = rfc;
    }

    public int getNum_cliente() {
        return num_cliente;
    }

    public String getNombre() {
        return nombre;
    }

    public String getRfc() {
        return rfc;
    }

    public void setNum_cliente(int num_cliente) {
        this.num_cliente = num_cliente;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setRfc(String rfc) {
        this.rfc = rfc;
    } 
}
