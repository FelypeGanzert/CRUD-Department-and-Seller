package model.dao;

import java.util.List;

import model.entites.Department;

public interface DepartmentDao {
	
	void insert(Department department);
	void update(Department department);
	void deleteById(Integer id);
	Department findById(Integer id);
	List<Department> findAll();

}
