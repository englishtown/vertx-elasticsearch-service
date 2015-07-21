package com.englishtown.vertx.elasticsearch;

import io.vertx.core.json.JsonObject;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link PutIndexedScriptOptions}
 */
public class PutIndexedScriptOptionsTest {

    private static String sampleId;
    private static String sampleScriptLang;
    private static JsonObject sampleSource;

    private static PutIndexedScriptOptions pojo;
    private static JsonObject json;

    private static PutIndexedScriptOptions pojoCopy;
    private static JsonObject jsonCopy;

    @BeforeClass
    public static void before() {

        sampleId = "12345";
        sampleScriptLang = "groovy";
        sampleSource = new JsonObject().put("sample", "source");

        pojo = new PutIndexedScriptOptions();
        pojo.setId(sampleId);
        pojo.setScriptLang(sampleScriptLang);
        pojo.setSource(sampleSource);

        pojoCopy = new PutIndexedScriptOptions(pojo);

        json = pojo.toJson();
        jsonCopy = pojoCopy.toJson();
    }

    @Test
    public void testGetProperties() {
        assertEquals(json.getString(PutIndexedScriptOptions.JSON_FIELD_ID), sampleId);
        assertEquals(json.getString(PutIndexedScriptOptions.JSON_FIELD_SCRIPT_LANG), sampleScriptLang);
        assertEquals(json.getJsonObject(PutIndexedScriptOptions.JSON_FIELD_SOURCE).encode(), sampleSource.encode());

    }

    @Test
    public void testSetProperties() {
        assertEquals(pojo.getId(), json.getString(PutIndexedScriptOptions.JSON_FIELD_ID));
        assertEquals(pojo.getScriptLang(), json.getString(PutIndexedScriptOptions.JSON_FIELD_SCRIPT_LANG));
        assertEquals(pojo.getSource().encode(), json.getJsonObject(PutIndexedScriptOptions.JSON_FIELD_SOURCE).encode());
    }

    @Test
    public void testCopy() {
        assertEquals(json.encode(), jsonCopy.encode());
    }

}
