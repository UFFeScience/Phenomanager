package com.uff.phenomanager.domain.api.scimanager;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class UserGroup implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String slug;
	private Long userGroupId;
	private String groupName;
	private Set<User> groupUsers; 
	
	public UserGroup() {}
	
	public UserGroup(UserGroupBuilder builder) {
		this.slug = builder.slug;
		this.userGroupId = builder.userGroupId;
		this.groupName = builder.groupName;
		this.groupUsers = builder.groupUsers;
	}
	
	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public Long getUserGroupId() {
		return userGroupId;
	}

	public void setUserGroupId(Long userGroupId) {
		this.userGroupId = userGroupId;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public Set<User> getGroupUsers() {
		if (groupUsers == null) {
			groupUsers = new HashSet<User>();
		}
		
		return groupUsers;
	}

	public void setGroupUsers(Set<User> groupUsers) {
		this.groupUsers = groupUsers;
	}
	
	public void addUserToTeam(User user) {
		if (!containUser(user)) {
			getGroupUsers().add(user);
		}
	}
	
	private Boolean containUser(User newUser) {
		for (User user : getGroupUsers()) {
			if (user.getSlug().equals(newUser.getSlug())) {
				return Boolean.TRUE;
			}
		}
		
		return Boolean.FALSE;
	}

	public static UserGroupBuilder builder() {
		return new UserGroupBuilder();
	}
	
	public static class UserGroupBuilder {
		
		private String slug;
		private Long userGroupId;
		private String groupName;
		private Set<User> groupUsers; 
		
		public UserGroupBuilder slug(String slug) {
			this.slug = slug;
			return this;
		}
		
		public UserGroupBuilder userGroupId(Long userGroupId) {
			this.userGroupId = userGroupId;
			return this;
		}
		
		public UserGroupBuilder groupName(String groupName) {
			this.groupName = groupName;
			return this;
		}
		
		public UserGroupBuilder groupUsers(Set<User> groupUsers) {
			this.groupUsers = groupUsers;
			return this;
		}
		
		public UserGroup build() {
			return new UserGroup(this);
		}
	}
	
}