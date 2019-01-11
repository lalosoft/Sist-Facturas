package datos;

import instancias.Cliente;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DatClientes 
{
    Conexion con;
    
    public DatClientes()
    {
        con = null;
    }
    
    public List<Cliente> getClientes()
    {
        String q = "SELECT CLIENTE, RAZON_SOCIAL NOMBRE, RFC FROM CLIENTES";
        List<Cliente> list = new ArrayList();
        con = new Conexion();
        
        try
        {
            ResultSet rs = con.getDatos(q);
            while(rs.next())
            {
                Cliente c = new Cliente(rs.getInt("CLIENTE"), rs.getString("RAZON_SOCIAL") ,rs.getString("RFC").trim());
                list.add(c);
            }
            rs.close();
            con.desconectar();
            return list;
        }catch(Exception e){
            return null;
        } 
    }
    
    public List<Cliente> getClientes(String param)
    {
        String q = "SELECT CLIENTE, APELLIDO_PATERNO, APELLIDO_MATERNO, NOMBRE, RFC FROM CLIENTES WHERE APELLIDO_PATERNO LIKE '%" + param + "%' OR APELLIDO_MATERNO LIKE '%" + param + "%' OR NOMBRE LIKE '%" + param + "%'";
        List<Cliente> list = new ArrayList();
        con = new Conexion();
        String nombre = "";
        
        try
        {
            ResultSet rs = con.getDatos(q);
            while(rs.next())
            {
                nombre =  rs.getString("NOMBRE").trim() + " " +  rs.getString("APELLIDO_PATERNO").trim() + " " +  rs.getString("APELLIDO_MATERNO").trim();
                Cliente c = new Cliente(rs.getInt("CLIENTE"), nombre ,rs.getString("RFC").trim());
                nombre = "";
                
                list.add(c);
            }
            rs.close();
            con.desconectar();
            return list;
        }catch(Exception e){
            return null;
        } 
    }
}
