package model.services;

import java.util.List;

import model.dao.DaoFactory;
import model.dao.SellerDao;
import model.entites.Department;
import model.entites.Seller;

public class SellerService {

	private SellerDao dao = DaoFactory.createSellerDao();

	public List<Seller> findAdll(){
		return dao.findAll();
	}
	
	public Integer quantityByDepartment(Department department) {
		List<Seller> list = dao.findByDepartment(department);
		return list.size();
	}
	
}
