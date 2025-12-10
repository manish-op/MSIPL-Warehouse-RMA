package com.serverManagement.server.management.service.admin.login;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.serverManagement.server.management.dao.admin.user.AdminUserDAO;
import com.serverManagement.server.management.entity.adminUser.AdminUserEntity;



@Service
public class AdminUserLogin implements UserDetailsService {

	private AdminUserDAO adminUserDAO;

	public AdminUserLogin(AdminUserDAO adminUserDAO) {
		super();
		this.adminUserDAO = adminUserDAO;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		AdminUserEntity adminUserEntity = adminUserDAO.findByEmail(username);
		List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();
		if (adminUserEntity != null) {
			list.add(new SimpleGrantedAuthority(adminUserEntity.getRoleModel().getRoleName()));
			return new User(adminUserEntity.getEmail(), adminUserEntity.getPassword(), list);
		} else {
			throw new UsernameNotFoundException("Invalid User Credential");
		}
	}

}
