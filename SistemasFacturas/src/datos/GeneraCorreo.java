package datos;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class GeneraCorreo 
{
    public boolean enviarCorreo(String num_factura, String []mail_cte)
    {
        try
        {
            String user = System.getProperty("user.name");
            String[] dividido_adj,dividido_adj_nom;
            String patron =";"; 
            dividido_adj = ("C:\\Users\\" + user + "\\Desktop\\FacturasCreadas\\" + num_factura + ".pdf;C:\\Users\\" + user + "\\Desktop\\FacturasCreadas\\" + num_factura + ".xml").split(patron);        
            dividido_adj_nom = (num_factura + ".pdf;" + num_factura + ".xml").split(patron);
            
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "false");
            props.put("mail.user", "facturas.delta18@gmail.com");
            props.put("mail.password", "hfoseizejurqhqtz");
            
            Session s = Session.getDefaultInstance(props, null);
            BodyPart texto = new MimeBodyPart();
            texto.setText("");
            
            List<BodyPart> bp = new LinkedList<BodyPart>();//<-------creamos una lista de adjuntos
            // Se compone el adjunto con la imagen
            
             for(int i=0;i<=dividido_adj.length-1;i++)
             {
                BodyPart adjunto = new MimeBodyPart();
                adjunto.setDataHandler(new DataHandler(new FileDataSource(dividido_adj[i])));
                adjunto.setFileName(dividido_adj_nom[i]);
                bp.add(adjunto);//<----------------añadimos el elemento a la lista
             }

            // Una MultiParte para agrupar texto e imagen.
            MimeMultipart multiParte = new MimeMultipart();
            multiParte.addBodyPart(texto);

            Iterator it = bp.iterator();//<------------la iteramos
            while(it.hasNext())//<----------------la recorremos
            {
                BodyPart attach =(BodyPart)it.next();//<------------obtenemos el objeto
                multiParte.addBodyPart(attach);//<-----------------finalmente lo añadimos al mensaje
             }

             InternetAddress[] internetAddresses = new InternetAddress[mail_cte.length];
             for(int i = 0 ; i < internetAddresses.length ; i++)
                 internetAddresses[i] = new InternetAddress(mail_cte[i]);
                                 //new InternetAddress("ubera39@gmail.com"),
                                 //new InternetAddress(mail_cte) };

             MimeMessage mensaje = new MimeMessage(s);
             mensaje.setFrom(new InternetAddress("facturas.delta18@gmail.com"));
             mensaje.addRecipients(Message.RecipientType.TO, internetAddresses);
             mensaje.setContent(multiParte);

             Transport t = s.getTransport("smtp");
             t.connect("facturas.delta18@gmail.com", "hfoseizejurqhqtz");
             t.sendMessage(mensaje, mensaje.getAllRecipients());
             t.close();
             return true;
            
        }catch(Exception e){ return false;}
    }
}