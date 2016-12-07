package com.tenchael.dubbo.provider.service.impl;

import java.io.Serializable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.tenchael.dubbo.api.DemoService;
import com.tenchael.dubbo.bean.User;
import com.tenchael.dubbo.provider.dao.UserRepository;

@Service
public class DemoServiceImpl implements DemoService {

	@Autowired
	private UserRepository userDao;

	public String sayHello(String msg) {
		return "Hello " + msg;
	}

	@Override
	public	<T extends Serializable>  List<T> get() {
		return (List<T>) userDao.get();
	}

	@Override
	public <T extends Serializable> void set(T user) {
		if(user instanceof User){
			userDao.set((User) user);
		}

	}
}