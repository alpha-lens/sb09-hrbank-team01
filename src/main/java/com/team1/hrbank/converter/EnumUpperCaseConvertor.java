package com.team1.hrbank.converter;

import com.team1.hrbank.entity.EmployeeTrendTimeUnit;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class EnumUpperCaseConvertor implements Converter<String, EmployeeTrendTimeUnit> {

  @Override
  public EmployeeTrendTimeUnit convert(String source) {
    try {
      return EmployeeTrendTimeUnit.valueOf(source.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("잘못된 입력값: " + e.getMessage());
    }
  }
}
