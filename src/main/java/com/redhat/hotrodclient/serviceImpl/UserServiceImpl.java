package com.redhat.hotrodclient.serviceImpl;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.redhat.hotrodclient.model.User;
import com.redhat.hotrodclient.services.UserService;

@Service
public class UserServiceImpl implements UserService {

	private RemoteCacheManager remoteCacheManager;

	@Autowired
	public UserServiceImpl(RemoteCacheManager remoteCacheManager) {
		this.remoteCacheManager = remoteCacheManager;
	}

	@Override
	public void createUser(User user) {
		remoteCacheManager.getCache("teams").put(user.getUserName(), user);
	}

	@Override
	public User getUser(String userName) {
		return (User) remoteCacheManager.getCache("teams").get(userName);
	}

}
