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
import com.google.common.collect.ImmutableSet;

/**
 * @author Herve Quiroz
 */
public abstract class AbstractStringConverter implements StringConverter
{
    private final Iterable<Class<?>> sourceTypes;

    protected AbstractStringConverter(final Class<?>... sourceTypes)
    {
        Preconditions.checkNotNull(sourceTypes);
        Preconditions.checkArgument(sourceTypes.length > 0);
        this.sourceTypes = ImmutableSet.copyOf(sourceTypes);
    }

    @Override
    public Iterable<Class<?>> getSourceTypes()
    {
        return sourceTypes;
    }
}
