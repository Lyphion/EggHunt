package dev.lyphium.egghunt.util;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public final class OneOf<A, B> {

    @Nullable
    private final A first;
    @Nullable
    private final B second;

    private OneOf(@Nullable A first, @Nullable B second) {
        this.first = first;
        this.second = second;
    }

    public static <A, B> OneOf<A, B> ofFirst(A first) {
        return new OneOf<>(first, null);
    }

    public static <A, B> OneOf<A, B> ofSecond(B second) {
        return new OneOf<>(null, second);
    }
}
