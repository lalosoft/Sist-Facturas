package datos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Conexion 
{
    private Connection conexion = null;
    private String db = "C:/deltaPV/deltaPV.fdb";
    private String user = "SYSDBA";
    private String pass = "masterkey";
    private Statement st = null;
    private ResultSet rs = null;
    
    public Conexion()
    {
        try
        {
            Class.forName("org.firebirdsql.jdbc.FBDriver");
            conexion = DriverManager.getConnection("jdbc:firebirdsql://localhost/" + db + "?encoding=ISO8859_1", this.user, this.pass);
            //System.out.println("Conectado a la base de datos[ "+ this.db + "]");
        }catch(Exception e){
            System.out.println(e);
        }
    }
        
    public ResultSet getDatos(String consulta)
    {
        try
        {
            st = conexion.createStatement();
            rs = st.executeQuery(consulta);
        }catch(SQLException e){}
        return rs;
    }
    
    public void desconectar()
    {
        try
        {
            rs.close();
            st.close();
            conexion.close();
        }catch(SQLException e){
            System.out.println(e);
        }
    }
    
    public boolean actualizaDatos(String q)
    {
        try
        {
            PreparedStatement pst = conexion.prepareStatement(q);
            pst.execute();
            pst.close();
            conexion.close();
            return true;
        }catch(SQLException e){ return false; }
    }
    
    public boolean actualiza(String q)
    {
        try
        {
            PreparedStatement pst = conexion.prepareStatement(q);
            pst.execute();
            pst.close();
            conexion.close();
            return true;
        }catch(Exception e){ return false; }
    }
    
    public boolean inserta(String q)
    {
        try
        {
            PreparedStatement pst = conexion.prepareStatement(q);               
            pst.execute();
            pst.close();
            return true;
        }catch(Exception e){
            return false; 
        }
    }
}