/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */
package org.elasticsearch.xpack.monitoring;

import org.elasticsearch.xpack.monitoring.exporter.Exporters;

/**
 * A wrapper around the services needed to produce usage information for the monitoring feature.
 * <p>
 * This class is temporary until actions can be constructed directly by plugins.
 */
record MonitoringUsageServices(MonitoringService monitoringService, Exporters exporters) {}
