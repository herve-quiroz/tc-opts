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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;

/**
 * @author Herve Quiroz
 */
public final class StringConverters
{
    private static final Map<Class<?>, StringConverter> CONVERTERS;

    static
    {
        final Map<Class<?>, StringConverter> converters = new HashMap<Class<?>, StringConverter>();
        for (final StringConverter converter : ServiceLoader.load(StringConverter.class))
        {
            for (final Class<?> type : converter.getSourceTypes())
            {
                converters.put(type, converter);
            }
        }
        CONVERTERS = Collections.unmodifiableMap(converters);
    }

    private StringConverters()
    {
        // No instantiation
    }

    public static Object convert(final String string, final Class<?> type)
    {
        final StringConverter explicitConverter = CONVERTERS.get(type);
        if (explicitConverter != null)
        {
            return explicitConverter.convert(string, type);
        }

        for (final Entry<Class<?>, StringConverter> entry : CONVERTERS.entrySet())
        {
            if (type.isAssignableFrom(entry.getKey()))
            {
                return entry.getValue().convert(string, entry.getKey());
            }
        }

        throw new IllegalArgumentException("unsupported type: " + type.getName());
    }
}
