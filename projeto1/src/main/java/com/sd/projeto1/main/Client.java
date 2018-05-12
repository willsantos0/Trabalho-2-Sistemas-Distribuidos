package com.sd.projeto1.main;

import com.sd.projeto1.dao.MapaDao;
import com.sd.projeto1.model.Mapa;
import com.sd.projeto1.model.MapaDTO;
import com.sd.projeto1.util.PropertyManagement;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.SerializationUtils;

public class Client {

    private static Queue<DatagramPacket> comandos = new LinkedList<>();
    private static DatagramSocket socketCliente;
    private static InetAddress enderecoIP;
    private static MapaDao mapaDAO = new MapaDao();
    private static List<Integer> chavesMonitoradas = new ArrayList<Integer>();

    static PropertyManagement pm = new PropertyManagement();
    
    public static void main(String[] args) throws SocketException, UnknownHostException {

        socketCliente = new DatagramSocket();
        enderecoIP = InetAddress.getByName(pm.getAddress());
        byte[] receiveData = new byte[1400];

        ExecutorService executor = Executors.newCachedThreadPool();

        Thread receive = new Thread(new Runnable() {
            // metodo que recebe resposta do servidor
            @Override
            public void run() {
                try {
                    while (true) {
                        DatagramPacket pacoteRecebido = new DatagramPacket(receiveData, receiveData.length);
                        socketCliente.receive(pacoteRecebido);
                        //String msg = new String(pacoteRecebido.getData(), 0, pacoteRecebido.getLength());
                        MapaDTO maparetorno = (MapaDTO) SerializationUtils.deserialize(pacoteRecebido.getData());

                        if (maparetorno == null) {
                            System.out.println(maparetorno.getMensagem());
                        } else {
                            if (maparetorno.getMapa().getTipoOperacaoId() == 4) {
                                objetoRetornado(maparetorno);
                            } else {
                                System.out.println(maparetorno.getMensagem());
                            }
                        }

                    }
                } catch (IOException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        });

        Thread send = new Thread(new Runnable() {
            @Override
            public void run() {
                Scanner scanner = new Scanner(System.in);
                try {
                    while (true) {
                       //private List<Integer> chavesMonitoradas = new ArrayList<Integer>(); só pra lembrar o nome kk
                        List<Mapa> listaDeChaves = new ArrayList<Mapa>();
                        int opcao, i, troca=-1, chaveEscolhida;
                        int[] tamanhoListaChaves;
                        System.out.println("Digite 1 se deseja monitar alguma chave ");
                        System.out.print("--> ");
                        opcao = scanner.nextInt();
                        if (opcao == 1) {
                            do{
                               
                                listaDeChaves = mapaDAO.buscarTodos();
                                System.out.println("Quais chaves deseja monitorar?");
                                for (i = 0; i < listaDeChaves.size(); i++) {
                                        System.out.println(listaDeChaves.get(i).getChave());
                                }
                                System.out.print("--> ");
                                chaveEscolhida = scanner.nextInt();
                                chavesMonitoradas.add(chaveEscolhida);
                                
                                System.out.println("Deseja escolher mais uma chave para monitorar? (1-Sim/2-Não)");
                                opcao = scanner.nextInt();
                                if(opcao != 1){
                                    troca = 1;
                                }
                                
                            }while(troca!=1);
                        }

                        menu();
                        Thread.sleep(2000);
                    }

                } catch (IOException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        });

        executor.execute(receive);
        executor.execute(send);

        executor.shutdown();
    }

    public static DatagramPacket send(byte[] outData) throws IOException {

        DatagramPacket sendPacket = new DatagramPacket(outData, outData.length, enderecoIP, pm.getPort());
        socketCliente.send(sendPacket);

        return sendPacket;
    }

    public static DatagramPacket receive(byte[] inData) throws IOException {

        DatagramPacket in = new DatagramPacket(inData, inData.length);
        socketCliente.receive(in);

        return in;
    }

    public static void menu() throws Exception {

        int opcao = 0, chave = 0;
        String msg;
        BufferedReader mensagem;
        Mapa mapa;
        mensagem = new BufferedReader(new InputStreamReader(System.in));

        Scanner scanner = new Scanner(System.in);

        System.out.println("\n===============================");
        System.out.println("Digite a operação: ");
        System.out.println("1 - Inserir");
        System.out.println("2 - Atualizar");
        System.out.println("3 - Excluir");
        System.out.println("4 - Buscar");
        System.out.println("Opção:");

        opcao = scanner.nextInt();

        switch (opcao) {
            case 1:
                System.out.println("Digite a chave:");
                chave = scanner.nextInt();

                System.out.println("Digite a Mensagem:");
                msg = mensagem.readLine();

                mapa = new Mapa();

                mapa.setChave(chave);
                mapa.setTipoOperacaoId(1);
                mapa.setTexto(msg);

                byte[] object = SerializationUtils.serialize(mapa);

                if (object.length > 1400) {
                    System.out.println("Pacote maior que o suportado!");
                } else {
                    send(object);
                }

                break;
            case 2:
                System.out.println("Digite a chave da mensagem que deseja atualizar:");
                chave = scanner.nextInt();

                System.out.println("Digite a Mensagem:");
                msg = mensagem.readLine();

                mapa = new Mapa();
                mapa.setChave(chave);
                mapa.setTipoOperacaoId(2);
                mapa.setTexto(msg);

                byte[] objectUpdate = SerializationUtils.serialize(mapa);

                if (objectUpdate.length > 1400) {
                    System.out.println("Pacote maior que o suportado!");
                } else {
                    send(objectUpdate);
                }

                break;
            case 3:
                System.out.println("Digite a chave da mensagem que deseja excluir:");
                chave = scanner.nextInt();

                mapa = new Mapa();
                mapa.setChave(chave);
                mapa.setTipoOperacaoId(3);

                byte[] objectDelete = SerializationUtils.serialize(mapa);

                if (objectDelete.length > 1400) {
                    System.out.println("Pacote maior que o suportado!");
                } else {
                    send(objectDelete);
                }

                break;
            case 4:
                System.out.println("Digite a chave da mensagem que deseja buscar:");
                chave = scanner.nextInt();

                mapa = new Mapa();
                mapa.setChave(chave);
                mapa.setTipoOperacaoId(4);

                byte[] objectSearch = SerializationUtils.serialize(mapa);

                if (objectSearch.length > 1400) {
                    System.out.println("Pacote maior que o suportado!");
                } else {
                    send(objectSearch);
                }

                break;
            default:
                System.out.println("Opção Inválida");
                break;
        }
    }

    public static void objetoRetornado(MapaDTO mapa) {
        System.out.println("\n================================");
        System.out.println("Chave: " + mapa.getMapa().getChave());
        System.out.println("Texto: " + mapa.getMapa().getTexto());
    }

}
