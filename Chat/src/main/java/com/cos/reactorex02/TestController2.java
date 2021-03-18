package com.cos.reactorex02;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitResult;

@RestController
public class TestController2 {

	// 프로세서 - 지속적 응답
	Sinks.Many<String> sink;
	
	// multicast() 새로 들어온 데이터만 응답받음 => hot시퀀스 (시퀀스=스트림)
	// replay()  기존 데이터 + 새로운 데이터 응답 cold시퀀스
	
	public TestController2() {
		this.sink = Sinks.many().multicast().onBackpressureBuffer();
	}

	@GetMapping("/")
	public Flux<Integer> findAll(){
		return Flux.just(1,2,3,4,5,6,7).log();
	}
	
	@CrossOrigin
	@PostMapping("/send")
	public void send(@RequestBody Chat chat) {	
		sink.tryEmitNext(chat.getUsername()+" : "+chat.getChat()); // event 안에 들어감
		System.out.println(chat.getUsername()+" : "+chat.getChat());
	}
	
	// data : 실제값 \n\n
	@GetMapping(value = "/sse")
	public Flux<ServerSentEvent<String>> sse() { // ServerSentEvent의 ContentType은 text event stream
		return sink.asFlux().map(e->ServerSentEvent.builder(e).build()).doOnCancel(()->{
			System.out.println("sse종료됨");
			sink.asFlux().blockLast();
		}); // 구독
	}
}
