package com.ahnchan.rsocket.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.retrosocket.EnableRSocketClients;
import org.springframework.retrosocket.RSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@EnableRSocketClients
@SpringBootApplication
public class ClientApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(ClientApplication.class, args);
		System.in.read();
	}

	@Bean
	RSocketRequester rSocketRequester(RSocketRequester.Builder builder) {
		return builder.tcp("localhost", 8181);
	}

	@Bean
	ApplicationRunner basic(RSocketRequester rSocketRequester) {
		return event -> {
			Mono<String> reply = rSocketRequester.route("hello").retrieveMono(String.class);
			reply.subscribe(System.out::println);
		};
	}

	@Bean
	ApplicationRunner basicName(RSocketRequester rSocketRequester) {
		return event -> {
			Mono<String> reply = rSocketRequester.route("hello.Gildong").retrieveMono(String.class);
			reply.subscribe(System.out::println);
		};
	}

	@Bean
	ApplicationRunner retroClient(GreetingClient gc) {
		return event -> {
				Mono<String> reply = gc.greet();
				reply.subscribe(System.out::println);
		};
	}

	@Bean
	ApplicationRunner retroClientName(GreetingClient gc) {
		return event -> {
			Mono<String> reply = gc.greetName("Spring Fans");
			reply.subscribe(System.out::println);
		};
	}


	@Bean
	ApplicationRunner greetings(RSocketRequester rSocketRequester) {
		return event -> {
			var reply = rSocketRequester
					.route("greetings")
					.data(new GreetingRequest("Gildong"))
					.retrieveFlux(GreetingResponse.class);
			reply.subscribe(System.out::println);
		};
	}

	@Bean
	ApplicationRunner retroGreetings(GreetingClient gc) {
		return event -> {
			var reply = gc.greetMany(new GreetingRequest("Gildong-retro"));
			reply.subscribe(System.out::println);
		};
	}
}

@RSocketClient
interface GreetingClient {
	@MessageMapping("hello")
	Mono<String> greet();

	@MessageMapping("hello.{name}")
	Mono<String> greetName(@DestinationVariable String name);

	@MessageMapping("greetings")
	Flux<GreetingResponse> greetMany(GreetingRequest request);
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