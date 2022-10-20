/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */
package org.elasticsearch.xpack.watcher;

import java.time.Clock;

/**
 * A wrapper around {@link Clock} to provide a concrete type for Guice injection.
 * <p>
 * This class is temporary until {@link Clock} can be passed to action constructors
 * directly, or the actions can be rewritten to be unit tested with the clock overriden
 * just for unit tests instead of via Node construction.
 */
public record ClockHolder(Clock clock) {}
