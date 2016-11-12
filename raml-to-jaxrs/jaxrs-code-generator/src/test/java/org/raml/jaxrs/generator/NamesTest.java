package org.raml.jaxrs.generator;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Created by Jean-Philippe Belanger on 10/29/16.
 * Just potential zeroes and ones
 */
@org.junit.Ignore
public class NamesTest {
    @Test
    public void buildTypeName() throws Exception {

        assertEquals("Fun", Names.buildTypeName("/fun"));
        assertEquals("Fun", Names.buildTypeName("/fun"));
        assertEquals("CodeBytes", Names.buildTypeName("//code//bytes"));
        assertEquals("Root", Names.buildTypeName(""));
        assertEquals("FunAllo", Names.buildTypeName("fun_allo"));
        assertEquals("FunAllo", Names.buildTypeName("fun allo"));
    }

    @Test
    public void buildVariableName() throws Exception {

        assertEquals("fun", Names.buildVariableName("/fun"));
        assertEquals("fun", Names.buildVariableName("fun"));
        assertEquals("funAllo", Names.buildVariableName("fun allo"));
    }

    @Test
    public void buildMethodNameSuffix() throws Exception {

        assertEquals("ById", Names.parameterNameMethodSuffix(Arrays.asList("id")));
        assertEquals("ByIdAndColor", Names.parameterNameMethodSuffix(Arrays.asList("id", "color")));
        assertEquals("", Names.parameterNameMethodSuffix(Arrays.<String>asList()));
    }

}
