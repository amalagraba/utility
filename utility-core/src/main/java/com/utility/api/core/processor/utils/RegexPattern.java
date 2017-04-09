package com.utility.api.core.processor.utils;

import java.util.regex.Pattern;

public class RegexPattern {

    public static final Pattern PRICE = Pattern.compile("\\d+\\,\\d{2}");
    public static final Pattern DIGITS = Pattern.compile("\\d+");
}
