package com.yubico.util;

import module java.base;

public final class Either<L, R> {

    private final boolean isRight;

    private final L leftValue;

    private final R rightValue;

    private Either(final R rightValue) {
        this.isRight = true;
        this.leftValue = null;
        this.rightValue = rightValue;
    }
    
    private Either(@SuppressWarnings("UnusedVariable") final boolean unused, final L leftValue) {
        this.isRight = false;
        this.leftValue = leftValue;
        this.rightValue = null;
    }

    public static <L, R> Either<L, R> left(final L value) {
        return new Either<>(false, value);
    }

    public static <L, R> Either<L, R> right(final R value) {
        return new Either<>(value);
    }

    public boolean isLeft() {
        return !isRight();
    }

    public boolean isRight() {
        return isRight;
    }

    public Optional<L> left() {
        if (isLeft()) {
            return Optional.of(leftValue);
        } else {
            throw new IllegalStateException("Cannot call left() on a right value.");
        }
    }

    public Optional<R> right() {
        if (isRight()) {
            return Optional.of(rightValue);
        } else {
            throw new IllegalStateException("Cannot call right() on a left value.");
        }
    }

    public <RO> Either<L, RO> map(final Function<R, RO> func) {
        return flatMap(r -> Either.right(func.apply(r)));
    }

    public <RO> Either<L, RO> flatMap(final Function<R, Either<L, RO>> func) {
        if (isRight()) {
            return func.apply(rightValue);
        } else {
            return Either.left(leftValue);
        }
    }

}
