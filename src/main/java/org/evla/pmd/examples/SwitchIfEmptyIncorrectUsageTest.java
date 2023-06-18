package org.evla.pmd.examples;

import reactor.core.publisher.Mono;

public class SwitchIfEmptyIncorrectUsageTest {

    public void incorrect_1() {
        Mono.just("dear")
                .switchIfEmpty(Mono.just(getRandomString()));
    }

    public void incorrect_2() {
        Mono.just("dear")
                .switchIfEmpty(Mono.error(getException()));
    }

    public void correct_1() {
        Mono.just("dear")
                .switchIfEmpty(Mono.defer(() -> Mono.just(getRandomString())));
    }

    public void correct_2() {
        Mono.just("dear")
                .switchIfEmpty(Mono.error(() -> getException()));
    }

    public void correct_3() {
        Mono.just("dear")
                .switchIfEmpty(Mono.error(this::getException));
    }

    private String getRandomString() {
        return "str";
    }

    private Exception getException() {
        return new RuntimeException();
    }

}
