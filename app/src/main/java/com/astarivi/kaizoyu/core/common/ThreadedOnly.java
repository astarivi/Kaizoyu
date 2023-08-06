package com.astarivi.kaizoyu.core.common;

import java.lang.annotation.*;


/**
 * Methods annotated cannot be run in the main thread, and must be run
 * in a background thread.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ThreadedOnly {
}