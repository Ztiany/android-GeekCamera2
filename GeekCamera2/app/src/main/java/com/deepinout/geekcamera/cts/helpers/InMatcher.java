package com.deepinout.geekcamera.cts.helpers;


import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * A {@link Matcher} class for checking if value contained in a {@link Collection} or array.
 */
public class InMatcher<T> extends BaseMatcher<T> {

    protected Collection<T> mValues;

    public InMatcher(Collection<T> values) {
        Preconditions.checkNotNull("values", values);
        mValues = values;
    }

    public InMatcher(T... values) {
        Preconditions.checkNotNull(values);
        mValues = Arrays.asList(values);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean matches(Object o) {
        T obj = (T) o;
        for (T elem : mValues) {
            if (Objects.equals(o, elem)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("in(").appendValue(mValues).appendText(")");
    }

    @Factory
    public static <T> Matcher<T> in(T... operand) {
        return new InMatcher<T>(operand);
    }

    @Factory
    public static <T> Matcher<T> in(Collection<T> operand) {
        return new InMatcher<T>(operand);
    }
}

