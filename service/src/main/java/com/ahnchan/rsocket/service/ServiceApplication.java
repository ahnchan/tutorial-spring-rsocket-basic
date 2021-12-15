package com.ahnchan.rsocket.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;
import java.util.stream.Stream;

@SpringBootApplication
public class ServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceApplication.class, args);
	}

}

@Data
@AllArgsConstructor
@NoArgsConstructor
class GreetingRequest {
	private String name;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class GreetingResponse {
	private String message;
}

@Controller
class GreetingController {

	@MessageMapping("hello")
	Mono<String> hello() {
		return Mono.just("Hello World!");
	}

	@MessageMapping("hello.{name}")
	Mono<String> helloName(@DestinationVariable String name) {
		return Mono.just("Hello "+ name + "!");
	}

	/**
	 * Fire-and-forget : Mono<Void> functionName(arguments)
	 * Request-response(HTTP) : Mono<Object> functionName(Mono<Object>)
	 * Request-Stream : Flux<Object> functionName(Mon<Object>)
	 * channel : Flux<Object> functionName(Flux<Object>)
	 */


	@MessageMapping("greetings")
	Flux<GreetingResponse> greets(GreetingRequest request) {
		var stream = Stream.generate(new Supplier<GreetingResponse>() {
			@Override
			public GreetingResponse get() {
				return new GreetingResponse("Hi " + request.getName() +" @ "+ Instant.now());
			}
		});

		// Lamda expression
		// var stream = Stream.generate(() -> new GreetingResponse("Hi" + request.getName() +" @ "+ Instant.now()));

		return Flux.fromStream(stream)
				.delayElements(Duration.ofSeconds(1));
	}

}
