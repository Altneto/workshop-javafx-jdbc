package model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import db.DB;
import db.DbException;
import model.dao.SellerDao;
import model.entities.Department;
import model.entities.Seller;

public class SellerDaoJDBC implements SellerDao {
	
	private Connection connection;
	
	public SellerDaoJDBC (Connection connection) {
		this.connection = connection;
	}

	@Override
	public void insert(Seller seller) {
		PreparedStatement statement = null;
		try {
			statement = connection.prepareStatement("INSERT INTO seller "
													+ "(Name, Email, BirthDate, BaseSalary, DepartmentId) "
													+ "VALUES "
													+ "(?, ?, ?, ?, ?)",
													Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, seller.getName());
			statement.setString(2, seller.getEmail());
			statement.setDate(3, new java.sql.Date(seller.getBirthDate().getTime()));
			statement.setDouble(4, seller.getBaseSalary());
			statement.setInt(5, seller.getDepartment().getId());
			
			int rowsAffected = statement.executeUpdate();
			
			if (rowsAffected > 0) {
				ResultSet resultSet = statement.getGeneratedKeys();
				if (resultSet.next()) {
					int id = resultSet.getInt(1);
					seller.setId(id);
				}
				DB.closeResultSet(resultSet);
			}else {
				throw new DbException("Erro inesperado! Nenhuma linha foi afetada");
			}
			
		}catch (SQLException e) {
			throw new DbException(e.getMessage());
		}finally {
			DB.closeStatement(statement);
		}
	}

	@Override
	public void update(Seller seller) {
		PreparedStatement statement = null;
		try {
			statement = connection.prepareStatement("UPDATE seller "
													+ "SET Name=?, Email=?, BirthDate=?, BaseSalary=?, DepartmentId=? "
													+ "WHERE id = ?");
			
			statement.setString(1, seller.getName());
			statement.setString(2, seller.getEmail());
			statement.setDate(3, new java.sql.Date(seller.getBirthDate().getTime()));
			statement.setDouble(4, seller.getBaseSalary());
			statement.setInt(5, seller.getDepartment().getId());
			statement.setInt(6, seller.getId());
			
			statement.executeUpdate();
			
		}catch (SQLException e) {
			throw new DbException(e.getMessage());
		}finally {
			DB.closeStatement(statement);
		}
	}

	@Override
	public void deleteById(Integer id) {
		PreparedStatement statement = null;
		try {
			statement = connection.prepareStatement("DELETE FROM seller WHERE Id = ?");
			statement.setInt(1, id);
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(statement);
		}
	}

	@Override
	public Seller findById(Integer id) {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connection.prepareStatement("SELECT seller.*,department.Name as DepName "
													+ "FROM seller INNER JOIN department "
													+ "ON seller.DepartmentId = department.Id "
													+ "WHERE seller.Id = ?");
			statement.setInt(1, id);
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				Department department = instantiateDepartment(resultSet);
				Seller obj = instantiateSeller(resultSet, department);
				return obj;
			}
			return null;
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(statement);
			DB.closeResultSet(resultSet);
		}
	}

	private Seller instantiateSeller(ResultSet resultSet, Department department) throws SQLException {
		Seller obj = new Seller();
		obj.setId(resultSet.getInt("Id"));
		obj.setName(resultSet.getString("Name"));
		obj.setEmail(resultSet.getString("Email"));
		obj.setBaseSalary(resultSet.getDouble("BaseSalary"));
		obj.setBirthDate(resultSet.getDate("BirthDate"));
		obj.setDepartment(department);
		return obj;
	}

	private Department instantiateDepartment(ResultSet resultSet) throws SQLException {
		Department department = new Department();
		department.setId(resultSet.getInt("DepartmentId"));
		department.setName(resultSet.getString("DepName"));
		return department;
	}

	@Override
	public List<Seller> findAll() {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connection.prepareStatement("SELECT seller.*,department.Name as DepName "
													+ "FROM seller INNER JOIN department "
													+ "ON seller.DepartmentId = department.Id "
													+ "ORDER BY Name");
			resultSet = statement.executeQuery();
			List<Seller> list = new ArrayList<>();
			Map<Integer, Department> map = new HashMap<>();
			
			while (resultSet.next()) {
				
				Department dep = map.get(resultSet.getInt("DepartmentId"));
				
				if (dep == null) {
					dep = instantiateDepartment(resultSet);
					map.put(resultSet.getInt("DepartmentId"), dep);
				}
				
				Seller obj = instantiateSeller(resultSet, dep);
				list.add(obj);
			}
			return list;
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(statement);
			DB.closeResultSet(resultSet);
		}
	}

	@Override
	public List<Seller> findByDepartment(Department department) {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connection.prepareStatement("SELECT seller.*,department.Name as DepName "
													+ "FROM seller INNER JOIN department "
													+ "ON seller.DepartmentId = department.Id "
													+ "WHERE DepartmentId = ? "
													+ "ORDER BY Name");
			statement.setInt(1, department.getId());
			resultSet = statement.executeQuery();
			List<Seller> list = new ArrayList<>();
			Map<Integer, Department> map = new HashMap<>();
			
			while (resultSet.next()) {
				
				Department dep = map.get(resultSet.getInt("DepartmentId"));
				
				if (dep == null) {
					dep = instantiateDepartment(resultSet);
					map.put(resultSet.getInt("DepartmentId"), dep);
				}
				
				Seller obj = instantiateSeller(resultSet, dep);
				list.add(obj);
			}
			return list;
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(statement);
			DB.closeResultSet(resultSet);
		}
	}
}
