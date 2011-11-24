/*
 * Copyright 2010 TranceCode
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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for {@link CommandLineExecutor}.
 * 
 * @author Herve Quiroz
 */
public final class CommandLineExecutorTest
{
    private static boolean run;

    @Command("java -jar sample.jar")
    public static final class SampleExecutor implements Runnable
    {
        private String aaa;
        private String bbb;
        private String ccc;

        @Flag(shortOption = "a", longOption = "aaa", description = "the A")
        public void setAaa(final String aaa)
        {
            this.aaa = aaa;
        }

        @Flag(longOption = "bbb", description = "the B")
        public void setBbb(final String bbb)
        {
            this.bbb = bbb;
        }

        @Flag(shortOption = "c", description = "the C")
        public void setCcc(final String ccc)
        {
            this.ccc = ccc;
        }

        @Switch(shortOption = "s", longOption = "switch", description = "Some switch", exit = true)
        public void someSwitch()
        {
            System.out.println("some switch");
        }

        @Override
        public void run()
        {
            Assert.assertEquals(aaa, "AAA");
            Assert.assertEquals(bbb, "BBB");
            Assert.assertEquals(ccc, "CCC");
            run = true;
        }
    }

    @BeforeMethod
    public void init()
    {
        run = false;
    }

    @Test
    public void testSimple()
    {
        CommandLineExecutor.execute(SampleExecutor.class, new String[] { "-a", "AAA", "--bbb", "BBB", "-c", "CCC" });
        assert run;
    }

    @Test
    public void testQuiet()
    {
        CommandLineExecutor.execute(SampleExecutor.class,
                new String[] { "-q", "-a", "AAA", "--bbb", "BBB", "-c", "CCC" });
        assert run;
    }

    @Test
    public void testHelp()
    {
        CommandLineExecutor.execute(SampleExecutor.class, new String[] { "-h" });
        assert !run;
    }

    @Test(expectedExceptions = CommandLineException.class)
    public void testUnsupportedFlag()
    {
        CommandLineExecutor.execute(SampleExecutor.class, new String[] { "-z" });
    }

    @Test
    public void testSwitch()
    {
        CommandLineExecutor.execute(SampleExecutor.class, new String[] { "-s" });
        assert !run;
    }
}
