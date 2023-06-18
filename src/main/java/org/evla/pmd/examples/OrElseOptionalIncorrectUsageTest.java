package org.evla.pmd.examples;

import java.util.Optional;

public class OrElseOptionalIncorrectUsageTest {

    public String incorrect() {
        return Optional
                .of("string")
                .orElse(getString());
    }

    private String getString() {
        return "empty";
    }

    public String correct_1() {
        return Optional
                .of("string")
                .orElse("empty");
    }

    public String correct_2() {
        return Optional
                .of("string")
                .orElseGet(() -> getString());
    }

    public String correct_3() {
        return Optional
                .of("string")
                .orElseGet(this::getString);
    }
}
