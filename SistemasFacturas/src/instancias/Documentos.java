package instancias;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;

public class Documentos 
{
    public Documentos(){}
    
    public static StringBuilder capturarTextoDeArchivo(String name_file) 
    {
        File archivo = null;
        FileReader fr = null;
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        
        try {
            archivo = new File(name_file);
            fr = new FileReader(archivo);
            br = new BufferedReader(fr);
            String linea = "";            
            while ((linea = br.readLine()) != null) {
                sb.append(linea).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != fr) {
                    fr.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return sb;
    }
    
    public static StringBuilder extraerTexto2(StringBuilder sb) 
    {
        int posIni = sb.indexOf("UUID");
        int posFin = sb.indexOf("FechaTimbrado");
        StringBuilder extracto = new StringBuilder(); 
        if(posIni > -1 && posFin > -1) {
            extracto.append(sb.substring(posIni + 5, posFin).trim());   
        }
        return extracto;
    }
    
    public String getUuid(String nom_arch)
    {
        //StringBuilder sb = extraerTexto2(capturarTextoDeArchivo("C:\\Users\\Jonathan\\Documents\\ProgramEdu\\EjmValidos\\AB46.xml"));
        String user = System.getProperty("user.name");
        StringBuilder sb = extraerTexto2(capturarTextoDeArchivo("C:\\Users\\" + user + "\\Desktop\\FacturasCreadas\\" + nom_arch + ".xml"));
        String uuid = sb.toString();
        String uuid1 = uuid.replace("\"", "");
        
        return uuid1;
    }   
}