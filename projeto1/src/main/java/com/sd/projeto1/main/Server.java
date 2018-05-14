package com.sd.projeto1.main;

import com.sd.projeto1.dao.MapaDao;
import com.sd.projeto1.model.Mapa;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server{
    
    private static MapaDao mapaDAO = new MapaDao();
    
    public static void main(String[] args) throws Exception {
        List<Mapa> logs = new ArrayList<Mapa>();
        Operacoes crud = new Operacoes();
        
        logs = mapaDAO.buscarTodos();
        
        for(Mapa m: logs){
            crud.salvar(m);
        }
        
        System.out.println("Log do Disco Recuperado");
        System.out.println("Tamanho do log: " + crud.getMapa().size() + "\n");
        
        
        System.out.println("Servidor Iniciado...");
        new Thread(new ServerThreadReceive()).start();
    }
   
}
