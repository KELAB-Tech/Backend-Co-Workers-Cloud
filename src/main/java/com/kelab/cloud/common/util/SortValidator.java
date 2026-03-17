package com.kelab.cloud.common.util;

import java.util.List;

public class SortValidator {

    public static String validate(String sortBy, List<String> allowed, String fallback) {
        return allowed.contains(sortBy) ? sortBy : fallback;
    }
}