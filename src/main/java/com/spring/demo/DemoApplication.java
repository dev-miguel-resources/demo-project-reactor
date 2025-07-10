package com.spring.demo;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DemoApplication.class);
	private static List<String> foods = new ArrayList<>();

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	public void createMono() {
		Mono<Integer> m1 = Mono.just(10);
		m1.subscribe(number -> log.info("Numero {}", number));
		Mono<String> m2 = Mono.just("Hello Jaime");
		m2.subscribe(greetings -> log.info("Greeting {}", greetings));
	}

	public void createFlux() {
		Flux<String> fx1 = Flux.fromIterable(foods);
		fx1.subscribe(x -> log.info("Food: " + x));

		// Convertir el flux a un Mono<List<T>> (ejercicio parcial)
		// fx1.collectList();
	}

	public void m1DoOnNext() {
		Flux<String> fx1 = Flux.fromIterable(foods);
		fx1.doOnNext(x -> log.info("Food: " + x)).subscribe();
	}

	public void m2Map() {
		Flux<String> fx1 = Flux.fromIterable(foods);
		// fx1.map(x -> x.toUpperCase()).subscribe(x -> log.info(x));
		fx1.map(x -> x.toUpperCase()).doOnNext(x -> log.info("Food: " + x)).subscribe();
	}

	@Override
	public void run(String... args) throws Exception {
		// createMono();
		foods.add("completo Italiano");
		foods.add("hamburguesa");
		foods.add("choripan");
		// createFlux();
		// m1DoOnNext();
		m2Map();
	}

}
