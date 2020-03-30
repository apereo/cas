package com.yubico.util;


import java.util.Optional;

/**
 * This is {@link Either}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public class Either<L, R> {

    private boolean isRight;
    private L leftValue;
    private R rightValue;

    public static <L, R> Either<L, R> left(final L value) {
        return new Either<L, R>().setLeft(value);
    }

    public Optional<L> left() {
        if (isLeft()) {
            return Optional.of(leftValue);
        } else {
            throw new IllegalStateException("Cannot call left() on a right value.");
        }
    }

    public static <L, R> Either<L, R> right(final R value) {
        return new Either<L, R>().setRight(value);
    }

    public Optional<R> right() {
        if (isRight()) {
            return Optional.of(rightValue);
        }
        throw new IllegalStateException("Cannot call right() on a left value.");
    }

    public boolean isLeft() {
        return !isRight();
    }

    public Either<L, R> setLeft(final L leftValue) {
        this.isRight = false;
        this.leftValue = leftValue;
        this.rightValue = null;
        return this;
    }

    public boolean isRight() {
        return isRight;
    }

    public Either<L, R> setRight(final R rightValue) {
        this.isRight = true;
        this.leftValue = null;
        this.rightValue = rightValue;
        return this;
    }
}
