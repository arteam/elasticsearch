/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the Elastic License 2.0 or the Server
 * Side Public License, v 1.
 */

package org.elasticsearch.action.admin.cluster.settings;

import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.xcontent.ConstructingObjectParser;
import org.elasticsearch.xcontent.ParseField;
import org.elasticsearch.xcontent.ToXContentObject;
import org.elasticsearch.xcontent.XContentBuilder;
import org.elasticsearch.xcontent.XContentParser;

import java.io.IOException;
import java.util.Objects;

import static org.elasticsearch.xcontent.ConstructingObjectParser.constructorArg;
import static org.elasticsearch.xcontent.ConstructingObjectParser.optionalConstructorArg;

/**
 * This response is specific to the REST client. {@link org.elasticsearch.action.admin.cluster.state.ClusterStateResponse}
 * is used on the transport layer.
 */
public record RestClusterGetSettingsResponse(Settings persistentSettings, Settings transientSettings, Settings defaultSettings)
    implements
        ToXContentObject {

    static final String PERSISTENT_FIELD = "persistent";
    static final String TRANSIENT_FIELD = "transient";
    static final String DEFAULTS_FIELD = "defaults";

    private static final ConstructingObjectParser<RestClusterGetSettingsResponse, Void> PARSER = new ConstructingObjectParser<>(
        "cluster_get_settings_response",
        true,
        a -> {
            Settings defaultSettings = a[2] == null ? Settings.EMPTY : (Settings) a[2];
            return new RestClusterGetSettingsResponse((Settings) a[0], (Settings) a[1], defaultSettings);
        }
    );

    static {
        PARSER.declareObject(constructorArg(), (p, c) -> Settings.fromXContent(p), new ParseField(PERSISTENT_FIELD));
        PARSER.declareObject(constructorArg(), (p, c) -> Settings.fromXContent(p), new ParseField(TRANSIENT_FIELD));
        PARSER.declareObject(optionalConstructorArg(), (p, c) -> Settings.fromXContent(p), new ParseField(DEFAULTS_FIELD));
    }

    public RestClusterGetSettingsResponse(Settings persistentSettings, Settings transientSettings, Settings defaultSettings) {
        this.persistentSettings = Objects.requireNonNullElse(persistentSettings, Settings.EMPTY);
        this.transientSettings = Objects.requireNonNullElse(transientSettings, Settings.EMPTY);
        this.defaultSettings = Objects.requireNonNullElse(defaultSettings, Settings.EMPTY);
    }

    /**
     * Returns the string value of the setting for the specified index. The order of search is first
     * in persistent settings the transient settings and finally the default settings.
     *
     * @param setting the name of the setting to get
     * @return String
     */
    public String getSetting(String setting) {
        if (persistentSettings.hasValue(setting)) {
            return persistentSettings.get(setting);
        } else if (transientSettings.hasValue(setting)) {
            return transientSettings.get(setting);
        } else if (defaultSettings.hasValue(setting)) {
            return defaultSettings.get(setting);
        } else {
            return null;
        }
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();

        builder.startObject(PERSISTENT_FIELD);
        persistentSettings.toXContent(builder, params);
        builder.endObject();

        builder.startObject(TRANSIENT_FIELD);
        transientSettings.toXContent(builder, params);
        builder.endObject();

        if (defaultSettings.isEmpty() == false) {
            builder.startObject(DEFAULTS_FIELD);
            defaultSettings.toXContent(builder, params);
            builder.endObject();
        }
        builder.endObject();
        return builder;
    }

    public static RestClusterGetSettingsResponse fromXContent(XContentParser parser) {
        return PARSER.apply(parser, null);
    }

    @Override
    public String toString() {
        return Strings.toString(this);
    }
}
