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

import com.google.common.base.Preconditions;

/**
 * @author Herve Quiroz
 */
public final class PrimitiveStringConverter extends AbstractStringConverter
{
    public PrimitiveStringConverter()
    {
        super(Boolean.TYPE, Boolean.class, Byte.TYPE, Byte.class, Character.TYPE, Character.class, Double.TYPE,
                Double.class, Float.TYPE, Float.class, Integer.TYPE, Integer.class, Long.TYPE, Long.class, Short.TYPE,
                Short.class);
    }

    @Override
    public Object convert(final String string, final Class<?> type)
    {
        if (type.equals(Boolean.TYPE) || type.equals(Boolean.class))
        {
            return Boolean.parseBoolean(string);
        }

        if (type.equals(Byte.TYPE) || type.equals(Byte.class))
        {
            return Byte.parseByte(string);
        }

        if (type.equals(Character.TYPE) || type.equals(Character.class))
        {
            Preconditions.checkArgument(string.length() == 1, "string is too long: %s", string);
            return string.charAt(0);
        }

        if (type.equals(Double.TYPE) || type.equals(Double.class))
        {
            return Double.parseDouble(string);
        }

        if (type.equals(Float.TYPE) || type.equals(Float.class))
        {
            return Float.parseFloat(string);
        }

        if (type.equals(Integer.TYPE) || type.equals(Integer.class))
        {
            return Integer.parseInt(string);
        }

        if (type.equals(Long.TYPE) || type.equals(Long.class))
        {
            return Long.parseLong(string);
        }

        if (type.equals(Short.TYPE) || type.equals(Short.class))
        {
            return Short.parseShort(string);
        }

        throw new IllegalArgumentException("unsupported type: " + type.getName());
    }
}
