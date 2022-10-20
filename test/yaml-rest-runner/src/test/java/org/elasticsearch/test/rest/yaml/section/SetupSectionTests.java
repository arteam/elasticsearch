/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the Elastic License 2.0 or the Server
 * Side Public License, v 1.
 */
package org.elasticsearch.test.rest.yaml.section;

import org.elasticsearch.Version;
import org.elasticsearch.xcontent.yaml.YamlXContent;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;

public class SetupSectionTests extends AbstractClientYamlTestFragmentParserTestCase {
    public void testParseSetupSection() throws Exception {
        parser = createParser(YamlXContent.yamlXContent, """
              - do:
                  index1:
                    index:  test_1
                    type:   test
                    id:     1
                    body:   { "include": { "field1": "v1", "field2": "v2" }, "count": 1 }
              - do:
                  index2:
                    index:  test_1
                    type:   test
                    id:     2
                    body:   { "include": { "field1": "v1", "field2": "v2" }, "count": 1 }
            """);

        SetupSection setupSection = SetupSection.parse(parser);

        assertThat(setupSection, notNullValue());
        assertThat(setupSection.skipSection().isEmpty(), equalTo(true));
        assertThat(setupSection.executableSections().size(), equalTo(2));
        assertThat(setupSection.executableSections().get(0), instanceOf(DoSection.class));
        assertThat(((DoSection) setupSection.executableSections().get(0)).getApiCallSection().getApi(), equalTo("index1"));
        assertThat(setupSection.executableSections().get(1), instanceOf(DoSection.class));
        assertThat(((DoSection) setupSection.executableSections().get(1)).getApiCallSection().getApi(), equalTo("index2"));
    }

    public void testParseSetSectionInSetupSection() throws IOException {
        parser = createParser(YamlXContent.yamlXContent, """
            - do:
                cluster.state: {}
            - set: { master_node: master }
            - do:
                nodes.info:
                  metric: [ http, transport ]
            - set: {nodes.$master.http.publish_address: host}
            - set: {nodes.$master.transport.publish_address: transport_host}
            """);

        final SetupSection setupSection = SetupSection.parse(parser);

        assertNotNull(setupSection);
        assertTrue(setupSection.skipSection().isEmpty());
        assertThat(setupSection.executableSections().size(), equalTo(5));
        assertThat(setupSection.executableSections().get(0), instanceOf(DoSection.class));
        assertThat(((DoSection) setupSection.executableSections().get(0)).getApiCallSection().getApi(), equalTo("cluster.state"));
        assertThat(setupSection.executableSections().get(1), instanceOf(SetSection.class));
        final SetSection firstSetSection = (SetSection) setupSection.executableSections().get(1);
        assertThat(firstSetSection.getStash().entrySet(), hasSize(1));
        assertThat(firstSetSection.getStash(), hasKey("master_node"));
        assertThat(firstSetSection.getStash().get("master_node"), equalTo("master"));
        assertThat(setupSection.executableSections().get(2), instanceOf(DoSection.class));
        assertThat(((DoSection) setupSection.executableSections().get(2)).getApiCallSection().getApi(), equalTo("nodes.info"));
        assertThat(setupSection.executableSections().get(3), instanceOf(SetSection.class));
        final SetSection secondSetSection = (SetSection) setupSection.executableSections().get(3);
        assertThat(secondSetSection.getStash().entrySet(), hasSize(1));
        assertThat(secondSetSection.getStash(), hasKey("nodes.$master.http.publish_address"));
        assertThat(secondSetSection.getStash().get("nodes.$master.http.publish_address"), equalTo("host"));
        assertThat(setupSection.executableSections().get(4), instanceOf(SetSection.class));
        final SetSection thirdSetSection = (SetSection) setupSection.executableSections().get(4);
        assertThat(thirdSetSection.getStash().entrySet(), hasSize(1));
        assertThat(thirdSetSection.getStash(), hasKey("nodes.$master.transport.publish_address"));
        assertThat(thirdSetSection.getStash().get("nodes.$master.transport.publish_address"), equalTo("transport_host"));
    }

    public void testParseSetupAndSkipSectionNoSkip() throws Exception {
        parser = createParser(YamlXContent.yamlXContent, """
              - skip:
                  version:  "6.0.0 - 6.3.0"
                  reason:   "Update doesn't return metadata fields, waiting for #3259"
              - do:
                  index1:
                    index:  test_1
                    type:   test
                    id:     1
                    body:   { "include": { "field1": "v1", "field2": "v2" }, "count": 1 }
              - do:
                  index2:
                    index:  test_1
                    type:   test
                    id:     2
                    body:   { "include": { "field1": "v1", "field2": "v2" }, "count": 1 }
            """);

        SetupSection setupSection = SetupSection.parse(parser);

        assertThat(setupSection, notNullValue());
        assertThat(setupSection.skipSection().isEmpty(), equalTo(false));
        assertThat(setupSection.skipSection(), notNullValue());
        assertThat(setupSection.skipSection().getLowerVersion(), equalTo(Version.fromString("6.0.0")));
        assertThat(setupSection.skipSection().getUpperVersion(), equalTo(Version.fromString("6.3.0")));
        assertThat(setupSection.skipSection().getReason(), equalTo("Update doesn't return metadata fields, waiting for #3259"));
        assertThat(setupSection.executableSections().size(), equalTo(2));
        assertThat(setupSection.executableSections().get(0), instanceOf(DoSection.class));
        assertThat(((DoSection) setupSection.executableSections().get(0)).getApiCallSection().getApi(), equalTo("index1"));
        assertThat(setupSection.executableSections().get(1), instanceOf(DoSection.class));
        assertThat(((DoSection) setupSection.executableSections().get(1)).getApiCallSection().getApi(), equalTo("index2"));
    }
}
