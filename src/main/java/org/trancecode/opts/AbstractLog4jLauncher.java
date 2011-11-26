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

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * @author Herve Quiroz
 */
public abstract class AbstractLog4jLauncher extends AbstractLauncher
{
    static
    {
        Logger.getRootLogger().removeAllAppenders();
        Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("%m%n")));
        Logger.getRootLogger().setLevel(Level.INFO);
    }

    @Override
    public void setQuiet()
    {
        super.setQuiet();
        Logger.getRootLogger().setLevel(Level.ERROR);
    }

    @Override
    public void setVerbose()
    {
        super.setVerbose();
        Logger.getRootLogger().setLevel(Level.DEBUG);
    }
}
