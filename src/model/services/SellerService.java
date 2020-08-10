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
	
	public List<Seller> findByDepartment(Department department) {
		return dao.findByDepartment(department);
	}

	public void saveOrUpdate(Seller seller) {
		if(seller.getId() == null) {
			dao.insert(seller);
		} else {
			dao.update(seller);
		}
	}
	
	public void delete(Seller seller) {
		dao.deleteById(seller.getId());
	}
	
}
