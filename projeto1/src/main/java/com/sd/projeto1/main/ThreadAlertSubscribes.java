package com.sd.projeto1.main;

import com.sd.projeto1.model.Mapa;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import com.sd.projeto1.proto.SubscribeResponse;
import com.sd.projeto1.util.Utilidades;
import io.grpc.stub.StreamObserver;

public class ThreadAlertSubscribes implements Runnable {

	private Queue< String > logQueue;
	private Queue< String > executeQueue;
	private Operacoes context;
	private DatagramSocket serverSocket;
	private Map< String, List< StreamObserver< SubscribeResponse > > > observers;
	
	public ThreadAlertSubscribes(
		DatagramSocket serverSocket,
		Queue< String > logQueue,
		Queue< String > executeQueue,
		Operacoes context,
		Map< String, List< StreamObserver< SubscribeResponse > > > observers ) {
		this.logQueue = logQueue;
		this.executeQueue = executeQueue;
		this.context = context;
		this.serverSocket = serverSocket;
		this.observers = observers;
	}

	@Override
	public void run() {

		while ( true ) {
			try {
				//Thread.sleep( 20000 );
				String instruction = executeQueue.poll();
				if ( instruction != null ) {
					System.out.println( "Executando instrucao: " + instruction );
					execute( instruction );
				}
				Thread.sleep( 1 );
			} catch ( Exception ex ) {
				ex.printStackTrace();
			}
		}

	}

	private void execute( String instruction ) {
		List< String > params = Arrays.asList( instruction.split( ";" ) );
                
                Mapa mapa = new Mapa();
                mapa.setChave(Integer.parseInt(params.get(1)));
                
                
		if ( Utilidades.retornaTipoOperacao(4).equals(instruction) ) {
			sendDatagram( params );
			return;
		} else if ( Utilidades.retornaTipoOperacao(1).equals(instruction) ) {
                        mapa.setTexto(params.get( 2 ));
			context.salvar(mapa);
			alertSubscribers( params.get( 1 ), "INSERT -> " + params.get( 2 ) );
		} else if ( Utilidades.retornaTipoOperacao(2).equals(instruction) ) {
                    if(!context.buscar(mapa).equals(null) || !context.buscar(mapa).equals("")){
			
                            mapa.setTexto(params.get( 2 ));
                            context.editar(mapa);
                            alertSubscribers( params.get( 1 ), "UPDATE -> " + params.get( 2 ) );
			}
		} else {
			context.excluir(mapa);
			alertSubscribers( params.get( 1 ), "DELETE -> " + params.get( 1 ) );
		}

		logQueue.add( instruction );

	}

	/**
	 * Retorna para o client o datagram
	 * @param params
	 */
	private void sendDatagram( List< String > params ) {
		try {
			System.out.println( "Enviando o contexto para o remetente" );
			byte[] sendData;
			if( params.size() < 4 ) {
				sendData = context.buscarTodos().getBytes();
				DatagramPacket sendPacket = new DatagramPacket( sendData, sendData.length,
					InetAddress.getByName( params.get( 1 ).replace( "/", "" ) ), Integer.parseInt( params.get( 2 ) ) );
				serverSocket.send( sendPacket );
			} else {
                            Mapa mapa = new Mapa();
                            mapa.setChave(Integer.parseInt(params.get(1)));
                            
				sendData = context.buscar(mapa).getBytes();
				DatagramPacket sendPacket = new DatagramPacket( sendData, sendData.length,
					InetAddress.getByName( params.get( 2 ).replace( "/", "" ) ), Integer.parseInt( params.get( 3 ) ) );
				serverSocket.send( sendPacket );
			}
		} catch ( Exception ex ) {
			ex.printStackTrace();
		}
	}
	
	private void alertSubscribers( String key, String changed ) {
	
		try {
			if( observers.get( key ) != null ) {
				List< StreamObserver< SubscribeResponse > > observerList = observers.get( key );
				for( StreamObserver< SubscribeResponse > observer : observerList ) {
					SubscribeResponse response = SubscribeResponse.newBuilder().setMessage( "Chave " + key + " alterada pela instrucao " + changed ).build();
					observer.onNext( response );
				}
			}
		} catch( Exception ex ) {
			// do nothing
		}
		
	}

}
