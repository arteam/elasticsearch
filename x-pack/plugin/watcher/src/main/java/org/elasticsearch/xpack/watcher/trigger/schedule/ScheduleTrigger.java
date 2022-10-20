/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */
package org.elasticsearch.xpack.watcher.trigger.schedule;

import org.elasticsearch.xcontent.XContentBuilder;
import org.elasticsearch.xpack.core.watcher.trigger.Trigger;

import java.io.IOException;

public record ScheduleTrigger(Schedule schedule) implements Trigger {

    public static final String TYPE = "schedule";

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        return builder.startObject().field(schedule.type(), schedule, params).endObject();
    }

    public static Builder builder(Schedule schedule) {
        return new Builder(schedule);
    }

    public static class Builder implements Trigger.Builder<ScheduleTrigger> {

        private final Schedule schedule;

        private Builder(Schedule schedule) {
            this.schedule = schedule;
        }

        @Override
        public ScheduleTrigger build() {
            return new ScheduleTrigger(schedule);
        }
    }
}
