package org.evla.pmd.examples;

import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public class MonoZipIncorrectUsageTest {

    public Mono<Tuple2<String, String>> incorrect() {
        return Mono
                .just("test")
                .map(String::toUpperCase)
                .zipWith(Mono.just(getString()));
    }

    public Mono<Tuple2<String, String>> correct_1() {
        return Mono
                .just("test")
                .map(String::toUpperCase)
                .zipWith(Mono.defer(() -> Mono.just(getString())));
    }

    public Mono<Tuple2<String, String>> correct_2() {
        return Mono
                .just("test")
                .map(String::toUpperCase)
                .zipWith(Mono.just("null"));
    }

    public Mono<Tuple2<String, String>> correct_3() {
        return Mono
                .just("test")
                .map(String::toUpperCase)
                .flatMap(m -> Mono.zip(Mono.just(m) , Mono.just(getString())));
    }

    public Mono<Tuple2<String, String>> correct_4() {
        Mono<String> nMono = Mono.just("null");
        return Mono
                .just("test")
                .map(String::toUpperCase)
                .zipWith(nMono);
    }

    private String getString() {
        return "null";
    }
}
