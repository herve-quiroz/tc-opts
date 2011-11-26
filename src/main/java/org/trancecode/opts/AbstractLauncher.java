/*
 * Copyright 2011 TranceCode
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

import com.google.common.base.Preconditions;

/**
 * Convenient base class for common use cases (help, quiet and verbose
 * switches).
 * 
 * @author Herve Quiroz
 */
public abstract class AbstractLauncher
{
    private boolean quiet;
    private boolean verbose;

    @Option(shortName = "h", longName = "help", description = "Print help and exit", exit = true)
    public final void printSyntaxAndExit()
    {
        Options.printSyntax(getClass());
    }

    @Option(shortName = "q", longName = "quiet", description = "Display less information")
    public void setQuiet()
    {
        Preconditions.checkArgument(!verbose, "cannot set both 'quiet' and 'verbose' switches");
        quiet = true;
    }

    @Option(shortName = "v", longName = "verbose", description = "Display more information")
    public void setVerbose()
    {
        Preconditions.checkArgument(!quiet, "cannot set both 'quiet' and 'verbose' switches");
        verbose = true;
    }

    public final boolean isQuiet()
    {
        return quiet;
    }

    public final boolean isVerbose()
    {
        return verbose;
    }
}
