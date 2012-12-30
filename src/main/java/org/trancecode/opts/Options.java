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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.trancecode.opts.converter.StringConverters;

/**
 * @author Herve Quiroz
 */
public final class Options
{
    private Options()
    {
        // No instantiation
    }

    private static String getOptionDisplayName(final Option option)
    {
        if (!option.longName().isEmpty())
        {
            return "--" + option.longName();
        }

        return "-" + option.shortName();
    }

    private static Option getOption(final Method method)
    {
        final Option explicitOption = method.getAnnotation(Option.class);
        if (explicitOption != null)
        {
            return explicitOption;
        }

        final Class<?> parentType = method.getDeclaringClass().getSuperclass();
        if (parentType != Object.class)
        {
            try
            {
                final Method parentClassMethod = parentType.getMethod(method.getName(), method.getParameterTypes());
                return getOption(parentClassMethod);
            }
            catch (final Exception e)
            {
                return null;
            }
        }

        return null;
    }

    private static Map<Option, Method> getOptions(final Class<?> type)
    {
        final Map<Option, Method> options = new HashMap<Option, Method>();
        for (final Method method : type.getMethods())
        {
            final Option option = getOption(method);
            if (option != null)
            {
                Preconditions.checkState(!option.description().isEmpty(), "@Option is missing a description: %s",
                        getOptionDisplayName(option));
                Preconditions.checkState(
                        method.getReturnType().equals(Void.TYPE) || method.getReturnType().equals(Integer.TYPE),
                        "an @Option method can only return 'void' or 'int': %s", method);
                Preconditions.checkState(
                        option.shortName().equals("") || findOptionWithShortName(options, option.shortName()) == null,
                        "duplicate option with short name -%s", option.shortName());
                Preconditions.checkState(
                        option.longName().equals("") || findOptionWithLongName(options, option.longName()) == null,
                        "duplicate option with long name --%s", option.longName());
                final Argument argument = method.getAnnotation(Argument.class);
                if (argument != null)
                {
                    try
                    {
                        Pattern.compile(argument.pattern());
                    }
                    catch (final PatternSyntaxException e)
                    {
                        throw new IllegalStateException(String.format("argument pattern is invalid for %s: %s", method,
                                argument.pattern()), e);
                    }
                }
                options.put(option, method);
            }
        }

        return options;
    }

    private static Option findOptionWithShortName(final Map<Option, Method> options, final String shortName)
    {
        for (final Option option : options.keySet())
        {
            if (option.shortName().equals(shortName))
            {
                return option;
            }
        }

        return null;
    }

    private static Option findOptionWithLongName(final Map<Option, Method> options, final String longName)
    {
        for (final Option option : options.keySet())
        {
            if (option.longName().equals(longName))
            {
                return option;
            }
        }

        return null;
    }

    /**
     * @return the launcher and the exit code.
     */
    public static <T extends Runnable> Entry<T, Integer> execute(final Class<T> launcherClass, final String... args)
    {
        Preconditions.checkNotNull(launcherClass);
        Preconditions.checkArgument(!launcherClass.isInterface(), "%s is an interface", launcherClass.getName());
        Preconditions.checkArgument(launcherClass.getAnnotation(Command.class) != null, "%s is missing %s",
                launcherClass, Command.class);
        Preconditions.checkNotNull(args);

        final Map<Option, Method> options = getOptions(launcherClass);
        final Map<Method, List<Object[]>> methodsToInvoke = new HashMap<Method, List<Object[]>>();

        for (int argIndex = 0; argIndex < args.length; argIndex++)
        {
            final String arg = args[argIndex];
            final Option option;
            if (arg.matches("--[a-zA-Z0-9].*"))
            {
                option = findOptionWithLongName(options, arg.substring(2));
            }
            else if (arg.matches("-[a-zA-Z0-9]"))
            {
                option = findOptionWithShortName(options, arg.substring(1));
            }
            else
            {
                throw new IllegalArgumentException(arg);
            }
            Preconditions.checkArgument(option != null, "unknown option: %s", arg);

            final Method method = options.get(option);
            Preconditions.checkArgument(option.multiple() || !methodsToInvoke.containsKey(method),
                    "duplicate option: %s", arg);
            final String optionArgument;
            if (method.getParameterTypes().length > 0)
            {
                Preconditions.checkArgument(argIndex < args.length - 1, "missing an argument for option %s", arg);
                optionArgument = args[++argIndex];
            }
            else
            {
                optionArgument = null;
            }
            final Object[] parameters = getParameters(method, optionArgument);
            if (!methodsToInvoke.containsKey(method))
            {
                methodsToInvoke.put(method, new ArrayList<Object[]>());
            }
            methodsToInvoke.get(method).add(parameters);
        }

        final T launcher;
        try
        {
            launcher = launcherClass.newInstance();
        }
        catch (final Exception e)
        {
            if (e instanceof RuntimeException)
            {
                throw (RuntimeException) e;
            }
            throw new IllegalStateException(e);
        }

        for (final Method method : launcherClass.getMethods())
        {
            final Option option = getOption(method);
            if (option != null)
            {
                if (option.required() && !methodsToInvoke.containsKey(method))
                {
                    throw new IllegalStateException("missing required option: " + option);
                }

                if (!methodsToInvoke.containsKey(method))
                {
                    continue;
                }

                for (final Object[] parameter : methodsToInvoke.get(method))
                {
                    final Object result;
                    try
                    {
                        result = method.invoke(launcher, parameter);
                    }
                    catch (final Exception e)
                    {
                        // TODO handle error code depending on the Exception
                        if (e instanceof RuntimeException)
                        {
                            throw (RuntimeException) e;
                        }
                        throw new IllegalStateException(e);
                    }

                    if (option.exit())
                    {
                        final int exitCode = getExitCode(result);
                        return new Map.Entry<T, Integer>()
                        {
                            @Override
                            public T getKey()
                            {
                                return launcher;
                            }

                            @Override
                            public Integer getValue()
                            {
                                return exitCode;
                            }

                            @Override
                            public Integer setValue(final Integer value)
                            {
                                throw new UnsupportedOperationException();
                            }
                        };
                    }
                }
            }
        }

        launcher.run();

        return new Map.Entry<T, Integer>()
        {
            @Override
            public T getKey()
            {
                return launcher;
            }

            @Override
            public Integer getValue()
            {
                return 0;
            }

            @Override
            public Integer setValue(final Integer value)
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    private static String getOptionArgumentPattern(final Method method)
    {
        final Argument argument = method.getAnnotation(Argument.class);
        if (argument != null)
        {
            return argument.pattern();
        }

        return "(.*)";
    }

    private static Object[] getParameters(final Method method, final String argument)
    {
        if (method.getParameterTypes().length == 0)
        {
            return new Object[0];
        }

        final String pattern = getOptionArgumentPattern(method);
        final Object[] parameters = new Object[method.getParameterTypes().length];
        for (int i = 0; i < parameters.length; i++)
        {
            final String argumentPart = argument.replaceAll(pattern, "$" + (i + 1));
            final Class<?> requiredType = method.getParameterTypes()[i];
            parameters[i] = StringConverters.convert(argumentPart, requiredType);
        }

        return parameters;
    }

    private static int getExitCode(final Object code)
    {
        if (code == null)
        {
            return 0;
        }

        if (code.getClass().equals(Integer.TYPE))
        {
            return Integer.TYPE.cast(code).intValue();
        }

        throw new UnsupportedOperationException(code.getClass().getName());
    }

    private static String getParameterName(final Method method)
    {
        Preconditions.checkNotNull(method);

        final Argument argument = method.getAnnotation(Argument.class);
        if (argument != null)
        {
            return argument.label();
        }

        return "VALUE";
    }

    private static CharSequence getSyntax(final Class<?> launcherClass)
    {
        Preconditions.checkNotNull(launcherClass);
        final StringBuilder syntax = new StringBuilder();
        final Command command = launcherClass.getAnnotation(Command.class);
        final Map<Option, Method> options = getOptions(launcherClass);
        Preconditions.checkArgument(command != null, "%s is missing %s", launcherClass, Command.class);
        syntax.append("usage: ").append(command.value());
        if (!options.isEmpty())
        {
            syntax.append(" [options]\n");

            for (final Entry<Option, Method> option : options.entrySet())
            {
                syntax.append("\n");
                final StringBuilder line = new StringBuilder();
                if (!option.getKey().shortName().isEmpty())
                {
                    line.append(" -").append(option.getKey().shortName());
                }
                while (line.length() < 3)
                {
                    line.append(" ");
                }
                if (!option.getKey().longName().isEmpty())
                {
                    line.append(" --").append(option.getKey().longName());
                }

                if (option.getValue().getParameterTypes().length > 0)
                {
                    final String parameterName = getParameterName(option.getValue());
                    line.append(" ").append(parameterName);
                    if (containsMultipleOptions(options.keySet()))
                    {
                        line.append(" [+]");
                    }
                }

                line.append(" ");
                while (line.length() < 30)
                {
                    line.append(" ");
                }

                line.append(option.getKey().description());

                syntax.append(line);
            }

            if (containsMultipleOptions(options.keySet()))
            {
                syntax.append("\n\n[+] marked option can be specified multiple times");
            }
        }

        return syntax;
    }

    private static boolean containsMultipleOptions(final Iterable<Option> options)
    {
        for (final Option option : options)
        {
            if (option.multiple())
            {
                return true;
            }
        }

        return false;
    }

    public static void printSyntax(final Class<?> launcherClass)
    {
        System.err.println(getSyntax(launcherClass));
    }
}
