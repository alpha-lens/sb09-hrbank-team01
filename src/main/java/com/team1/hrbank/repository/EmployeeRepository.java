package com.team1.hrbank.repository;

import com.team1.hrbank.entity.Employee;

public interface EmployeeRepository {

  Employee Save(Employee employee);

  Employee findById(long id);
}
