package com.tvs.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tvs.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}
