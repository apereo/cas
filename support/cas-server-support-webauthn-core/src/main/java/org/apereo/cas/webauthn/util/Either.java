package org.apereo.cas.webauthn.util;

import java.util.Optional;
import java.util.function.Function;

/**
 * This is {@link Either}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class Either<L, R> {

    private final boolean isRight;
    private final L leftValue;
    private final R rightValue;

    protected Either(final R rightValue) {
        this.isRight = true;
        this.leftValue = null;
        this.rightValue = rightValue;
    }

    protected Either(final boolean dummy, final L leftValue) {
        this.isRight = false;
        this.leftValue = leftValue;
        this.rightValue = null;
    }

    public boolean isLeft() {
        return !isRight();
    }

    public boolean isRight() {
        return isRight;
    }

    public Optional<L> left() {
        if (isLeft()) {
            return Optional.ofNullable(leftValue);
        }
        throw new IllegalStateException("Cannot call left() on a right value.");

    }

    public static <L, R> Either<L, R> left(final L value) {
        return new Either<>(false, value);
    }

    public Optional<R> right() {
        if (isRight()) {
            return Optional.ofNullable(rightValue);
        }
        throw new IllegalStateException("Cannot call right() on a left value.");
    }

    public static <L, R> Either<L, R> right(final R value) {
        return new Either<>(value);
    }

    public <RO> Either<L, RO> map(final Function<R, RO> func) {
        return flatMap(r -> Either.right(func.apply(r)));
    }

    public <RO> Either<L, RO> flatMap(final Function<R, Either<L, RO>> func) {
        if (isRight()) {
            return func.apply(rightValue);
        }
        return Either.left(leftValue);
    }
}

