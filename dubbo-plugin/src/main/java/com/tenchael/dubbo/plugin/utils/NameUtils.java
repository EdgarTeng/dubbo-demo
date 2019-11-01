package com.tenchael.dubbo.plugin.utils;

import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;

public class NameUtils {

    public static String shortMethodName(Invoker<?> invoker, Invocation invocation) {
        return name(invoker.getInterface().getSimpleName(), invocation.getMethodName());
    }

    public static String fullMethodName(Invoker<?> invoker, Invocation invocation) {
        return name(invoker.getInterface().getName(), invocation.getMethodName());
    }

    public static String name(String prefix, char split, String... names) {
        final StringBuilder builder = new StringBuilder();
        append(builder, split, prefix);
        if (names != null) {
            for (String s : names) {
                append(builder, split, s);
            }
        }
        return builder.toString();
    }

    public static String name(String prefix, String... names) {
        return name(prefix, '#', names);
    }

    private static void append(StringBuilder builder, char split, String part) {
        if (part != null && !part.isEmpty()) {
            if (builder.length() > 0) {
                builder.append(split);
            }
            builder.append(part);
        }
    }

}