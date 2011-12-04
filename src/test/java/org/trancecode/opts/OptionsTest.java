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
    public void simpleTest()
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
        @Argument(label = "VALUE")
        public void setBooleanValue(final boolean booleanValue)
        {
            this.booleanValue = booleanValue;
        }

        @Option(longName = "int", description = "some int value")
        @Argument(label = "NUMBER")
        public void setIntValue(final int intValue)
        {
            this.intValue = intValue;
        }

        @Option(shortName = "s", longName = "string", description = "some String value")
        @Argument(label = "VALUE")
        public void setStringValue(final String stringValue)
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
    public void optionArgumentConversion()
    {
        final Launcher2 launcher = Options.execute(Launcher2.class,
                new String[] { "-b", "true", "--int", "123", "--string", "abc", "-d", "1.0" }).getKey();
        Assert.assertEquals(launcher.booleanValue, true);
        Assert.assertEquals(launcher.intValue, 123);
        Assert.assertEquals(launcher.stringValue, "abc");
        Assert.assertEquals(launcher.doubleValue, 1.0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void argumentConversionError()
    {
        Options.execute(Launcher2.class, new String[] { "--int", "abc" });
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
    public void exit()
    {
        final Launcher3 launcher = Options.execute(Launcher3.class, new String[] { "--exit" }).getKey();
        Assert.assertTrue(launcher.exit);
        Assert.assertFalse(launcher.run);
    }

    @Command("java -jar something.jar")
    public static final class Launcher4 implements Runnable
    {
        private String name;
        private int size;

        @Option(shortName = "o", description = "option")
        @Argument(label = "NAME=SIZE", pattern = "([a-zA-Z]+)=([0-9]+)")
        public void advancedArgument(final String name, final int size)
        {
            this.name = name;
            this.size = size;
        }

        @Override
        public void run()
        {
            // Nothing
        }
    }

    @Test
    public void optionWithMultipleArguments()
    {
        final Launcher4 launcher = Options.execute(Launcher4.class, new String[] { "-o", "abc=4" }).getKey();
        Assert.assertEquals(launcher.name, "abc");
        Assert.assertEquals(launcher.size, 4);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void optionWithMultipleArgumentsPatternError()
    {
        Options.execute(Launcher4.class, new String[] { "-o", "abc" }).getKey();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void optionWithMultipleArgumentsConversionError()
    {
        Options.execute(Launcher4.class, new String[] { "-o", "abc=def" }).getKey();
    }
}
