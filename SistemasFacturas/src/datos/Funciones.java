package datos;

import instancias.Items;
import instancias.Documentos;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Funciones 
{
    Conexion con;
    int cte;
    Documentos doc;
    float total;
    float ivat;
    
    public Funciones()
    {
        con = null;
        cte = 0;
        total = 0;
        ivat = 0;
        doc = new Documentos();
    }
    
    public int getVenta(int folio, String serie)
    {
        int venta = Integer.MIN_VALUE;
        String q = "SELECT VENTA FROM VENTAS WHERE FOLIO = " + folio + " AND SERIE = '" + serie + "'";
        con = new Conexion();
        try
        {
            ResultSet rs = con.getDatos(q);
            while(rs.next())
            {
                venta = rs.getInt("VENTA");
            }
            rs.close();
            con.desconectar();
            return venta;
            
        }catch(Exception e){
            return Integer.MIN_VALUE;
        }
    }
    
    public int getCliente(String serie, int folio)
    {
        int cte = Integer.MIN_VALUE;
        String q = "SELECT CLIENTE FROM VENTAS WHERE FOLIO = " + folio + " AND SERIE= '" + serie + "'";
        con = new Conexion();
        
        try
        {
            ResultSet rs = con.getDatos(q);
            while(rs.next())
            {
                cte = rs.getInt("CLIENTE");
            }
            rs.close();
            con.desconectar();
            return cte;
        }catch(Exception e){ return Integer.MIN_VALUE; }
    }
    
    public String getCorreoCte(String serie, int folio)
    {
        String mail_cte = "";
        String q = "SELECT EMAIL FROM CLIENTES WHERE CLIENTE = " + getCliente(serie, folio);
        con = new Conexion();
        
        try
        {
            ResultSet rs = con.getDatos(q);
            while(rs.next())
            {
                mail_cte = rs.getString("EMAIL");
            }
            rs.close();
            con.desconectar();
        }catch(Exception ex){}
        
        return mail_cte;
    }
    
    public List<Items> getVentas(int venta)
    {
        String q = "SELECT CANTIDAD, PRODUCTO_CODIGO, CONCEPTO, UNIDAD, PRECIO, LOTE, FECHA_CADUCIDAD, IVA_PRC FROM VENTAS_ITEMS WHERE VENTA = " + venta;
        String cod_sat = "";
        List<Items> articulos = new ArrayList();
        DateFormat fecha_formato = new SimpleDateFormat("dd-MM-yyyy");
        con = new Conexion();
        total = 0.0f;
        ivat = 0.0f;
        
        try
        {
            ResultSet rs = con.getDatos(q);
            while(rs.next())
            {
                cod_sat = getCodSat(rs.getString("PRODUCTO_CODIGO").trim());
                if(cod_sat.equals("")) cod_sat = getNoExist();
                
                BigDecimal precio = new BigDecimal(rs.getFloat("PRECIO"));
                precio = precio.setScale(2, BigDecimal.ROUND_HALF_UP);
                
                float importe = rs.getInt("CANTIDAD") * rs.getFloat("PRECIO");
                BigDecimal importe1 = new BigDecimal(importe);
                importe1 = importe1.setScale(2, BigDecimal.ROUND_HALF_UP);
                
                if(rs.getFloat("IVA_PRC") == 16.0)
                {
                    ivat = ivat + (rs.getInt("CANTIDAD") * rs.getFloat("PRECIO"));
                }
                total = total + importe;
                
                Items item = new Items(cod_sat, rs.getInt("CANTIDAD"), rs.getString("UNIDAD").trim(), rs.getString("CONCEPTO").trim(), rs.getString("LOTE").trim(), fecha_formato.format(rs.getDate("FECHA_CADUCIDAD")), precio.toString(), importe1.toString());
                articulos.add(item);
                cod_sat = "";
            }
            //System.out.println(iva);
            rs.close();
            con.desconectar();
            return articulos;
            
        }catch(Exception e){
            return null;
        }
    }
            
    public String getCodSat(String cod_prod)
    {
        con = new Conexion();
        String split = " ";
        String q = "SELECT COD_SAT FROM CODIGOS_SAT WHERE COD_PROD = '" + cod_prod + "'";
        String cod = "01010101";
        
        try
        {
            ResultSet rs = con.getDatos(q);
            while(rs.next())
            {
                cod = rs.getString("COD_SAT").trim();
            }
            rs.close();
            con.desconectar();
        }catch(Exception e){}
        
        //String valor [] = cod.split(split);
        //cod = valor[0];
        return cod;
    }
    
    public String[] getDatosEmpresa()
    {
        String datos[] = new String[4];
        String q = "SELECT * FROM CONFIGURACION";
        con = new Conexion();
        
        try
        {
            ResultSet rs = con.getDatos(q);
            while(rs.next())
            {
                datos[0] = rs.getString("FD_NOMBRE").trim();
                datos[1] = rs.getString("FD_CALLE").trim() + " " + rs.getString("FD_NUMERO_EXTERIOR").trim() + " " + rs.getString("FD_COLONIA").trim() + "\n" +
                           rs.getString("FD_MUNICIPIO").trim() + ", " + rs.getString("FD_ESTADO").trim();
                datos[2] = rs.getString("FD_RFC").trim();
                datos[3] = Integer.toString(rs.getInt("FD_CODIGO_POSTAL")).trim();
            }
            rs.close();
            con.desconectar();
        }catch(Exception e){}
        return datos;
    }
    
    public String getSustancia(String codigo)
    {
        con = new Conexion();
        String split = " ";
        String q = "SELECT SUSTANCIA_ACTIVA FROM PRODUCTOS WHERE CODIGO = '" + codigo + "'";
        String cod = "";
        
        try
        {
            ResultSet rs = con.getDatos(q);
            while(rs.next())
            {
                cod = rs.getString("SUSTANCIA_ACTIVA").trim();
            }
            rs.close();
            con.desconectar();
        }catch(Exception e){}
        
        String valor [] = cod.split(split);
        cod = valor[0];
        return cod;
    }
    
    public String eliminaSignos(String cadena)
    {
        String signos = ".,;'?¿!¡-_:()";
        int iterador = 0;
        String car_Inspec;
        while(iterador < cadena.length())
        {
            car_Inspec = cadena.substring(iterador, iterador + 1);
            if(signos.contains(car_Inspec)) cadena =  cadena.replaceAll("\\" + car_Inspec, "");
            else iterador = iterador + 1;
        }
        return cadena;
    }
    
    public String getNoExist()
    {
        con = new Conexion();
        String cod = "";
        String q = "SELECT FIRST 1 CLAVE_PROD_SERV FROM CODIGOS_SAT";
        try
        {
            ResultSet rs = con.getDatos(q);
            while(rs.next())
            {
                cod = rs.getString("CLAVE_PROD_SERV").trim();
            }
            rs.close();
            con.desconectar();
            return cod;
        }catch(Exception e){
            return cod;
        }
    }
    
    public String getFecha(int venta)
    {
        con = new Conexion();
        String q = "SELECT FECHA FROM VENTAS WHERE VENTA = " + venta;
        String fecha = "";
        DateFormat fecha_formato = new SimpleDateFormat("dd/MM/yyyy");
        
        try
        {
            ResultSet rs = con.getDatos(q);
            while(rs.next())
            {
                fecha = fecha_formato.format(rs.getDate("FECHA"));
            }
            rs.close();
            con.desconectar();
        }catch(Exception e){}
        return fecha.trim();
    }
    
    public String getFormaPago(int venta)
    {
        con = new Conexion();
        String q = "SELECT METODO_PAGO FROM VENTAS WHERE VENTA = " +  venta;
        String pago = "";
        
        try
        {
            ResultSet rs = con.getDatos(q);
            while(rs.next())
            {
                pago = rs.getString("METODO_PAGO").trim();
            }
            rs.close();
            con.desconectar();
        }catch(Exception e){}
        
        return pago;
    }
    
    public int getCliente(int venta)
    {
        con = new Conexion();
        String q = "SELECT CLIENTE FROM VENTAS WHERE VENTA = " + venta;
        int id_cte = 0;
        
        try
        { 
            ResultSet rs = con.getDatos(q);
            while(rs.next())
            {
                id_cte = rs.getInt("CLIENTE");
            }
            rs.close();
            con.desconectar();
        }catch(Exception e){}

        return id_cte;
    }
    
    public String[] buscaCte(int id_cte)
    {
        con = new Conexion();
        String q = "SELECT RFC, RAZON_SOCIAL FROM CLIENTES WHERE CLIENTE = " + id_cte;
        String [] datos = new String[2];
        
        try
        {
            ResultSet rs = con.getDatos(q);
            while(rs.next())
            {
                datos[0] = rs.getString("RFC").trim();
                datos[1] = rs.getString("RAZON_SOCIAL").trim();
            }
            rs.close();
            con.desconectar();
        }catch(Exception e){}
        return datos;
    }
    
    public float[] getIvaTotal(int venta)
    {
        float [] totales = new float[8];
        String q = "SELECT IMPORTE_IVA0, IMPORTE_IVA16, IMPORTE, IVA, TOTAL, DESCUENTO FROM VENTAS WHERE VENTA = " + venta;
        con = new Conexion();
        
        try
        {
            ResultSet rs = con.getDatos(q);
            while(rs.next())
            {
                totales[0] = rs.getFloat("IMPORTE");
                totales[1] = rs.getFloat("TOTAL");
                totales[2] = rs.getFloat("DESCUENTO");
                totales[3] = rs.getFloat("IMPORTE_IVA16");
                totales[4] = rs.getFloat("IMPORTE_IVA0");
                totales[5] = rs.getFloat("IVA");
            }
            rs.close();
            con.desconectar();
        }catch(Exception e){}
        
        return totales;
    }
    
    public boolean buscaSW(String sw_cte)
    {
        String q = "SELECT SW_CLIENTE_FACTURA FROM CONFIGURACION WHERE SW_CLIENTE_FACTURA = '" + sw_cte + "'";
        con = new Conexion();
        String aux = "";
        
        try
        {
            ResultSet rs = con.getDatos(q);
            while(rs.next())
            {
                aux = rs.getString("SW_CLIENTE_FACTURA").trim();
            }
            rs.close();
        }catch(Exception e){}
        
        if(aux.endsWith(sw_cte)) return true;
        else return false;
    }
    
    public int getTransaction()
    {
        String q = "SELECT TRANSACTION_ID FROM VENTAS WHERE TRANSACTION_ID = ( SELECT MAX(TRANSACTION_ID) FROM VENTAS)";
        con = new Conexion();
        int trans = 0;
        
        try
        {
            ResultSet rs = con.getDatos(q);
            while(rs.next())
            {
                trans = rs.getInt("TRANSACTION_ID");
            }
            rs.close();
        }catch(Exception e){}
        
        return trans;
    }
    
    public boolean actualizaTimbrado(String serie, int folio, String forma_pago)
    {
        String user = System.getProperty("user.name");
        String uuid = doc.getUuid(serie + folio);        
        String pdf = "C:\\Users\\" + user + "\\Desktop\\FacturasCreadas\\" + serie + folio + ".pdf";
        String xml = "C:\\Users\\" + user + "\\Desktop\\FacturasCreadas\\" + serie + folio + ".xml";
        String actDatos = "UPDATE VENTAS SET METODO_PAGO = '" + forma_pago + "', TIMBRADA = 'S', QRCODE = 'S', ENVIADA = 'S', UUID = '" + uuid + "', PDF = '" + pdf + "', XML = '" + xml + "', TRANSACTION_ID = " + (getTransaction() + 1) + " WHERE SERIE = '" + serie + "' AND FOLIO = " + folio;
        //String actDatos = "UPDATE VENTAS SET TIMBRADA = 'S', QRCODE = 'S', ENVIADA = 'S', UUID = '" + uuid + "', PDF = '" + pdf + "', XML = '" + xml + "', TRANSACTION_ID = " + (getTransaction() + 1) + " WHERE SERIE = 'A' AND FOLIO = 6933";
        con = new Conexion();
        
        if(con.actualiza(actDatos)) return true;
        else return false;
        //System.out.println(serie + folio + " " + uuid);
    }
    
    public boolean actualizaEnvio(String serie, int folio)
    {
        String q = "UPDATE VENTAS SET TIMBRADA = 'S' WHERE SERIE = '" + serie + "' AND FOLIO = " + folio;
        con = new Conexion();
        
        if(con.actualiza(q)) return true;
        else return false;
    }
    
    public void getVenta(String serie, int folio)
    {
        String q = "SELECT VENTA FROM VENTAS WHERE SERIE = '" + serie + "' AND FOLIO = " + folio;
        int venta = 0; 
        con = new Conexion();
        
        try
        {
            ResultSet rs = con.getDatos(q);
            while(rs.next())
            {
                venta = rs.getInt("VENTA");
            }
            rs.close();
            con.desconectar();
        }catch(Exception e){}
    }
    
    public boolean actualizaCodigoSat(String cod_prod, String cod_sat)
    {
        //String act_codigos = "UPDATE PRODUCTOS SET COD_SAT = '" + cod_sat + "' WHERE CODIGO = '" + cod_prod + "'";
        String act_codigos = "INSERT INTO CODIGOS_SAT (COD_PROD, COD_SAT) VALUES ('" + cod_prod + "', '" + cod_sat + "')"; 
        
        con = new Conexion();
        
        
        if(con.inserta(act_codigos)) return true;
        else return false;
    }
    
    public float getTotal()
    {
        return total;
    }
    
    public float getIvaTot()
    {
        return ivat;
    }
    
    public String[] getInfoFact(int venta)
    {
        String[] info = new String[2];
        String q = "SELECT TIMBRADA, ENVIADA FROM VENTAS WHERE VENTA = " + venta;
        con = new Conexion();
        
        try
        {
            ResultSet rs = con.getDatos(q);
            while(rs.next())
            {
                info[0] = rs.getString("TIMBRADA");
                info[1] = rs.getString("ENVIADA");
            }
            rs.close();
            con.desconectar();
            
        }catch(Exception ex){}
        return info;
    }
}