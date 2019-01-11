package ventanas;

import com.sun.glass.events.KeyEvent;
import datos.Funciones;
import datos.Conexion;
import datos.Errores;
import instancias.Items;
import instancias.Factura;
import reportes.GenerarArchivo;
import datos.GeneraCorreo;

import java.sql.ResultSet;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import java.awt.Desktop;
import java.awt.Dialog.ModalityType;

public class Principal extends javax.swing.JFrame {
    
    static final int SEGUNDOS = 15;
    List<Items> items;
    String descuento;
    Funciones fun = new Funciones();
    GeneraCorreo gc = new GeneraCorreo();

    public Principal() {
        initComponents();
        descuento = "";
        llenaComboSeries();
        datosEmpresa();
        btn_genFact.setEnabled(false);
        this.setIconImage(new ImageIcon(getClass().getResource("/resources/icon_fact.png")).getImage());
    }
    
    public void ponDatos(int venta)
    {
        limpiaDatos();
        
        String [] datos = fun.buscaCte(fun.getCliente(venta));
        float [] totales = fun.getIvaTotal(venta);
        text_fecha.setText(fun.getFecha(venta));
        text_Cte.setText(datos[0].trim());
        text_nmbrReceptor.setText(datos[1].trim());       
        text_Dcto.setText(String.format("%.02f", totales[2]));
        text_iva0.setText(String.format("%.02f", totales[4]));
    }
    
    public void llenaTabla(int venta)
    {
        limpiaTabla();
        ponDatos(venta);
        Funciones fun = new Funciones();
        DefaultTableModel modeloTabla = (DefaultTableModel) tabla_items.getModel();
        items = fun.getVentas(venta);
        Object[] fila = new Object[modeloTabla.getColumnCount()];
        
        for(int i = 0 ; i < items.size() ; i++)
        {
            fila[0] = items.get(i).getClave();
            fila[1] = items.get(i).getCantidad();
            fila[2] = items.get(i).getUnidad();
            fila[3] = items.get(i).getDescripcion();
            fila[4] = items.get(i).getLote();
            fila[5] = items.get(i).getCaducidad();
            fila[6] = items.get(i).getPrecio_unitario();
            fila[7] = items.get(i).getImporte();
            
            modeloTabla.addRow(fila);
        }
        
        float iva = fun.getIvaTot();
        text_iva.setText(String.format("%.02f", iva));
        
        float iva_p = Float.parseFloat(text_iva.getText()) * .16f;
        text_ivaP.setText(String.format("%.02f", iva_p));
        
        float sub_total = Float.parseFloat(text_iva0.getText()) + Float.parseFloat(text_iva.getText());
        text_subTotal.setText(String.format("%.02f", sub_total));
                
        float total = Float.parseFloat(text_subTotal.getText()) + Float.parseFloat(text_ivaP.getText()) + Float.parseFloat(text_Dcto.getText());  
        text_total.setText(String.format("%.02f", total));
    }
    
    public void llenaComboSeries()
    {
        String q = "SELECT SUCURSAL_SERIE FROM CONFIGURACION";
        Conexion con = new Conexion();
        DefaultComboBoxModel modeloComboLab = new DefaultComboBoxModel();
        combo_Series.setModel(modeloComboLab);
        try
        {
            ResultSet rs = con.getDatos(q);
            while(rs.next())
            {
                modeloComboLab.addElement(rs.getString("SUCURSAL_SERIE").trim());
                combo_Series.setModel(modeloComboLab);
            }
            rs.close();
            con.desconectar();
        }catch(Exception e){}
    }
    
    public void datosEmpresa()
    {
        Funciones fun = new Funciones();
        String [] datos_empresa = fun.getDatosEmpresa();
        text_nombr_empresa.setText(datos_empresa[0]);
        text_lugar.setText(datos_empresa[1]);
        text_rfcEmisor.setText(datos_empresa[2]);
        text_CP.setText(datos_empresa[3]);
    }
    
    public void limpiaTabla()
    {
        DefaultTableModel temp;
        try
        {
            temp = (DefaultTableModel) tabla_items.getModel();
            int a = temp.getRowCount();
            for(int i = 0 ; i < a ; i++)
                temp.removeRow(0);
        }catch(Exception e){ }
    }
    
    public void limpiaDatos()
    {
        text_fecha.setText("");
        text_Cte.setText("");
        text_subTotal.setText("");
        text_total.setText("");
    }
    
    public void verificaPdf(String nom_arch, String forma_pago)
    {
        String user = System.getProperty("user.name");
        File f = new File("C:\\Users\\" + user + "\\Desktop\\FacturasCreadas\\" + nom_arch);
        String[] correos;
                
        Popup pop = new Popup(this, true);
        pop.setModalityType(ModalityType.MODELESS);
        pop.setVisible(true);
        esperar();
        pop.dispose();
        
        if(f.exists()){
            String mail = fun.getCorreoCte(combo_Series.getSelectedItem().toString(), Integer.parseInt(text_venta.getText().trim()));
            if(mail.trim().equals("*"))
            {
                correos = new String[1];
                correos[0] = "lalo_ubera20@hotmail.com";
                //correos[0] = "facturacion_farmacias@farmadelta.com.mx";
            }
            else
            {
                correos = new String[2];
                correos[0] = "lalo_ubera20@hotmail.com";
                correos[1] = "ubera39@gmail.com";
                //correos[0] = "facturacion_farmacias@farmadelta.com.mx";
                //correos[1] = mail;
            }
            
            JOptionPane.showMessageDialog(this, "Factura creada ", "Mensaje", JOptionPane.INFORMATION_MESSAGE);
            int venta = fun.getVenta(Integer.parseInt(text_venta.getText().trim()), combo_Series.getSelectedItem().toString());
            
            try
            {
                Desktop.getDesktop().open(f);
            }catch(Exception ex){}
            
            fun.actualizaTimbrado(combo_Series.getSelectedItem().toString(), Integer.parseInt(text_venta.getText()), forma_pago);
            
            if(gc.enviarCorreo(combo_Series.getSelectedItem().toString() + text_venta.getText(), correos))
                fun.actualizaEnvio(combo_Series.getSelectedItem().toString(), Integer.parseInt(text_venta.getText()));
            
            String[] info_fact = fun.getInfoFact(venta);
            if(info_fact[0].trim().equals("S") && info_fact[1].trim().equals("S"))
            {
                text_timbrada.setText("SI");
                text_enviada.setText("SI");
                btn_genFact.setEnabled(false);
                btn_reenv.setEnabled(true);
            }
                    
            if(info_fact[0].trim().equals("S") && ! (info_fact[1].trim().equals("S")))
            {
                text_timbrada.setText("SI");
                text_enviada.setText("NO");
                btn_genFact.setEnabled(false);
                btn_reenv.setEnabled(true);
            }
                    
            if(info_fact[0].trim().equals("*") && info_fact[1].trim().equals("*"))
            {
                text_timbrada.setText("NO");
                text_enviada.setText("NO");
                btn_genFact.setEnabled(true);
                btn_reenv.setEnabled(false);
            }
        }
        else
        { 
            Errores err = new Errores();
            File arch_err = new File("C:\\SistemaFactura3.3\\ErrorFactura\\" + combo_Series.getSelectedItem().toString() + text_venta.getText()+ "-error timbrado.txt");
            if(arch_err.exists())
            {
                String error = err.getArchivoError("C:\\SistemaFactura3.3\\ErrorFactura\\" + combo_Series.getSelectedItem().toString() + text_venta.getText()+ "-error timbrado.txt");
                JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
            }
            else JOptionPane.showMessageDialog(this, "La factura no se creo", "Mensaje", JOptionPane.ERROR_MESSAGE);
        }  
    }
    
    public void esperar()
    {
        try
        {
            Thread.sleep(SEGUNDOS * 1000);
        }catch(InterruptedException e){}
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        text_nombr_empresa = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        text_fecha = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        text_lugar = new javax.swing.JTextArea();
        jLabel5 = new javax.swing.JLabel();
        text_rfcEmisor = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        text_Cte = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabla_items = new javax.swing.JTable();
        text_subTotal = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        text_iva = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        text_total = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        text_venta = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        combo_Series = new javax.swing.JComboBox<>();
        obtenVenta = new javax.swing.JButton();
        btn_genFact = new javax.swing.JButton();
        text_nmbrReceptor = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        text_CP = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        text_Dcto = new javax.swing.JTextField();
        text_ivaP = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        text_iva0 = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        comboRegimen = new javax.swing.JComboBox<>();
        jLabel19 = new javax.swing.JLabel();
        comboMetPago = new javax.swing.JComboBox<>();
        jLabel21 = new javax.swing.JLabel();
        comboUsoCfdi = new javax.swing.JComboBox<>();
        btn_ActCods = new javax.swing.JButton();
        cmb_FormaPago = new javax.swing.JComboBox<>();
        jLabel20 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        text_timbrada = new javax.swing.JTextField();
        text_enviada = new javax.swing.JTextField();
        btn_reenv = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Sistema de Facturas");
        setBackground(new java.awt.Color(255, 255, 255));
        setLocation(new java.awt.Point(10, 10));
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(153, 255, 255));
        jPanel1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel1.setText("Nombre de la empresa:");

        text_nombr_empresa.setEditable(false);
        text_nombr_empresa.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel2.setText("Lugar de expedicion:");

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel3.setText("Fecha de expedición:");

        text_fecha.setEditable(false);
        text_fecha.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

        text_lugar.setEditable(false);
        text_lugar.setColumns(20);
        text_lugar.setRows(5);
        jScrollPane2.setViewportView(text_lugar);

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel5.setText("RFC Emisor: ");

        text_rfcEmisor.setEditable(false);
        text_rfcEmisor.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel6.setText("RFC Receptor:");

        text_Cte.setEditable(false);
        text_Cte.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

        jLabel7.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel7.setText("Régimen Fiscal:");

        jLabel8.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel8.setText("Tipo de Comprobante:");

        jTextField4.setEditable(false);
        jTextField4.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jTextField4.setText("INGRESO");

        tabla_items.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "CLAVE", "CANTIDAD", "UNIDAD", "DESCRIPCIÓN", "LOTE", "FECHA CADUCIDAD", "PRECIO UNITARIO", "IMPORTE"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tabla_items.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(tabla_items);

        text_subTotal.setEditable(false);
        text_subTotal.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

        jLabel9.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel9.setText("Sub-total:");

        text_iva.setEditable(false);
        text_iva.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

        jLabel10.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel10.setText("I.V.A 16%:");

        jLabel11.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel11.setText("Total:");

        text_total.setEditable(false);
        text_total.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

        jLabel12.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel12.setText("Folio Factura #:");

        text_venta.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        text_venta.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                text_ventaKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                text_ventaKeyTyped(evt);
            }
        });

        jLabel13.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel13.setText("Serie:");

        combo_Series.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        combo_Series.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        obtenVenta.setText("Aceptar");
        obtenVenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                obtenVentaActionPerformed(evt);
            }
        });

        btn_genFact.setText("Generar Factura");
        btn_genFact.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_genFactActionPerformed(evt);
            }
        });

        text_nmbrReceptor.setEditable(false);
        text_nmbrReceptor.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

        jLabel14.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel14.setText("Nombre Receptor:");

        jLabel15.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel15.setText("Forma de Pago:");

        jLabel16.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel16.setText("Código Postal:");

        text_CP.setEditable(false);
        text_CP.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

        jLabel17.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel17.setText("Desuento:");

        text_Dcto.setEditable(false);
        text_Dcto.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

        text_ivaP.setEditable(false);
        text_ivaP.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel4.setText("I.V.A:");

        text_iva0.setEditable(false);
        text_iva0.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

        jLabel18.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel18.setText("I.V.A 0%:");

        comboRegimen.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        comboRegimen.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "601, General de Ley Personas Morales" }));

        jLabel19.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel19.setText("Metodo de Pago:");

        comboMetPago.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        comboMetPago.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "PUE, Pago en una sola exhibición", "PIP, Pago inicial y parcialidades", "PPD, Pago en parcialidades o diferido" }));

        jLabel21.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel21.setText("Uso CFDI:");

        comboUsoCfdi.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        comboUsoCfdi.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "P01, Por definir", "G01, Adquisición de mercancias", "G02, Devoluciones, descuentos o bonificaciones", "G03, Gastos en general", "D01, Honorarios médicos, dentales y gastos hospitalarios.", "D02, Gastos médicos por incapacidad o discapacidad", "D07, Primas por seguros de gastos médicos." }));

        btn_ActCods.setText("Establecer Codigo SAT");
        btn_ActCods.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_ActCodsActionPerformed(evt);
            }
        });

        cmb_FormaPago.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        cmb_FormaPago.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "01, Efectivo", "02, Cheque nominativo", "03, Transferencia electrónica de fondos", "04, Tarjeta de crédito", "05, Monedero electrónico", "28, Tarjeta de débito", "29, Tarjeta de servicios", "30, Aplicacion de Anticipos", "99, Por definir" }));

        jLabel20.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel20.setText("Timbrada");

        jLabel22.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel22.setText("Enviada");

        text_timbrada.setEditable(false);
        text_timbrada.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        text_timbrada.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                text_timbradaKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                text_timbradaKeyTyped(evt);
            }
        });

        text_enviada.setEditable(false);
        text_enviada.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        text_enviada.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                text_enviadaKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                text_enviadaKeyTyped(evt);
            }
        });

        btn_reenv.setText("Reenviar");
        btn_reenv.setEnabled(false);
        btn_reenv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_reenvActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addComponent(btn_genFact)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(btn_reenv, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel18, javax.swing.GroupLayout.Alignment.TRAILING)))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addGap(0, 0, Short.MAX_VALUE)
                                        .addComponent(jLabel9)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(text_subTotal)
                                    .addComponent(text_iva)
                                    .addComponent(text_iva0, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jScrollPane1)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(jLabel13)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(combo_Series, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(jLabel12)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(text_venta, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(obtenVenta)
                                    .addGap(34, 34, 34)
                                    .addComponent(jLabel20)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(text_timbrada, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(jLabel22)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(text_enviada, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btn_ActCods)
                                    .addGap(229, 229, 229))
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel1)
                                        .addComponent(jLabel16)
                                        .addComponent(jLabel3)
                                        .addComponent(jLabel5)
                                        .addComponent(jLabel6)
                                        .addComponent(jLabel14))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 463, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(text_nombr_empresa, javax.swing.GroupLayout.PREFERRED_SIZE, 900, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(text_nmbrReceptor, javax.swing.GroupLayout.PREFERRED_SIZE, 463, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(text_rfcEmisor, javax.swing.GroupLayout.PREFERRED_SIZE, 463, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(text_Cte, javax.swing.GroupLayout.PREFERRED_SIZE, 463, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(text_CP, javax.swing.GroupLayout.PREFERRED_SIZE, 463, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(text_fecha, javax.swing.GroupLayout.PREFERRED_SIZE, 463, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGap(52, 52, 52)
                                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(jPanel1Layout.createSequentialGroup()
                                                    .addComponent(jLabel21)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(comboUsoCfdi, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addGroup(jPanel1Layout.createSequentialGroup()
                                                    .addComponent(jLabel19)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(comboMetPago, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addGap(0, 0, Short.MAX_VALUE))))))
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(jLabel2)
                                    .addGap(547, 547, 547)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                            .addComponent(jLabel7)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(comboRegimen, javax.swing.GroupLayout.PREFERRED_SIZE, 329, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel15)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cmb_FormaPago, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel8)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 257, javax.swing.GroupLayout.PREFERRED_SIZE))))))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel17, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(text_total, javax.swing.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE)
                            .addComponent(text_ivaP)
                            .addComponent(text_Dcto))))
                .addContainerGap(41, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(text_venta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13)
                    .addComponent(combo_Series, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(obtenVenta)
                    .addComponent(btn_ActCods)
                    .addComponent(text_timbrada, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20)
                    .addComponent(jLabel22)
                    .addComponent(text_enviada, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(text_nombr_empresa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addComponent(jLabel2))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(comboRegimen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel15)
                            .addComponent(cmb_FormaPago, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(text_CP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16)
                    .addComponent(jLabel19)
                    .addComponent(comboMetPago, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(14, 14, 14)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel21)
                        .addComponent(comboUsoCfdi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(text_fecha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel3)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(text_rfcEmisor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(text_Cte, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(text_nmbrReceptor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(text_iva0, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel18))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(text_iva, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(text_subTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(text_Dcto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel17))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(text_ivaP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(text_total, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel11))
                        .addGap(23, 23, 23))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btn_genFact, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btn_reenv, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 726, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void text_ventaKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_text_ventaKeyTyped
        
        char c = evt.getKeyChar();
        if(c < '0' || c > '9') evt.consume();  
    }//GEN-LAST:event_text_ventaKeyTyped

    private void obtenVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_obtenVentaActionPerformed
        
        if(text_venta.getText().equals("")) JOptionPane.showMessageDialog(this, "El Campo Folio Está Vacio", "Error", JOptionPane.ERROR_MESSAGE);
        else
        {
            Funciones f = new Funciones();
            int venta = f.getVenta(Integer.parseInt(text_venta.getText().trim()), combo_Series.getSelectedItem().toString());

            if(venta == Integer.MIN_VALUE)
            {
                text_timbrada.setText("");
                text_enviada.setText("");
                JOptionPane.showMessageDialog(this, "LA VENTA NO EXISTE!!", "Error en los datos", JOptionPane.ERROR_MESSAGE);
            }
            else
            {
                String[] info_fact = f.getInfoFact(venta);
                if(info_fact[0].trim().equals("S") && info_fact[1].trim().equals("S"))
                {
                    text_timbrada.setText("SI");
                    text_enviada.setText("SI");
                    btn_genFact.setEnabled(false);
                    btn_reenv.setEnabled(true);
                }
                    
                if(info_fact[0].trim().equals("S") && info_fact[1].trim().equals("*"))
                {
                    text_timbrada.setText("SI");
                    text_enviada.setText("NO");
                    btn_genFact.setEnabled(false);
                    btn_reenv.setEnabled(true);
                }
                    
                if(! (info_fact[0].trim().equals("S")) && ! (info_fact[1].trim().equals("S")))
                {
                    text_timbrada.setText("NO");
                    text_enviada.setText("NO");
                    btn_genFact.setEnabled(true);
                    btn_reenv.setEnabled(false);
                }
                llenaTabla(venta);
            }
        }
    }//GEN-LAST:event_obtenVentaActionPerformed

    private void btn_genFactActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_genFactActionPerformed

        String regimen = comboRegimen.getSelectedItem().toString();
        String[] datos = regimen.split(",");
        
        String met_pag = comboMetPago.getSelectedItem().toString();
        String[] metodo_pago = met_pag.split(",");
                
        String uso = comboUsoCfdi.getSelectedItem().toString();
        String[] uso_cfdi = uso.split(",");
        
        String forma_pago = cmb_FormaPago.getSelectedItem().toString();
        String[] fp = forma_pago.split(",");
        
        Factura fact = new Factura(combo_Series.getSelectedItem().toString(), text_venta.getText(), fp[0], text_subTotal.getText(), text_Dcto.getText(), text_total.getText(), text_CP.getText(), text_rfcEmisor.getText(), 
                       text_nombr_empresa.getText(), datos[0], text_Cte.getText(), text_nmbrReceptor.getText(), metodo_pago[0], uso_cfdi[0], items);
        
        GenerarArchivo  ga = new GenerarArchivo(fact, text_iva.getText(), text_ivaP.getText());
        if(ga.creaTXT())
        { 
            verificaPdf(combo_Series.getSelectedItem().toString() + text_venta.getText() + ".pdf", fp[0]);   
        }
        else JOptionPane.showMessageDialog(this, "No se pudo generar el archivo", "Error", JOptionPane.ERROR_MESSAGE);
    }//GEN-LAST:event_btn_genFactActionPerformed

    private void text_ventaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_text_ventaKeyPressed
        // TODO add your handling code here:
        if(evt.getKeyCode() == KeyEvent.VK_ENTER)
        {
            if(text_venta.getText().equals("")) JOptionPane.showMessageDialog(this, "El Campo Folio Está Vacio", "Error", JOptionPane.ERROR_MESSAGE);
            else
            {
                Funciones f = new Funciones();
                int venta = f.getVenta(Integer.parseInt(text_venta.getText().trim()), combo_Series.getSelectedItem().toString());
                if(venta == Integer.MIN_VALUE)
                {
                    text_timbrada.setText("");
                    text_enviada.setText("");
                    limpiaTabla();
                    JOptionPane.showMessageDialog(this, "LA VENTA NO EXISTE!!", "Error en los datos", JOptionPane.ERROR_MESSAGE);
                }
                
                else
                {
                    String[] info_fact = f.getInfoFact(venta);
                    if(info_fact[0].trim().equals("S") && info_fact[1].trim().equals("S"))
                    {
                        text_timbrada.setText("SI");
                        text_enviada.setText("SI");
                        btn_genFact.setEnabled(false);
                        btn_reenv.setEnabled(true);
                    }
                    
                    if(info_fact[0].trim().equals("S") && info_fact[1].trim().equals("*"))
                    {
                        text_timbrada.setText("SI");
                        text_enviada.setText("NO");
                        btn_genFact.setEnabled(false);
                        btn_reenv.setEnabled(true);
                    }
                    
                    if(! (info_fact[0].trim().equals("S")) && ! (info_fact[1].trim().equals("S")))
                    {
                        text_timbrada.setText("NO");
                        text_enviada.setText("NO");
                        btn_genFact.setEnabled(true);
                        btn_reenv.setEnabled(false);
                    }
                    llenaTabla(venta);
                }
            }
        }
    }//GEN-LAST:event_text_ventaKeyPressed

    private void btn_ActCodsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_ActCodsActionPerformed
        // TODO add your handling code here:
        PonCodigoSat pcs = new PonCodigoSat(this, true);
        pcs.show();
    }//GEN-LAST:event_btn_ActCodsActionPerformed

    private void text_timbradaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_text_timbradaKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_text_timbradaKeyPressed

    private void text_timbradaKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_text_timbradaKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_text_timbradaKeyTyped

    private void text_enviadaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_text_enviadaKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_text_enviadaKeyPressed

    private void text_enviadaKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_text_enviadaKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_text_enviadaKeyTyped

    private void btn_reenvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_reenvActionPerformed
        // TODO add your handling code here:
        String user = System.getProperty("user.name");
        if(text_venta.getText().equals("")) JOptionPane.showMessageDialog(this, "El Campo Folio Está Vacio", "Error", JOptionPane.ERROR_MESSAGE);
        else
        {
            File file1 = new File("C:\\Users\\" + user + "\\Desktop\\FacturasCreadas\\" + combo_Series.getSelectedItem().toString() + text_venta.getText() + ".pdf");
            File file2 = new File("C:\\Users\\" + user + "\\Desktop\\FacturasCreadas\\" + combo_Series.getSelectedItem().toString() + text_venta.getText() + ".xml");

            if(file1.exists() && file2.exists())
            {
                String[] correos;
                String mail = fun.getCorreoCte(combo_Series.getSelectedItem().toString(), Integer.parseInt(text_venta.getText().trim()));
                if(mail.trim().equals("*"))
                {
                    correos = new String[1];
                    //correos[0] = "lalo_ubera20@hotmail.com";
                    correos[0] = "facturacion_farmacias@farmadelta.com.mx";
                }
                else
                {
                    correos = new String[2];
                    //correos[0] = "lalo_ubera20@hotmail.com";
                    //correos[1] = "ubera39@gmail.com";
                    correos[0] = "facturacion_farmacias@farmadelta.com.mx";
                    correos[1] = mail;
                }

                if(gc.enviarCorreo(combo_Series.getSelectedItem().toString() + text_venta.getText(), correos))
                {
                    fun.actualizaEnvio(combo_Series.getSelectedItem().toString(), Integer.parseInt(text_venta.getText()));
                    int venta = fun.getVenta(Integer.parseInt(text_venta.getText().trim()), combo_Series.getSelectedItem().toString());
                    String[] info_fact = fun.getInfoFact(venta);
                    if(info_fact[0].equals("S") && info_fact[1].equals("S"))
                    {
                        text_timbrada.setText("SI");
                        text_enviada.setText("SI");
                        btn_genFact.setEnabled(false);
                        btn_reenv.setEnabled(true);
                    }

                    if(info_fact[0].equals("S") && ! (info_fact[1].equals("S")))
                    {
                        text_timbrada.setText("SI");
                        text_enviada.setText("NO");
                        btn_genFact.setEnabled(false);
                        btn_reenv.setEnabled(true);
                    }

                    if(! (info_fact[0].equals("S")) && ! (info_fact[1].equals("S")))
                    {
                        text_timbrada.setText("NO");
                        text_enviada.setText("NO");
                        btn_genFact.setEnabled(true);
                        btn_reenv.setEnabled(false);
                    }
                    JOptionPane.showMessageDialog(this, "Factura Enviada", "Mensaje", JOptionPane.INFORMATION_MESSAGE); 
                }
                else JOptionPane.showMessageDialog(this, "No se pudo enviar la factura", "Mensaje", JOptionPane.ERROR_MESSAGE); 
            }
            else JOptionPane.showMessageDialog(this, "La factura no existe", "Mensaje", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btn_reenvActionPerformed

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            javax.swing.UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Principal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Principal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Principal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Principal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Principal().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_ActCods;
    private javax.swing.JButton btn_genFact;
    private javax.swing.JButton btn_reenv;
    private javax.swing.JComboBox<String> cmb_FormaPago;
    private javax.swing.JComboBox<String> comboMetPago;
    private javax.swing.JComboBox<String> comboRegimen;
    private javax.swing.JComboBox<String> comboUsoCfdi;
    private javax.swing.JComboBox<String> combo_Series;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JButton obtenVenta;
    private javax.swing.JTable tabla_items;
    private javax.swing.JTextField text_CP;
    private javax.swing.JTextField text_Cte;
    private javax.swing.JTextField text_Dcto;
    private javax.swing.JTextField text_enviada;
    private javax.swing.JTextField text_fecha;
    private javax.swing.JTextField text_iva;
    private javax.swing.JTextField text_iva0;
    private javax.swing.JTextField text_ivaP;
    private javax.swing.JTextArea text_lugar;
    private javax.swing.JTextField text_nmbrReceptor;
    private javax.swing.JTextField text_nombr_empresa;
    private javax.swing.JTextField text_rfcEmisor;
    private javax.swing.JTextField text_subTotal;
    private javax.swing.JTextField text_timbrada;
    private javax.swing.JTextField text_total;
    private javax.swing.JTextField text_venta;
    // End of variables declaration//GEN-END:variables
}