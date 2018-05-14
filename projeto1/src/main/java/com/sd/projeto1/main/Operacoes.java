/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sd.projeto1.main;

import com.sd.projeto1.model.Mapa;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Willian
 */
public class Operacoes {
    
     private static Map<BigInteger, String> mapa = new HashMap();
    
    public void salvar(Mapa mapa1) {
        BigInteger chave = new BigInteger(String.valueOf(mapa1.getChave()));

        if (mapa.containsKey(mapa1.getChave())) {
            System.out.println("Mensagem com essa chave já adicionada");
        }

        mapa.put(chave, mapa1.getTexto());
    }

    public void editar(Mapa mapa1) {
        BigInteger chave = new BigInteger(String.valueOf(mapa1.getChave()));

        if (mapa.containsKey(chave)) {
            mapa.replace(chave, mapa1.getTexto());
            return;
        }
        System.out.println("Chave não encontrada");       
    }

    public void excluir(Mapa mapa1) {
        BigInteger chave = new BigInteger(String.valueOf(mapa1.getChave()));

        mapa.remove(chave);
    }

    public String buscar(Mapa mapa1) {
        BigInteger chave = new BigInteger(String.valueOf(mapa1.getChave()));
        
        return mapa.get(chave);
    }

    public Map<BigInteger, String> getMapa() {
        return mapa;
    }
    
}
