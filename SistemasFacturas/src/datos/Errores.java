package datos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Errores 
{
    public String getArchivoError(String filename)
    {
        String line = "";
        String line2= "";
        
        try
        {
            File archivo = new File(filename);
            FileReader fr = new FileReader(archivo);
            BufferedReader br = new BufferedReader(fr);
            
            while((line = br.readLine())!= null)
            {
                line2 = line2 + "\n" + line;
            }
            br.close();
            fr.close();
            
            line2 = line2.replace(',', '\n');
        }catch(Exception ex){}
        return line2.trim();
    }
}
