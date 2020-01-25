// Copyright (c) 2018, Yubico AB
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice, this
//    list of conditions and the following disclaimer.
//
// 2. Redistributions in binary form must reproduce the above copyright notice,
//    this list of conditions and the following disclaimer in the documentation
//    and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package com.yubico.util;


import java.util.Optional;
import java.util.function.Function;

public final class Either<L, R> {

    private final boolean isRight;
    private final L leftValue;
    private final R rightValue;

    private Either(R rightValue) {
        this.isRight = true;
        this.leftValue = null;
        this.rightValue = rightValue;
    }

    private Either(boolean dummy, L leftValue) {
        this.isRight = false;
        this.leftValue = leftValue;
        this.rightValue = null;
    }

    public final boolean isLeft() {
        return !isRight();
    }

    public final boolean isRight() {
        return isRight;
    }

    public final Optional<L> left() {
        if (isLeft()) {
            return Optional.of(leftValue);
        } else {
            throw new IllegalStateException("Cannot call left() on a right value.");
        }
    }

    public final Optional<R> right() {
        if (isRight()) {
            return Optional.of(rightValue);
        } else {
            throw new IllegalStateException("Cannot call right() on a left value.");
        }
    }

    public final <RO> Either<L, RO> map(Function<R, RO> func) {
       return flatMap(r -> Either.right(func.apply(r)));
    }

    public final <RO> Either<L, RO> flatMap(Function<R, Either<L, RO>> func) {
        if (isRight()) {
            return func.apply(rightValue);
        } else {
            return Either.left(leftValue);
        }
    }

    public static <L, R> Either<L, R> left(L value) {
        return new Either<>(false, value);
    }

    public static <L, R> Either<L, R> right(R value) {
        return new Either<>(value);
    }

}
