/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the Elastic License 2.0 or the Server
 * Side Public License, v 1.
 */

package org.elasticsearch.test.rest.yaml.section;

import org.elasticsearch.Version;
import org.elasticsearch.client.NodeSelector;
import org.elasticsearch.common.ParsingException;
import org.elasticsearch.xcontent.XContentLocation;
import org.elasticsearch.xcontent.yaml.YamlXContent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class ClientYamlTestSuiteTests extends AbstractClientYamlTestFragmentParserTestCase {
    public void testParseTestSetupTeardownAndSections() throws Exception {
        final boolean includeSetup = randomBoolean();
        final boolean includeTeardown = randomBoolean();
        StringBuilder testSpecBuilder = new StringBuilder();
        if (includeSetup) {
            testSpecBuilder.append("""
                ---
                setup:
                  - do:
                        indices.create:
                          index: test_index

                """);
        }
        if (includeTeardown) {
            testSpecBuilder.append("""
                ---
                teardown:
                  - do:
                      indices.delete:
                        index: test_index

                """);
        }
        parser = createParser(YamlXContent.yamlXContent, testSpecBuilder + """
            ---
            "Get index mapping":
              - do:
                  indices.get_mapping:
                    index: test_index

              - match: {test_index.test_type.properties.text.type:     string}
              - match: {test_index.test_type.properties.text.analyzer: whitespace}

            ---
            "Get type mapping - pre 6.0":

              - skip:
                  version:     "6.0.0 - "
                  reason:      "for newer versions the index name is always returned"

              - do:
                  indices.get_mapping:
                    index: test_index
                    type: test_type

              - match: {test_type.properties.text.type:     string}
              - match: {test_type.properties.text.analyzer: whitespace}
            """);

        ClientYamlTestSuite restTestSuite = ClientYamlTestSuite.parse(getTestClass().getName(), getTestName(), parser);

        assertThat(restTestSuite, notNullValue());
        assertThat(restTestSuite.name(), equalTo(getTestName()));
        assertThat(restTestSuite.setupSection(), notNullValue());
        if (includeSetup) {
            assertThat(restTestSuite.setupSection().isEmpty(), equalTo(false));
            assertThat(restTestSuite.setupSection().skipSection().isEmpty(), equalTo(true));
            assertThat(restTestSuite.setupSection().executableSections().size(), equalTo(1));
            final ExecutableSection maybeDoSection = restTestSuite.setupSection().executableSections().get(0);
            assertThat(maybeDoSection, instanceOf(DoSection.class));
            final DoSection doSection = (DoSection) maybeDoSection;
            assertThat(doSection.getApiCallSection().getApi(), equalTo("indices.create"));
            assertThat(doSection.getApiCallSection().getParams().size(), equalTo(1));
            assertThat(doSection.getApiCallSection().getParams().get("index"), equalTo("test_index"));
        } else {
            assertThat(restTestSuite.setupSection().isEmpty(), equalTo(true));
        }

        assertThat(restTestSuite.teardownSection(), notNullValue());
        if (includeTeardown) {
            assertThat(restTestSuite.teardownSection().isEmpty(), equalTo(false));
            assertThat(restTestSuite.teardownSection().getSkipSection().isEmpty(), equalTo(true));
            assertThat(restTestSuite.teardownSection().getDoSections().size(), equalTo(1));
            assertThat(
                ((DoSection) restTestSuite.teardownSection().getDoSections().get(0)).getApiCallSection().getApi(),
                equalTo("indices.delete")
            );
            assertThat(
                ((DoSection) restTestSuite.teardownSection().getDoSections().get(0)).getApiCallSection().getParams().size(),
                equalTo(1)
            );
            assertThat(
                ((DoSection) restTestSuite.teardownSection().getDoSections().get(0)).getApiCallSection().getParams().get("index"),
                equalTo("test_index")
            );
        } else {
            assertThat(restTestSuite.teardownSection().isEmpty(), equalTo(true));
        }

        assertThat(restTestSuite.testSections().size(), equalTo(2));

        assertThat(restTestSuite.testSections().get(0).name(), equalTo("Get index mapping"));
        assertThat(restTestSuite.testSections().get(0).skipSection().isEmpty(), equalTo(true));
        assertThat(restTestSuite.testSections().get(0).executableSections().size(), equalTo(3));
        assertThat(restTestSuite.testSections().get(0).executableSections().get(0), instanceOf(DoSection.class));
        DoSection doSection = (DoSection) restTestSuite.testSections().get(0).executableSections().get(0);
        assertThat(doSection.getApiCallSection().getApi(), equalTo("indices.get_mapping"));
        assertThat(doSection.getApiCallSection().getParams().size(), equalTo(1));
        assertThat(doSection.getApiCallSection().getParams().get("index"), equalTo("test_index"));
        assertThat(restTestSuite.testSections().get(0).executableSections().get(1), instanceOf(MatchAssertion.class));
        MatchAssertion matchAssertion = (MatchAssertion) restTestSuite.testSections().get(0).executableSections().get(1);
        assertThat(matchAssertion.getField(), equalTo("test_index.test_type.properties.text.type"));
        assertThat(matchAssertion.getExpectedValue().toString(), equalTo("string"));
        assertThat(restTestSuite.testSections().get(0).executableSections().get(2), instanceOf(MatchAssertion.class));
        matchAssertion = (MatchAssertion) restTestSuite.testSections().get(0).executableSections().get(2);
        assertThat(matchAssertion.getField(), equalTo("test_index.test_type.properties.text.analyzer"));
        assertThat(matchAssertion.getExpectedValue().toString(), equalTo("whitespace"));

        assertThat(restTestSuite.testSections().get(1).name(), equalTo("Get type mapping - pre 6.0"));
        assertThat(restTestSuite.testSections().get(1).skipSection().isEmpty(), equalTo(false));
        assertThat(
            restTestSuite.testSections().get(1).skipSection().getReason(),
            equalTo("for newer versions the index name is always returned")
        );
        assertThat(restTestSuite.testSections().get(1).skipSection().getLowerVersion(), equalTo(Version.fromString("6.0.0")));
        assertThat(restTestSuite.testSections().get(1).skipSection().getUpperVersion(), equalTo(Version.CURRENT));
        assertThat(restTestSuite.testSections().get(1).executableSections().size(), equalTo(3));
        assertThat(restTestSuite.testSections().get(1).executableSections().get(0), instanceOf(DoSection.class));
        doSection = (DoSection) restTestSuite.testSections().get(1).executableSections().get(0);
        assertThat(doSection.getApiCallSection().getApi(), equalTo("indices.get_mapping"));
        assertThat(doSection.getApiCallSection().getParams().size(), equalTo(2));
        assertThat(doSection.getApiCallSection().getParams().get("index"), equalTo("test_index"));
        assertThat(doSection.getApiCallSection().getParams().get("type"), equalTo("test_type"));
        assertThat(restTestSuite.testSections().get(0).executableSections().get(1), instanceOf(MatchAssertion.class));
        matchAssertion = (MatchAssertion) restTestSuite.testSections().get(1).executableSections().get(1);
        assertThat(matchAssertion.getField(), equalTo("test_type.properties.text.type"));
        assertThat(matchAssertion.getExpectedValue().toString(), equalTo("string"));
        assertThat(restTestSuite.testSections().get(1).executableSections().get(2), instanceOf(MatchAssertion.class));
        matchAssertion = (MatchAssertion) restTestSuite.testSections().get(1).executableSections().get(2);
        assertThat(matchAssertion.getField(), equalTo("test_type.properties.text.analyzer"));
        assertThat(matchAssertion.getExpectedValue().toString(), equalTo("whitespace"));
    }

    public void testParseTestSingleTestSection() throws Exception {
        parser = createParser(YamlXContent.yamlXContent, """
            ---
            "Index with ID":

             - do:
                  index:
                      index:  test-weird-index-中文
                      type:   weird.type
                      id:     1
                      body:   { foo: bar }

             - is_true:   ok
             - match:   { _index:   test-weird-index-中文 }
             - match:   { _type:    weird.type }
             - match:   { _id:      "1"}
             - match:   { _version: 1}

             - do:
                  get:
                      index:  test-weird-index-中文
                      type:   weird.type
                      id:     1

             - match:   { _index:   test-weird-index-中文 }
             - match:   { _type:    weird.type }
             - match:   { _id:      "1"}
             - match:   { _version: 1}
             - match:   { _source: { foo: bar }}""");

        ClientYamlTestSuite restTestSuite = ClientYamlTestSuite.parse(getTestClass().getName(), getTestName(), parser);

        assertThat(restTestSuite, notNullValue());
        assertThat(restTestSuite.name(), equalTo(getTestName()));

        assertThat(restTestSuite.setupSection().isEmpty(), equalTo(true));

        assertThat(restTestSuite.testSections().size(), equalTo(1));

        assertThat(restTestSuite.testSections().get(0).name(), equalTo("Index with ID"));
        assertThat(restTestSuite.testSections().get(0).skipSection().isEmpty(), equalTo(true));
        assertThat(restTestSuite.testSections().get(0).executableSections().size(), equalTo(12));
        assertThat(restTestSuite.testSections().get(0).executableSections().get(0), instanceOf(DoSection.class));
        DoSection doSection = (DoSection) restTestSuite.testSections().get(0).executableSections().get(0);
        assertThat(doSection.getCatch(), nullValue());
        assertThat(doSection.getApiCallSection().getApi(), equalTo("index"));
        assertThat(doSection.getApiCallSection().getParams().size(), equalTo(3));
        assertThat(doSection.getApiCallSection().hasBody(), equalTo(true));
        assertThat(restTestSuite.testSections().get(0).executableSections().get(1), instanceOf(IsTrueAssertion.class));
        IsTrueAssertion trueAssertion = (IsTrueAssertion) restTestSuite.testSections().get(0).executableSections().get(1);
        assertThat(trueAssertion.getField(), equalTo("ok"));
        assertThat(restTestSuite.testSections().get(0).executableSections().get(2), instanceOf(MatchAssertion.class));
        MatchAssertion matchAssertion = (MatchAssertion) restTestSuite.testSections().get(0).executableSections().get(2);
        assertThat(matchAssertion.getField(), equalTo("_index"));
        assertThat(matchAssertion.getExpectedValue().toString(), equalTo("test-weird-index-中文"));
        assertThat(restTestSuite.testSections().get(0).executableSections().get(3), instanceOf(MatchAssertion.class));
        matchAssertion = (MatchAssertion) restTestSuite.testSections().get(0).executableSections().get(3);
        assertThat(matchAssertion.getField(), equalTo("_type"));
        assertThat(matchAssertion.getExpectedValue().toString(), equalTo("weird.type"));
        assertThat(restTestSuite.testSections().get(0).executableSections().get(4), instanceOf(MatchAssertion.class));
        matchAssertion = (MatchAssertion) restTestSuite.testSections().get(0).executableSections().get(4);
        assertThat(matchAssertion.getField(), equalTo("_id"));
        assertThat(matchAssertion.getExpectedValue().toString(), equalTo("1"));
        assertThat(restTestSuite.testSections().get(0).executableSections().get(5), instanceOf(MatchAssertion.class));
        matchAssertion = (MatchAssertion) restTestSuite.testSections().get(0).executableSections().get(5);
        assertThat(matchAssertion.getField(), equalTo("_version"));
        assertThat(matchAssertion.getExpectedValue().toString(), equalTo("1"));
        assertThat(restTestSuite.testSections().get(0).executableSections().get(6), instanceOf(DoSection.class));
        doSection = (DoSection) restTestSuite.testSections().get(0).executableSections().get(6);
        assertThat(doSection.getCatch(), nullValue());
        assertThat(doSection.getApiCallSection().getApi(), equalTo("get"));
        assertThat(doSection.getApiCallSection().getParams().size(), equalTo(3));
        assertThat(doSection.getApiCallSection().hasBody(), equalTo(false));
        assertThat(restTestSuite.testSections().get(0).executableSections().get(7), instanceOf(MatchAssertion.class));
        matchAssertion = (MatchAssertion) restTestSuite.testSections().get(0).executableSections().get(7);
        assertThat(matchAssertion.getField(), equalTo("_index"));
        assertThat(matchAssertion.getExpectedValue().toString(), equalTo("test-weird-index-中文"));
        assertThat(restTestSuite.testSections().get(0).executableSections().get(8), instanceOf(MatchAssertion.class));
        matchAssertion = (MatchAssertion) restTestSuite.testSections().get(0).executableSections().get(8);
        assertThat(matchAssertion.getField(), equalTo("_type"));
        assertThat(matchAssertion.getExpectedValue().toString(), equalTo("weird.type"));
        assertThat(restTestSuite.testSections().get(0).executableSections().get(9), instanceOf(MatchAssertion.class));
        matchAssertion = (MatchAssertion) restTestSuite.testSections().get(0).executableSections().get(9);
        assertThat(matchAssertion.getField(), equalTo("_id"));
        assertThat(matchAssertion.getExpectedValue().toString(), equalTo("1"));
        assertThat(restTestSuite.testSections().get(0).executableSections().get(10), instanceOf(MatchAssertion.class));
        matchAssertion = (MatchAssertion) restTestSuite.testSections().get(0).executableSections().get(10);
        assertThat(matchAssertion.getField(), equalTo("_version"));
        assertThat(matchAssertion.getExpectedValue().toString(), equalTo("1"));
        assertThat(restTestSuite.testSections().get(0).executableSections().get(11), instanceOf(MatchAssertion.class));
        matchAssertion = (MatchAssertion) restTestSuite.testSections().get(0).executableSections().get(11);
        assertThat(matchAssertion.getField(), equalTo("_source"));
        assertThat(matchAssertion.getExpectedValue(), instanceOf(Map.class));
        assertThat(((Map) matchAssertion.getExpectedValue()).get("foo").toString(), equalTo("bar"));
    }

    public void testParseTestMultipleTestSections() throws Exception {
        parser = createParser(YamlXContent.yamlXContent, """
            ---
            "Missing document (partial doc)":

              - do:
                  catch:      missing
                  update:
                      index:  test_1
                      type:   test
                      id:     1
                      body:   { doc: { foo: bar } }

              - do:
                  update:
                      index: test_1
                      type:  test
                      id:    1
                      body:  { doc: { foo: bar } }
                      ignore: 404

            ---
            "Missing document (script)":


              - do:
                  catch:      missing
                  update:
                      index:  test_1
                      type:   test
                      id:     1
                      body:
                        script: "ctx._source.foo = bar"
                        params: { bar: 'xxx' }

              - do:
                  update:
                      index:  test_1
                      type:   test
                      id:     1
                      ignore: 404
                      body:
                        script:       "ctx._source.foo = bar"
                        params:       { bar: 'xxx' }
            """);

        ClientYamlTestSuite restTestSuite = ClientYamlTestSuite.parse(getTestClass().getName(), getTestName(), parser);

        assertThat(restTestSuite, notNullValue());
        assertThat(restTestSuite.name(), equalTo(getTestName()));

        assertThat(restTestSuite.setupSection().isEmpty(), equalTo(true));

        assertThat(restTestSuite.testSections().size(), equalTo(2));

        assertThat(restTestSuite.testSections().get(0).name(), equalTo("Missing document (partial doc)"));
        assertThat(restTestSuite.testSections().get(0).skipSection().isEmpty(), equalTo(true));
        assertThat(restTestSuite.testSections().get(0).executableSections().size(), equalTo(2));

        assertThat(restTestSuite.testSections().get(0).executableSections().get(0), instanceOf(DoSection.class));
        DoSection doSection = (DoSection) restTestSuite.testSections().get(0).executableSections().get(0);
        assertThat(doSection.getCatch(), equalTo("missing"));
        assertThat(doSection.getApiCallSection().getApi(), equalTo("update"));
        assertThat(doSection.getApiCallSection().getParams().size(), equalTo(3));
        assertThat(doSection.getApiCallSection().hasBody(), equalTo(true));
        assertThat(restTestSuite.testSections().get(0).executableSections().get(1), instanceOf(DoSection.class));
        doSection = (DoSection) restTestSuite.testSections().get(0).executableSections().get(1);
        assertThat(doSection.getCatch(), nullValue());
        assertThat(doSection.getApiCallSection().getApi(), equalTo("update"));
        assertThat(doSection.getApiCallSection().getParams().size(), equalTo(4));
        assertThat(doSection.getApiCallSection().hasBody(), equalTo(true));

        assertThat(restTestSuite.testSections().get(1).name(), equalTo("Missing document (script)"));
        assertThat(restTestSuite.testSections().get(1).skipSection().isEmpty(), equalTo(true));
        assertThat(restTestSuite.testSections().get(1).executableSections().size(), equalTo(2));
        assertThat(restTestSuite.testSections().get(1).executableSections().get(0), instanceOf(DoSection.class));
        assertThat(restTestSuite.testSections().get(1).executableSections().get(1), instanceOf(DoSection.class));
        doSection = (DoSection) restTestSuite.testSections().get(1).executableSections().get(0);
        assertThat(doSection.getCatch(), equalTo("missing"));
        assertThat(doSection.getApiCallSection().getApi(), equalTo("update"));
        assertThat(doSection.getApiCallSection().getParams().size(), equalTo(3));
        assertThat(doSection.getApiCallSection().hasBody(), equalTo(true));
        assertThat(restTestSuite.testSections().get(0).executableSections().get(1), instanceOf(DoSection.class));
        doSection = (DoSection) restTestSuite.testSections().get(1).executableSections().get(1);
        assertThat(doSection.getCatch(), nullValue());
        assertThat(doSection.getApiCallSection().getApi(), equalTo("update"));
        assertThat(doSection.getApiCallSection().getParams().size(), equalTo(4));
        assertThat(doSection.getApiCallSection().hasBody(), equalTo(true));
    }

    public void testParseTestDuplicateTestSections() throws Exception {
        parser = createParser(YamlXContent.yamlXContent, """
            ---
            "Missing document (script)":

              - do:
                  catch:      missing
                  update:
                      index:  test_1
                      type:   test
                      id:     1
                      body:   { doc: { foo: bar } }

            ---
            "Missing document (script)":


              - do:
                  catch:      missing
                  update:
                      index:  test_1
                      type:   test
                      id:     1
                      body:
                        script: "ctx._source.foo = bar"
                        params: { bar: 'xxx' }

            """);

        Exception e = expectThrows(
            ParsingException.class,
            () -> ClientYamlTestSuite.parse(getTestClass().getName(), getTestName(), parser)
        );
        assertThat(e.getMessage(), containsString("duplicate test section"));
    }

    public void testParseSkipOs() throws Exception {
        parser = createParser(YamlXContent.yamlXContent, """
            "Broken on some os":

              - skip:
                  features:     skip_os
                  os:           ["windows95", "debian-5"]
                  reason:      "not supported"

              - do:
                  indices.get_mapping:
                    index: test_index
                    type: test_type

              - match: {test_type.properties.text.type:     string}
              - match: {test_type.properties.text.analyzer: whitespace}
            """);

        ClientYamlTestSuite restTestSuite = ClientYamlTestSuite.parse(getTestClass().getName(), getTestName(), parser);

        assertThat(restTestSuite, notNullValue());
        assertThat(restTestSuite.name(), equalTo(getTestName()));
        assertThat(restTestSuite.testSections().size(), equalTo(1));

        assertThat(restTestSuite.testSections().get(0).name(), equalTo("Broken on some os"));
        assertThat(restTestSuite.testSections().get(0).skipSection().isEmpty(), equalTo(false));
        assertThat(restTestSuite.testSections().get(0).skipSection().getReason(), equalTo("not supported"));
        assertThat(
            restTestSuite.testSections().get(0).skipSection().getOperatingSystems(),
            containsInAnyOrder("windows95", "debian-5")
        );
        assertThat(restTestSuite.testSections().get(0).skipSection().getFeatures(), containsInAnyOrder("skip_os"));
    }

    public void testAddingDoWithoutSkips() {
        int lineNumber = between(1, 10000);
        DoSection doSection = new DoSection(new XContentLocation(lineNumber, 0));
        doSection.setApiCallSection(new ApiCallSection("test"));
        ClientYamlTestSection section = new ClientYamlTestSection(
            new XContentLocation(0, 0),
            "test",
            SkipSection.EMPTY,
            Collections.singletonList(doSection)
        );
        ClientYamlTestSuite clientYamlTestSuite = new ClientYamlTestSuite(
            "api",
            "name",
            SetupSection.EMPTY,
            TeardownSection.EMPTY,
            Collections.singletonList(section)
        );
        clientYamlTestSuite.validate();
    }

    public void testAddingDoWithWarningWithoutSkipWarnings() {
        int lineNumber = between(1, 10000);
        DoSection doSection = new DoSection(new XContentLocation(lineNumber, 0));
        doSection.setExpectedWarningHeaders(singletonList("foo"));
        doSection.setApiCallSection(new ApiCallSection("test"));
        ClientYamlTestSuite testSuite = createTestSuite(SkipSection.EMPTY, doSection);
        Exception e = expectThrows(IllegalArgumentException.class, testSuite::validate);
        assertThat(e.getMessage(), containsString(formatted("""
            api/name:
            attempted to add a [do] with a [warnings] section without a corresponding ["skip": "features": "warnings"] \
            so runners that do not support the [warnings] section can skip the test at line [%d]\
            """, lineNumber)));
    }

    public void testAddingDoWithWarningRegexWithoutSkipWarnings() {
        int lineNumber = between(1, 10000);
        DoSection doSection = new DoSection(new XContentLocation(lineNumber, 0));
        doSection.setExpectedWarningHeadersRegex(singletonList(Pattern.compile("foo")));
        doSection.setApiCallSection(new ApiCallSection("test"));
        ClientYamlTestSuite testSuite = createTestSuite(SkipSection.EMPTY, doSection);
        Exception e = expectThrows(IllegalArgumentException.class, testSuite::validate);
        assertThat(e.getMessage(), containsString(formatted("""
            api/name:
            attempted to add a [do] with a [warnings_regex] section without a corresponding ["skip": "features": "warnings_regex"] \
            so runners that do not support the [warnings_regex] section can skip the test at line [%d]\
            """, lineNumber)));
    }

    public void testAddingDoWithAllowedWarningWithoutSkipAllowedWarnings() {
        int lineNumber = between(1, 10000);
        DoSection doSection = new DoSection(new XContentLocation(lineNumber, 0));
        doSection.setAllowedWarningHeaders(singletonList("foo"));
        doSection.setApiCallSection(new ApiCallSection("test"));
        ClientYamlTestSuite testSuite = createTestSuite(SkipSection.EMPTY, doSection);
        Exception e = expectThrows(IllegalArgumentException.class, testSuite::validate);
        assertThat(e.getMessage(), containsString(formatted("""
            api/name:
            attempted to add a [do] with a [allowed_warnings] section without a corresponding ["skip": "features": \
            "allowed_warnings"] so runners that do not support the [allowed_warnings] section can skip the test at \
            line [%d]\
            """, lineNumber)));
    }

    public void testAddingDoWithAllowedWarningRegexWithoutSkipAllowedWarnings() {
        int lineNumber = between(1, 10000);
        DoSection doSection = new DoSection(new XContentLocation(lineNumber, 0));
        doSection.setAllowedWarningHeadersRegex(singletonList(Pattern.compile("foo")));
        doSection.setApiCallSection(new ApiCallSection("test"));
        ClientYamlTestSuite testSuite = createTestSuite(SkipSection.EMPTY, doSection);
        Exception e = expectThrows(IllegalArgumentException.class, testSuite::validate);
        assertThat(e.getMessage(), containsString(formatted("""
            api/name:
            attempted to add a [do] with a [allowed_warnings_regex] section without a corresponding ["skip": "features": \
            "allowed_warnings_regex"] so runners that do not support the [allowed_warnings_regex] section can skip the test \
            at line [%d]\
            """, lineNumber)));
    }

    public void testAddingDoWithHeaderWithoutSkipHeaders() {
        int lineNumber = between(1, 10000);
        DoSection doSection = new DoSection(new XContentLocation(lineNumber, 0));
        ApiCallSection apiCallSection = new ApiCallSection("test");
        apiCallSection.addHeaders(Collections.singletonMap("header", "value"));
        doSection.setApiCallSection(apiCallSection);
        ClientYamlTestSuite testSuite = createTestSuite(SkipSection.EMPTY, doSection);
        Exception e = expectThrows(IllegalArgumentException.class, testSuite::validate);
        assertThat(e.getMessage(), containsString(formatted("""
            api/name:
            attempted to add a [do] with a [headers] section without a corresponding ["skip": "features": "headers"] \
            so runners that do not support the [headers] section can skip the test at line [%d]\
            """, lineNumber)));
    }

    public void testAddingDoWithNodeSelectorWithoutSkipNodeSelector() {
        int lineNumber = between(1, 10000);
        DoSection doSection = new DoSection(new XContentLocation(lineNumber, 0));
        ApiCallSection apiCall = new ApiCallSection("test");
        apiCall.setNodeSelector(NodeSelector.SKIP_DEDICATED_MASTERS);
        doSection.setApiCallSection(apiCall);
        ClientYamlTestSuite testSuite = createTestSuite(SkipSection.EMPTY, doSection);
        Exception e = expectThrows(IllegalArgumentException.class, testSuite::validate);
        assertThat(e.getMessage(), containsString(formatted("""
            api/name:
            attempted to add a [do] with a [node_selector] section without a corresponding ["skip": "features": "node_selector"] \
            so runners that do not support the [node_selector] section can skip the test at line [%d]\
            """, lineNumber)));
    }

    public void testAddingContainsWithoutSkipContains() {
        int lineNumber = between(1, 10000);
        ContainsAssertion containsAssertion = new ContainsAssertion(
            new XContentLocation(lineNumber, 0),
            randomAlphaOfLength(randomIntBetween(3, 30)),
            randomDouble()
        );
        ClientYamlTestSuite testSuite = createTestSuite(SkipSection.EMPTY, containsAssertion);
        Exception e = expectThrows(IllegalArgumentException.class, testSuite::validate);
        assertThat(e.getMessage(), containsString(formatted("""
            api/name:
            attempted to add a [contains] assertion without a corresponding ["skip": "features": "contains"] \
            so runners that do not support the [contains] assertion can skip the test at line [%d]\
            """, lineNumber)));
    }

    public void testMultipleValidationErrors() {
        int firstLineNumber = between(1, 10000);
        List<ClientYamlTestSection> sections = new ArrayList<>();
        {
            ContainsAssertion containsAssertion = new ContainsAssertion(
                new XContentLocation(firstLineNumber, 0),
                randomAlphaOfLength(randomIntBetween(3, 30)),
                randomDouble()
            );
            sections.add(
                new ClientYamlTestSection(
                    new XContentLocation(0, 0),
                    "section1",
                    SkipSection.EMPTY,
                    Collections.singletonList(containsAssertion)
                )
            );
        }
        int secondLineNumber = between(1, 10000);
        int thirdLineNumber = between(1, 10000);
        List<ExecutableSection> doSections = new ArrayList<>();
        {
            DoSection doSection = new DoSection(new XContentLocation(secondLineNumber, 0));
            doSection.setExpectedWarningHeaders(singletonList("foo"));
            doSection.setApiCallSection(new ApiCallSection("test"));
            doSections.add(doSection);
        }
        {
            DoSection doSection = new DoSection(new XContentLocation(thirdLineNumber, 0));
            ApiCallSection apiCall = new ApiCallSection("test");
            apiCall.setNodeSelector(NodeSelector.SKIP_DEDICATED_MASTERS);
            doSection.setApiCallSection(apiCall);
            doSections.add(doSection);
        }
        sections.add(new ClientYamlTestSection(new XContentLocation(0, 0), "section2", SkipSection.EMPTY, doSections));

        ClientYamlTestSuite testSuite = new ClientYamlTestSuite("api", "name", SetupSection.EMPTY, TeardownSection.EMPTY, sections);
        Exception e = expectThrows(IllegalArgumentException.class, testSuite::validate);
        assertEquals(String.format(Locale.ROOT, """
            api/name:
            attempted to add a [contains] assertion without a corresponding ["skip": "features": "contains"] so runners that \
            do not support the [contains] assertion can skip the test at line [%d],
            attempted to add a [do] with a [warnings] section without a corresponding ["skip": "features": "warnings"] so runners \
            that do not support the [warnings] section can skip the test at line [%d],
            attempted to add a [do] with a [node_selector] section without a corresponding ["skip": "features": "node_selector"] so \
            runners that do not support the [node_selector] section can skip the test \
            at line [%d]\
            """, firstLineNumber, secondLineNumber, thirdLineNumber), e.getMessage());
    }

    public void testAddingDoWithWarningWithSkip() {
        int lineNumber = between(1, 10000);
        DoSection doSection = new DoSection(new XContentLocation(lineNumber, 0));
        doSection.setExpectedWarningHeaders(singletonList("foo"));
        doSection.setApiCallSection(new ApiCallSection("test"));
        SkipSection skipSection = new SkipSection(null, singletonList("warnings"), emptyList(), null);
        createTestSuite(skipSection, doSection).validate();
    }

    public void testAddingDoWithWarningRegexWithSkip() {
        int lineNumber = between(1, 10000);
        DoSection doSection = new DoSection(new XContentLocation(lineNumber, 0));
        doSection.setExpectedWarningHeadersRegex(singletonList(Pattern.compile("foo")));
        doSection.setApiCallSection(new ApiCallSection("test"));
        SkipSection skipSection = new SkipSection(null, singletonList("warnings_regex"), emptyList(), null);
        createTestSuite(skipSection, doSection).validate();
    }

    public void testAddingDoWithNodeSelectorWithSkip() {
        int lineNumber = between(1, 10000);
        SkipSection skipSection = new SkipSection(null, singletonList("node_selector"), emptyList(), null);
        DoSection doSection = new DoSection(new XContentLocation(lineNumber, 0));
        ApiCallSection apiCall = new ApiCallSection("test");
        apiCall.setNodeSelector(NodeSelector.SKIP_DEDICATED_MASTERS);
        doSection.setApiCallSection(apiCall);
        createTestSuite(skipSection, doSection).validate();
    }

    public void testAddingDoWithHeadersWithSkip() {
        int lineNumber = between(1, 10000);
        SkipSection skipSection = new SkipSection(null, singletonList("headers"), emptyList(), null);
        DoSection doSection = new DoSection(new XContentLocation(lineNumber, 0));
        ApiCallSection apiCallSection = new ApiCallSection("test");
        apiCallSection.addHeaders(singletonMap("foo", "bar"));
        doSection.setApiCallSection(apiCallSection);
        createTestSuite(skipSection, doSection).validate();
    }

    public void testAddingContainsWithSkip() {
        int lineNumber = between(1, 10000);
        SkipSection skipSection = new SkipSection(null, singletonList("contains"), emptyList(), null);
        ContainsAssertion containsAssertion = new ContainsAssertion(
            new XContentLocation(lineNumber, 0),
            randomAlphaOfLength(randomIntBetween(3, 30)),
            randomDouble()
        );
        createTestSuite(skipSection, containsAssertion).validate();
    }

    public void testAddingCloseToWithSkip() {
        int lineNumber = between(1, 10000);
        SkipSection skipSection = new SkipSection(null, singletonList("close_to"), emptyList(), null);
        CloseToAssertion closeToAssertion = new CloseToAssertion(
            new XContentLocation(lineNumber, 0),
            randomAlphaOfLength(randomIntBetween(3, 30)),
            randomDouble(),
            randomDouble()
        );
        createTestSuite(skipSection, closeToAssertion).validate();
    }

    private static ClientYamlTestSuite createTestSuite(SkipSection skipSection, ExecutableSection executableSection) {
        final SetupSection setupSection;
        final TeardownSection teardownSection;
        final ClientYamlTestSection clientYamlTestSection;
        switch (randomIntBetween(0, 4)) {
            case 0 -> {
                setupSection = new SetupSection(skipSection, Collections.emptyList());
                teardownSection = TeardownSection.EMPTY;
                clientYamlTestSection = new ClientYamlTestSection(
                    new XContentLocation(0, 0),
                    "test",
                    SkipSection.EMPTY,
                    Collections.singletonList(executableSection)
                );
            }
            case 1 -> {
                setupSection = SetupSection.EMPTY;
                teardownSection = new TeardownSection(skipSection, Collections.emptyList());
                clientYamlTestSection = new ClientYamlTestSection(
                    new XContentLocation(0, 0),
                    "test",
                    SkipSection.EMPTY,
                    Collections.singletonList(executableSection)
                );
            }
            case 2 -> {
                setupSection = SetupSection.EMPTY;
                teardownSection = TeardownSection.EMPTY;
                clientYamlTestSection = new ClientYamlTestSection(
                    new XContentLocation(0, 0),
                    "test",
                    skipSection,
                    Collections.singletonList(executableSection)
                );
            }
            case 3 -> {
                setupSection = new SetupSection(skipSection, Collections.singletonList(executableSection));
                teardownSection = TeardownSection.EMPTY;
                clientYamlTestSection = new ClientYamlTestSection(
                    new XContentLocation(0, 0),
                    "test",
                    SkipSection.EMPTY,
                    randomBoolean() ? Collections.emptyList() : Collections.singletonList(executableSection)
                );
            }
            case 4 -> {
                setupSection = SetupSection.EMPTY;
                teardownSection = new TeardownSection(skipSection, Collections.singletonList(executableSection));
                clientYamlTestSection = new ClientYamlTestSection(
                    new XContentLocation(0, 0),
                    "test",
                    SkipSection.EMPTY,
                    randomBoolean() ? Collections.emptyList() : Collections.singletonList(executableSection)
                );
            }
            default -> throw new UnsupportedOperationException();
        }
        return new ClientYamlTestSuite("api", "name", setupSection, teardownSection, Collections.singletonList(clientYamlTestSection));
    }
}
