/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sd.projeto1.main;

import com.sd.projeto1.dao.MapaDao;
import com.sd.projeto1.model.Mapa;
import com.sd.projeto1.model.MapaDTO;
import com.sd.projeto1.util.PropertyManagement;
import com.sd.projeto1.util.Utilidades;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.SerializationUtils;

/**
 *
 * @author willi
 */
public class ServerThreadDisco implements Runnable {
    private Operacoes crud = new Operacoes();
    private DatagramSocket socketServidor;
    private static PropertyManagement pm;
    private static byte[] in;
    private MapaDao mapaDAO = new MapaDao();
    private ExecutorService executor;
//    private io.grpc.stub.StreamObserver<ComandResponse> responseObserverGrpc;

    /// Recebendo o pacote da Thread Anterior;
    ServerThreadDisco(DatagramSocket socketServidor) {
        this.socketServidor = socketServidor;
    }

    @Override
    public void run() {
        try {
            executor = Executors.newCachedThreadPool();
            pm = new PropertyManagement();
            //socketServidor = new DatagramSocket(pm.getPort());

            while (true) {
                in = new byte[1400];
                DatagramPacket receivedPacket = MultiQueue.getDiscoFila();
                if(receivedPacket != null){
                    
                    Mapa maparetorno = new Mapa();
                    maparetorno = (Mapa) SerializationUtils.deserialize(receivedPacket.getData());

                    MapaDTO mapaDisco = new MapaDTO();
                    mapaDisco = tipoOperacao(maparetorno);

                    ServerThreadSend serverSend = new ServerThreadSend(mapaDisco, socketServidor);

                    if (serverSend != null) {
                        executor.execute(serverSend);
                    }
                }
                
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    public void imprimeCRUD(Mapa mapa1) {
        System.out.println("\n===============================");
        System.out.println("Chave: " + mapa1.getChave());
        System.out.println("Texto: " + mapa1.getTexto());
        System.out.println("Tipo de Operaçao: " + Utilidades.retornaTipoOperacao(mapa1.getTipoOperacaoId()));
        System.out.println("Data: " + mapa1.getData());
        System.out.println("Tamanho da fila: " + crud.getMapa().size());
        System.out.println("===============================");
    }

    public MapaDTO tipoOperacao(Mapa mapaEntity) throws Exception {

        MapaDTO mapaDTO = new MapaDTO();

        switch (mapaEntity.getTipoOperacaoId()) {
            case 1:
                
                Mapa mbi = mapaDAO.buscarPorId(mapaEntity.getChave());
                
                if(mbi.getChave() != 0){
                   mapaDTO.setMensagem("Já existe mensagem com essa chave!");
                   break;
                }
                    
                
                Mapa mi = mapaDAO.salvar(mapaEntity);
                
                if (mi != null) {
                    mapaDTO.setMapa(mi);
                    crud.salvar(mi);
                    imprimeCRUD(mi);
                    mapaDTO.setMensagem("Inserido com Sucesso!");

                } else {
                    mapaDTO.setMensagem("Erro ao inserir!");
                }
                break;
            case 2:
                Mapa ma = mapaDAO.editar(mapaEntity);
                
                if (ma != null) {
                    mapaDTO.setMapa(ma);
                    crud.editar(ma);
                    imprimeCRUD(ma);
                    mapaDTO.setMensagem("Atualizado com Sucesso!");

                } else {
                    mapaDTO.setMensagem("Erro ao atualizar!");
                }
                break;
            case 3:
                Mapa me = mapaDAO.excluir(mapaEntity.getChave());
                
                if (me != null) {
                    me.setTipoOperacaoId(3);
                    mapaDTO.setMapa(me);
                    crud.excluir(me);
                    imprimeCRUD(me);
                    mapaDTO.setMensagem("Excluido com Sucesso!");

                } else {
                    mapaDTO.setMensagem("Chave não encontrada para excluir!");
                }
                break;
            case 4:
                Mapa mb = mapaDAO.buscarPorId(mapaEntity.getChave());
                
                if (mb.getChave() != 0) {
                    mb.setTipoOperacaoId(4);
                    mapaDTO.setMapa(mb);

                    imprimeCRUD(mb);
                    mapaDTO.setMensagem("Recuperado com Sucesso!");

                } else {
                    mapaDTO.setMensagem("Erro ao recuperar!");
                }
                break;
            case 5:
//                ComandResponse rspGrpc = ComandResponse.newBuilder().setCmd(mapaEntity.getChave() + " " + Utilidades.retornaTipoOperacao(mapaEntity.getTipoOperacaoId())).build();
//                this.responseObserverGrpc.onNext(rspGrpc);
//                this.responseObserverGrpc.onCompleted();
                break;
            default:
                mapaDTO.setMapa(null);
                mapaDTO.setMensagem("Opção inválida");

        }

        return mapaDTO;
    }

//    public io.grpc.stub.StreamObserver<ComandResponse> getResponseObserverGrpc() {
//        return responseObserverGrpc;
//    }
//    
//    public void setResponseObserverGrpc(io.grpc.stub.StreamObserver<ComandResponse> responseObserverGrpc) {
//        this.responseObserverGrpc = responseObserverGrpc;
//    }
}
