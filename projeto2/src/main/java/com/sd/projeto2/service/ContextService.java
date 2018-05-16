package com.sd.projeto2.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.sd.projeto2.main.Operacoes;
import com.sd.projeto1.proto.ContextRequest;
import com.sd.projeto1.proto.ContextResponse;
import com.sd.projeto1.proto.ContextServiceGrpc;
import com.sd.projeto1.proto.SubscribeRequest;
import com.sd.projeto1.proto.SubscribeResponse;

import io.grpc.stub.StreamObserver;
import java.util.Arrays;

public class ContextService extends ContextServiceGrpc.ContextServiceImplBase {

	private Queue<String> logQueue;
	private Queue<String> executeQueue;
	private Operacoes context;	
	private Map<String, List<StreamObserver< SubscribeResponse>>> observers;

	public ContextService(Queue<String> logQueue, Queue<String> executeQueue, Operacoes context, Map<String, List< StreamObserver< SubscribeResponse > > > observers ) {
		super();
		this.logQueue = logQueue;
		this.executeQueue = executeQueue;
		this.context = context;
		this.observers = observers;
	}

	@Override
	public void insert( ContextRequest request, StreamObserver< ContextResponse > responseObserver ) {
		messageToQueue( request.getInstruction() );
		ContextResponse response = ContextResponse.newBuilder().setMessage( "Instrucao executada: " + request.getInstruction() ).build();
		responseObserver.onNext( response );
		responseObserver.onCompleted();
	}

	@Override
	public void delete( ContextRequest request, StreamObserver< ContextResponse > responseObserver ) {
		messageToQueue( request.getInstruction() );
		ContextResponse response = ContextResponse.newBuilder().setMessage( "Instrucao executada: " + request.getInstruction() ).build();
		responseObserver.onNext( response );
		responseObserver.onCompleted();
	}

	@Override
	public void update( ContextRequest request, StreamObserver< ContextResponse > responseObserver ) {
		messageToQueue( request.getInstruction() );
		ContextResponse response = ContextResponse.newBuilder().setMessage( "Instrucao executada: " + request.getInstruction() ).build();
		responseObserver.onNext( response );
		responseObserver.onCompleted();
	}

	@Override
	public void find( ContextRequest request, StreamObserver< ContextResponse > responseObserver ) {
                messageToQueue( request.getInstruction() );
		String stringify = context.buscarTodos();
		ContextResponse response = ContextResponse.newBuilder().setMessage(stringify).build();
		responseObserver.onNext( response );
		responseObserver.onCompleted();
	}
	
	@Override
	public void subscribe( SubscribeRequest request, StreamObserver< SubscribeResponse > responseObserver ) {
		List< StreamObserver< SubscribeResponse > > registry = observers.get( request.getKey() );
		if( registry != null ) {
			registry.add( responseObserver );
			observers.put( request.getKey(), registry );
		} else {
			List< StreamObserver< SubscribeResponse > > list = new ArrayList< StreamObserver< SubscribeResponse > >();
			list.add( responseObserver );
			observers.put( request.getKey(), list );
		}
		SubscribeResponse response = SubscribeResponse.newBuilder().setMessage( "Monitoramento realizado com sucesso" ).build();
		responseObserver.onNext( response );
	}

	private void messageToQueue( String message ) {

		List< String > params = Arrays.asList( message.split( ";" ) );

		if (params.get(0).toUpperCase().equals("PROCURAR") || params.get(0).toUpperCase().equals("EXCLUIR")) {
			executeQueue.add( params.get(0).toUpperCase() + ";" + params.get(1) );
		} else {		
			executeQueue.add( params.get(0).toUpperCase() + ";" + params.get(1) + ";" + params.get(2));
		}

	}

}
