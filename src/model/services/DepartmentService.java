package model.services;

import java.util.List;

import model.dao.DaoFactory;
import model.dao.DepartmentDao;
import model.entites.Department;

public class DepartmentService {
	
	private DepartmentDao dao = DaoFactory.createDepartmentDao();

	public synchronized List<Department> findAdll(){
		return dao.findAll();
	}
	
	public synchronized void saveOrUpdate(Department department) {
		if(department.getId() == null) {
			dao.insert(department);
		} else {
			dao.update(department);
		}
	}
	
	public synchronized void delete(Department department) {
		dao.deleteById(department.getId());
	}
	
}
