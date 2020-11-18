package com.borealfeast.reservation.dao;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Converter
public class StringSetConverter implements AttributeConverter<Set<Integer>, String> {
    private static final String SPLIT_CHAR = ";";

    @Override
    public String convertToDatabaseColumn(Set<Integer> valueList) {
        return String.join(SPLIT_CHAR, valueList.stream().map(String::valueOf).collect(Collectors.toList()));
    }

    @Override
    public Set<Integer> convertToEntityAttribute(String string) {
        return Arrays.asList(string.split(SPLIT_CHAR)).stream().map(Integer::valueOf).collect(Collectors.toSet());
    }
}
