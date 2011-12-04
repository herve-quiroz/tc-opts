/*
 * Copyright 2010 Herve Quiroz
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

import org.trancecode.logging.Logger;

/**
 * @author Herve Quiroz
 */
public final class CommandLineException extends RuntimeException
{
    private static final long serialVersionUID = 3849840697709034812L;
    private static final Logger LOG = Logger.getLogger(CommandLineException.class);

    private final int exitCode;

    private static String formatMessage(final String message, final Object... args)
    {
        try
        {
            return String.format(message, args);
        }
        catch (final Exception e)
        {
            LOG.error("could not format '{}': {message}", message, e);
            LOG.debug("{stackTrace}", e);
            return message;
        }
    }

    public CommandLineException(final Throwable cause, final int exitCode, final String message, final Object... args)
    {
        super(formatMessage(message, args), cause);
        this.exitCode = exitCode;
    }

    public CommandLineException(final int exitCode, final String message, final Object... args)
    {
        super(formatMessage(message, args));
        this.exitCode = exitCode;
    }

    public int exitCode()
    {
        return exitCode;
    }
}
