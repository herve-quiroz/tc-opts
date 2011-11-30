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

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for {@link Options}.
 * 
 * @author Herve Quiroz
 */
public final class OptionsTest
{
    @Command("java -jar something.jar")
    public static final class Launcher1 implements Runnable
    {
        private boolean run;

        @Override
        public void run()
        {
            run = true;
        }
    }

    @Test
    public void testLauncher1()
    {
        final Launcher1 launcher = Options.execute(Launcher1.class, new String[0]).getKey();
        Assert.assertTrue(launcher.run);
    }

    @Command("java -jar something.jar")
    public static final class Launcher2 implements Runnable
    {
        private boolean booleanValue;
        private int intValue;
        private String stringValue;
        private double doubleValue;

        @Option(shortName = "b", description = "some boolean value")
        public void setBooleanValue(@Name("VALUE") final boolean booleanValue)
        {
            this.booleanValue = booleanValue;
        }

        @Option(longName = "int", description = "some int value")
        public void setIntValue(@Name("VALUE") final int intValue)
        {
            this.intValue = intValue;
        }

        @Option(shortName = "s", longName = "string", description = "some String value")
        public void setStringValue(@Name("VALUE") final String stringValue)
        {
            this.stringValue = stringValue;
        }

        @Option(shortName = "d", description = "some String value")
        public void setDoubleValue(final Double value)
        {
            this.doubleValue = value;
        }

        @Override
        public void run()
        {
            // nothing
        }
    }

    @Test
    public void testLauncher2()
    {
        final Launcher2 launcher = Options.execute(Launcher2.class,
                new String[] { "-b", "true", "--int", "123", "--string", "abc", "-d", "1.0" }).getKey();
        Assert.assertEquals(launcher.booleanValue, true);
        Assert.assertEquals(launcher.intValue, 123);
        Assert.assertEquals(launcher.stringValue, "abc");
        Assert.assertEquals(launcher.doubleValue, 1.0);
    }

    @Test
    public void printSyntax()
    {
        Options.printSyntax(Launcher2.class);
    }

    @Command("java -jar something.jar")
    public static final class Launcher3 implements Runnable
    {
        private boolean run;
        private boolean exit;

        @Option(longName = "exit", description = "exit", exit = true)
        public void exit()
        {
            exit = true;
        }

        @Override
        public void run()
        {
            run = true;
        }
    }

    @Test
    public void testLauncher3()
    {
        final Launcher3 launcher = Options.execute(Launcher3.class, new String[] { "--exit" }).getKey();
        Assert.assertTrue(launcher.exit);
        Assert.assertFalse(launcher.run);
    }
}