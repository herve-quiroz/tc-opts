/*
 * Copyright 2012 Herve Quiroz
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
package org.trancecode.opts;

/**
 * @author Herve Quiroz
 */
public final class Preconditions
{
    private Preconditions()
    {
        // No instantiation
    }

    public static <T> T checkNotNull(final T object)
    {
        if (object == null)
        {
            throw new NullPointerException();
        }
        return object;
    }

    public static void checkArgument(final boolean condition, final String message, final Object... args)
    {
        if (!condition)
        {
            throw new IllegalArgumentException(String.format(message, args));
        }
    }

    public static void checkState(final boolean condition, final String message, final Object... args)
    {
        if (!condition)
        {
            throw new IllegalStateException(String.format(message, args));
        }
    }
}
