/*
 * Copyright 2011 Herve Quiroz
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.trancecode.opts.converter;

import java.io.File;
import java.net.URI;
import java.net.URL;

import javax.xml.namespace.QName;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for {@link StringConverters}.
 * 
 * @author Herve Quiroz
 */
public final class StringConvertersTest
{
    @Test
    public void doubleValue()
    {
        Assert.assertEquals(StringConverters.convert("1.0", Double.TYPE), 1.0);
    }

    @Test
    public void stringValue()
    {
        Assert.assertEquals(StringConverters.convert("abc", String.class), "abc");
    }

    @Test
    public void nonExplicitType()
    {
        StringConverters.convert("1", Number.class);
    }

    @Test
    public void qnameValue()
    {
        Assert.assertEquals(StringConverters.convert("{abc}def", QName.class), new QName("abc", "def"));
    }

    @Test
    public void uriValue()
    {
        Assert.assertEquals(StringConverters.convert("abc://def/ghi", URI.class), URI.create("abc://def/ghi"));
    }

    @Test
    public void urlValue() throws Exception
    {
        Assert.assertEquals(StringConverters.convert("http://www.trancecode.org/", URL.class), new URL(
                "http://www.trancecode.org/"));
        Assert.assertEquals(StringConverters.convert("pom.xml", URL.class), new File("pom.xml").toURI().toURL());
    }
}
