package com.spring.demo;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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

	public void m3FlatMap() {
		Mono.just("sergio").flatMap(x -> Mono.just(33)).subscribe(e -> log.info("Data: " + e));
		// Map: <A> -> <B> (transforma pero no traspasa el valor resultante a una nueva
		// estructura o flujo)
		// FlatMap: Te permite traspasar valores de A a nuevas estructuras que pueden
		// ser diferentes (no son las mismas, pueden ser Monos y Flux)
		// FlatMap: Mono1: Mono<String> = "sergio" -> Mono<T><Mono<T>> = Mono<T> = 33
	}

	public void m4Range() {
		// Emitir 10 valores consecutivos, empezando desde el 0
		Flux<Integer> fx1 = Flux.range(0, 9);
		// Aplicar una transformación a cada elemento:
		fx1.map(e -> e + 1).subscribe(e -> log.info("Data " + e));
	}

	public void m5DelayElements() throws InterruptedException {
		// Generar una secuencia de enteros del 0 al 19 (20 elementos en total)
		Flux.range(0, 20)
				// Introduce un retraso de 1 segundo entre cada emisión de valor
				.delayElements(Duration.ofSeconds(1))
				.doOnNext(x -> log.info("Element: " + x))
				.subscribe();
		// Detiene el hilo principal durante 10 seg.
		Thread.sleep(10000);
	}

	public void m6ZipWith() {
		List<String> clients = new ArrayList<>();
		clients.add("Jaime");
		clients.add("Richard");
		// clients.add("German");

		// Crear un flux que emitirá cada cliente de la lista uno por uno.
		Flux<String> fx1 = Flux.fromIterable(clients);
		// Crear otro flux que emitirá cada elemento de la lista de foods.
		Flux<String> fx2 = Flux.fromIterable(foods);

		// Combinar 2 fuentes de datos y emparejarlas en pares de valor
		// Resultado A: Jaime - italiano
		// Resultado B: Richard - hamburguesa
		// Resultado C: German - choripan
		// Por ej: Si cliente tiene 2 elementos y foods tiene 5, solo emitirá las
		// primeras 2 combinaciones.
		fx1.zipWith(fx2, (c, f) -> c + " - " + f).subscribe(log::info);
	}

	public void m7Merge() {
		List<String> clients = new ArrayList<>();
		clients.add("Jaime");
		clients.add("Richard");

		Flux<String> fx1 = Flux.fromIterable(clients);

		Flux<String> fx2 = Flux.fromIterable(foods);

		Mono<String> m1 = Mono.just("Spring");

		fx1.doOnNext(e -> {
			throw new ArithmeticException("BAD OPERATION");
		}).subscribe();

		// Merge: Combinar varios publishers (Flux o Mono) mezclando sus emisiones en un
		// solo flujo sin esperar que terminen uno por uno.
		// Por lo tanto, emitirá todos los elementos de estos flujos en el orden en que
		// vayan
		// llegando, no ordenados ni sincronizados entre ellos.
		// Resultado: fx1 (clientes) fx2 (foods) m1 (spring) m1 (spring) fx2 (foods)
		Flux.merge(fx1, fx2, m1, m1, fx2).subscribe(log::info);
	}

	public void m8Filter() {
		Flux<String> fx1 = Flux.fromIterable(foods);

		// Aplicamos un filtro que solo deja pasar los elementos que comiencen con "co"
		fx1.filter(e -> e.startsWith("co"))
				.subscribe(log::info);
	}

	public void m9TakeLast() {
		Flux<String> fx1 = Flux.fromIterable(foods);

		// Retiene solo los últimos 6 elementos del Flux, pero espera a que el flujo
		// termine
		// completamente antes de emitir algo.
		// Es decir, almacena temporalmente todos los elementos hasta el final, y luego
		// emite los últimos 6.
		// Consideración: no utilizar en flujos considerados infinitos. (Realtime:
		// chats)
		fx1.takeLast(6).subscribe(log::info);
	}

	public void m10Take() {
		Flux<String> fx1 = Flux.fromIterable(foods);
		// Toma los primeros 6 elementos que emite el Flux y descarta el resto.
		// Se detiene inmediatamente después de emitir el sexto elemento.
		fx1.take(1).subscribe(log::info);
	}

	public void m11DefaultIfEmpty() {
		foods = new ArrayList<>();
		Flux<String> fx1 = Flux.fromIterable(foods);
		fx1.map(e -> "Food: " + e)
				.defaultIfEmpty("EMPTY FLUX")
				.subscribe(log::info);
	}

	public void m12Error() {
		Flux<String> fx1 = Flux.fromIterable(foods);

		// onErrorMap(e -> new Exception(e.getMessage())).subscribe();
		fx1.doOnNext(e -> {
			throw new ArithmeticException("BAD OPERATION");
		}).onErrorReturn("ERROR TRY AGAIN")
				.subscribe(log::info);
	}

	public void m13Threads() {
		final Mono<String> mono = Mono.just("hello world");

		// nuevo thread: Thread-1
		Thread t = new Thread(() -> mono.map(msg -> msg + "thread : ")
				.subscribe(v -> System.out.println(v + Thread.currentThread().getName())));

		// main
		System.out.println(Thread.currentThread().getName());

		// inicializa el hilo t
		t.start();

	}

	public void m14PublishOn() {
		Flux.range(1, 2)
				// Resultados desde Main
				.map(x -> {
					log.info("Valor : " + x + " | Thread: " + Thread.currentThread().getName());
					return x;
				})
				// Resultados desde New-1
				.publishOn(Schedulers.newSingle("new"))
				.map(x -> {
					log.info("Valor : " + x + " | Thread: " + Thread.currentThread().getName());
					return x;
				})
				// Resultados desde Bounded-elastic-1
				.publishOn(Schedulers.boundedElastic())
				.map(x -> {
					log.info("Valor : " + x + " | Thread: " + Thread.currentThread().getName());
					return x;
				}).subscribe();
	}

	public void m16SubscribeOn() {
		Flux.range(1, 2)
				// Inmediate utilizaría el hilo Main
				.subscribeOn(Schedulers.immediate())
				.map(x -> {
					log.info("Valor : " + x + " | Thread: " + Thread.currentThread().getName());
					return x;
				})
				.map(x -> {
					log.info("Valor : " + x + " | Thread: " + Thread.currentThread().getName());
					return x;
				})
				.map(x -> {
					log.info("Valor : " + x + " | Thread: " + Thread.currentThread().getName());
					return x;
				}).subscribe();
	}

	public void m16PublishSubscribeOn() {
		Flux.range(1, 2)
				// New-1
				.map(x -> {
					log.info("Valor : " + x + " | Thread: " + Thread.currentThread().getName());
					return x;
				})
				// New-1
				.subscribeOn(Schedulers.newSingle("new"))
				.map(x -> {
					log.info("Valor : " + x + " | Thread: " + Thread.currentThread().getName());
					return x;
				})
				// New-1
				.map(x -> {
					log.info("Valor : " + x + " | Thread: " + Thread.currentThread().getName());
					return x;
				})
				.subscribeOn(Schedulers.immediate())
				.map(x -> {
					log.info("Valor : " + x + " | Thread: " + Thread.currentThread().getName());
					return x;
				})
				// Bounded
				.publishOn(Schedulers.boundedElastic())
				.map(x -> {
					log.info("Valor : " + x + " | Thread: " + Thread.currentThread().getName());
					return x;
				})
				// Bounded
				.map(x -> {
					log.info("Valor : " + x + " | Thread: " + Thread.currentThread().getName());
					return x;
				}).subscribe();

	}

	public void m17runOn() {
		Flux.range(1, 32)
				// Habilitar 16 "rails" -> canales de procesamiento (hilos)
				.parallel(16) // acá convertimos a parallel Flux
				.runOn(Schedulers.parallel()) // resolvemos el proceso en paralelo
				.map(x -> {
					log.info("Valor : " + x + " | Thread : " + Thread.currentThread().getName());
					return x;
				})
				.subscribe();
	}

	@Override
	public void run(String... args) throws Exception {
		// createMono();
		foods.add("completo Italiano");
		foods.add("hamburguesa");
		foods.add("choripan");
		// createFlux();
		// m1DoOnNext();
		// m2Map();
		// m3FlatMap();
		// m4Range();
		// m5DelayElements();
		// m6ZipWith();
		// m7Merge();
		// m8Filter();
		// m9TakeLast();
		// m10Take();
		// m11DefaultIfEmpty();
		// m12Error();
		// m13Threads();
		// m14PublishOn();
		// m15SubscribeOn();
		// m16PublishSubscribeOn();
		m17runOn();
	}

}
