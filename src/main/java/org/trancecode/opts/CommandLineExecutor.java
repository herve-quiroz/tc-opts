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

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * @author Herve Quiroz
 */
public final class CommandLineExecutor<T extends Runnable>
{
    private final String command;
    private final Class<T> executorClass;
    private final boolean helpSwitch;
    private final Options options;
    private final boolean quietSwitch;
    private final boolean verboseSwitch;
    private T executor;

    static
    {
        configureLogger();
    }

    public static <T extends Runnable> T execute(final Class<T> executorClass, final String... args)
    {
        final CommandLineExecutor<T> executor = new CommandLineExecutor<T>(executorClass);
        executor.execute(args);
        return executor.getExecutor();
    }

    private T getExecutor()
    {
        return executor;
    }

    private CommandLineExecutor(final Class<T> executorClass)
    {
        this.executorClass = Preconditions.checkNotNull(executorClass);

        final Command command = executorClass.getAnnotation(Command.class);
        Preconditions.checkArgument(command != null, "class %s missing the @%s annotation", executorClass,
                Command.class.getName());
        this.command = command.value();

        options = new Options();

        helpSwitch = command.helpSwitch();
        if (helpSwitch)
        {
            options.addOption(new Option("h", "help", false, "Print help"));
        }

        quietSwitch = command.quietSwitch();
        if (quietSwitch)
        {
            options.addOption(new Option("q", "quiet", false, "Display less information"));
        }

        verboseSwitch = command.verboseSwitch();
        if (verboseSwitch)
        {
            options.addOption(new Option("v", "verbose", false, "Display more information"));
        }

        for (final Method method : executorClass.getMethods())
        {
            final Flag flag = method.getAnnotation(Flag.class);
            if (flag != null)
            {
                Preconditions.checkArgument(method.getParameterTypes().length == 1, "%s", method);
                Preconditions.checkArgument(method.getParameterTypes()[0].equals(String.class), "%s", method);
                final Option option = new Option(getShortName(flag), getLongName(flag), true, flag.description());
                option.setRequired(flag.required());
                options.addOption(option);
            }

            final Switch theSwitch = method.getAnnotation(Switch.class);
            if (theSwitch != null)
            {
                Preconditions.checkArgument(method.getParameterTypes().length == 0, "%s", method);
                final Option option = new Option(getShortName(theSwitch), getLongName(theSwitch), false,
                        theSwitch.description());
                options.addOption(option);
            }
        }
    }

    private static void configureLogger()
    {
        Logger.getRootLogger().removeAllAppenders();
        Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("%m%n")));
        Logger.getRootLogger().setLevel(Level.INFO);
    }

    public void printSyntax(final PrintStream destination)
    {
        final HelpFormatter helpFormatter = new HelpFormatter();
        final PrintWriter printWriter = new PrintWriter(destination);
        try
        {
            helpFormatter.printHelp(printWriter, helpFormatter.getWidth(), command, null, options,
                    helpFormatter.getLeftPadding(), helpFormatter.getDescPadding(), null, true);
        }
        finally
        {
            printWriter.flush();
            printWriter.close();
        }
    }

    private static String getLongName(final Flag flag)
    {
        if (flag.longOption().equals(""))
        {
            return null;
        }

        return flag.longOption();
    }

    private static String getShortName(final Flag flag)
    {
        if (flag.shortOption().equals(""))
        {
            return Preconditions.checkNotNull(getLongName(flag));
        }

        return flag.shortOption();
    }

    private static String getLongName(final Switch theSwitch)
    {
        if (theSwitch.longOption().equals(""))
        {
            return null;
        }

        return theSwitch.longOption();
    }

    private static String getShortName(final Switch theSwitch)
    {
        if (theSwitch.shortOption().equals(""))
        {
            return Preconditions.checkNotNull(getLongName(theSwitch));
        }

        return theSwitch.shortOption();
    }

    public void execute(final String... args)
    {
        execute(System.in, System.out, System.err, args);
    }

    public void execute(final InputStream stdin, final PrintStream stdout, final PrintStream stderr,
            final String... args)
    {
        final GnuParser parser = new GnuParser();
        final CommandLine commandLine;
        try
        {
            commandLine = parser.parse(options, args);
        }
        catch (final ParseException e)
        {
            printSyntax(stderr);
            throw new CommandLineException(e, 100, e.getMessage());
        }

        if (helpSwitch && commandLine.hasOption("help"))
        {
            printSyntax(stderr);
            return;
        }

        final boolean quiet = quietSwitch && commandLine.hasOption("quiet");
        final boolean verbose = verboseSwitch && commandLine.hasOption("verbose");
        if (quiet && verbose)
        {
            throw new CommandLineException(101, "cannot set both 'quiet' and 'verbose' switches");
        }

        if (quiet)
        {
            Logger.getRootLogger().setLevel(Level.ERROR);
        }

        if (verbose)
        {
            Logger.getRootLogger().setLevel(Level.DEBUG);
        }

        try
        {
            executor = executorClass.newInstance();
        }
        catch (final Exception e)
        {
            throw Throwables.propagate(e);
        }

        for (final Method method : executorClass.getMethods())
        {
            final Flag flag = method.getAnnotation(Flag.class);
            if (flag != null)
            {
                if (commandLine.hasOption(getShortName(flag)))
                {
                    final String value = commandLine.getOptionValue(getShortName(flag));
                    try
                    {
                        method.invoke(executor, new Object[] { value });
                    }
                    catch (final Exception e)
                    {
                        throw Throwables.propagate(e);
                    }
                }
            }

            final Switch theSwitch = method.getAnnotation(Switch.class);
            if (theSwitch != null)
            {
                if (commandLine.hasOption(getShortName(theSwitch)))
                {
                    try
                    {
                        method.invoke(executor, new Object[0]);
                    }
                    catch (final Exception e)
                    {
                        throw Throwables.propagate(e);
                    }

                    if (theSwitch.exit())
                    {
                        return;
                    }
                }
            }
        }

        executor.run();
    }
}
