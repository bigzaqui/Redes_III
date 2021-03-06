package redesiii;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Esta Clase representan cada uno de los usuarios(Clientes) que se
 * encuentran corriendo en los equipos a monitorizar.
 * 
 * Esta clase dispone de todas las implementaciones necesarias para la
 * ejecucon de scripts enviados por el servidor.
 * 
 * @author Cesar Freitas.
 * @author Edward Zambrano.
 */
public class Cliente extends UnicastRemoteObject implements Interfaz_Servidor_Cliente {

    public Interfaz_Cliente_Servidor servidor; //La interfaz de comunicacion
                                                //con el servidor.
    private int puerto; // El puerto donde se desea establecer
                        //la conexion.
    
    private LinkedList<String> procesos;
    /**
     * Constructor de la clase Cliente, esta permite inicializar todos 
     * los parametros necesarios para su funcionamiento.
     * 
     * @param ip Un String que representa la direccion ip de del servidor
     *           al cual esta maquina se va a registrar para ser monitorizada.
     * @param puerto Un entero que representa el numero de puerto donde se va
     *        a establecer la conexion.
     */
    public Cliente (String ip, int puerto) throws RemoteException {
        try {
            this.puerto = puerto;
            InetAddress direccion = InetAddress.getByName(ip);
            
            String direc = "rmi://" + ip + ":" + puerto + "/Servidor";
            servidor = (Interfaz_Cliente_Servidor) Naming.lookup(direc);
            
            procesos = new LinkedList<String>();
            
            this.leerProcesos("process");
            
        } catch (NotBoundException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        } 
       
        
    }
    
    
    /**
     * Este metodo permite obtener el numero de puerto de donde se establece
     * la conexion.
     * 
     * @return Un entero que representa el numero de puerto.
     *
     */
     public int getPuerto() {
        return puerto;
    }
    
     /**
     * Este metodo permite obtener una interfaz para interactuar con el
     * servidor remoto. 
     * 
     * @return Un objeto de interfaz de conexion cliente-servidor.
     *
     */
    public Interfaz_Cliente_Servidor getServidor() {
        return servidor;
    }
    /**
     * Este metodo permite proporcionar al servidor la lista de los
     * procesos que se encuentran activos en la una instacia de Cliente.
     * 
     * @return Un arreglo de Strings de tamanho 2, donde la posicion
     *          0 representa la salida estandar con los procesos listados 
     *          y la posicion 1 representa error estandar.
     */
    @Override
    public String[] verificar() {
        return ejecutar("ps -eo fname");
    }
    /**
     * Este metodo permite ejecutar un script de bash en una instacia
     * de Cliente.
     * 
     * @param script La representacion en String del Script a ejecutar.
     * 
     * @return Un arreglo de Strings de tamanho 2, donde la posicion
     *          0 representa la salida estandar y la posicion 1 representa
     *          error estandar.
     */
    @Override
    public String[] ejecutar(String script) {

        String s;
        String[] salida = new String[2];
        salida[0] = "";
        salida[1] = "";
        
        try {

            // Ejcutamos el comando
            Process p = Runtime.getRuntime().exec(script);

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));

            BufferedReader stdError = new BufferedReader(new InputStreamReader(
                    p.getErrorStream()));

            // Leemos la salida del comando
            System.out.println("Esta es la salida standard del comando:\n");
            while ((s = stdInput.readLine()) != null) {
                salida[0] += s + "\n";
            }
            System.out.println(salida[0]);
            // Leemos los errores si los hubiera
            System.out.println("Esta es la salida standard de error del comando (si la hay):\n");
            while ((s = stdError.readLine()) != null) {

                salida[1] += s + "\n";
            }

            System.out.println(salida[1]);

            

          
        } catch (IOException ex) {
            
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(0);
        }
        return salida;
    }
    
    public void leerProcesos(String arch) {
        try {
            BufferedReader buffer = new BufferedReader(new FileReader(System.getProperty("user.dir")+"/src/redesiii/"+arch));

            String linea;
            try {
                while ((linea = buffer.readLine()) != null) {
                    if (!linea.matches("\\s*")) { //se ignora lineas en blanco

                        procesos.add(linea);
                      

                    }
                }
            } catch (IOException ex) {
                System.out.println("Error de lectura sobre el archivo " + arch);
                System.exit(-1);
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Error al intentar abrir el archivo" + arch);
            System.exit(-1);
        }
    }
    
    /**
     * Este metodo permite proporcionar al servidor la lista de los
     * procesos criticos que se encuentran inactivos en la maquina
     * cliente.
     * 
     * @return Una lista con los nombres de los procesos inactivos.
     */
    @Override
     public LinkedList<String> verificarProcesos(){
    
        String[] salidas = this.ejecutar("ps -eo fname");
        
        String[] separado = salidas[0].split("\n");
        int n;
        
        
        LinkedList<String> falta = new LinkedList();
        
        for(String p:procesos){
            n=1;
            
            while(n<separado.length){
                
                if(separado[n].compareTo(p)==0){
                    System.out.println(p+" encontrado");
                    break;
                    
                }
                n++;
            }
            
            if(n>=separado.length){
                
                falta.addLast(p);
            }
        }
        
        for(String x:falta){
        
            System.out.println("no encontrado "+x);
        
        }
        
        
        return falta;
    
    }

/**
* Metodo principal de ejecucion del cliente.
*
* @param Un arreglo con los String que ingresaron por la entrada
*        estandar.
*/
public static void main(String[] args){
        try {
            
            System.setProperty(
                           "java.rmi.server.codebase",
                           "file:" + System.getProperty("user.dir") + "/");
                   Cliente maquina = new Cliente(args[0],Integer.parseInt(args[1]));        
                   java.rmi.registry.LocateRegistry.createRegistry(maquina.getPuerto());
                   String host = InetAddress.getLocalHost().toString().split("/")[1];

                    
                   Naming.rebind("rmi://" + host + ":" + maquina.getPuerto() + "/Maquina", maquina);
                  // maquina.servidor.registrar();
        } catch (MalformedURLException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
   
    
}


}
