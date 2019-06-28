package com.uff.phenomanager.domain.api.scimanager;

import java.io.Serializable;

public class User implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String slug;
	private Long userId;
	private String username;
	private String institution;
	private Boolean hasProfileImage = Boolean.FALSE;
	private String email;
	private String password;
	private Role userRole;
	private ProfileImage profileImage;
	
	public User() {}
	
	public User(UserBuilder builder) {
		this.slug = builder.slug;
		this.userId = builder.userId;
		this.username = builder.username;
		this.hasProfileImage = builder.hasProfileImage;
		this.email = builder.email;
		this.institution = builder.institution;
		this.password = builder.password;
		this.userRole = builder.userRole;
		this.profileImage = builder.profileImage;
	}
	
	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Boolean getHasProfileImage() {
		return hasProfileImage;
	}

	public void setHasProfileImage(Boolean hasProfileImage) {
		this.hasProfileImage = hasProfileImage;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Role getUserRole() {
		return userRole;
	}

	public void setUserRole(Role userRole) {
		this.userRole = userRole;
	}
	
	public String getInstitution() {
		return institution;
	}

	public void setInstitution(String institution) {
		this.institution = institution;
	}
	
	public ProfileImage getProfileImage() {
		return profileImage;
	}

	public void setProfileImage(ProfileImage profileImage) {
		this.profileImage = profileImage;
	}

	public static UserBuilder builder() {
		return new UserBuilder();
	}
	
	public static class UserBuilder {
		
		private String slug;
		private Long userId;
		private String username;
		private Boolean hasProfileImage;
		private String email;
		private String institution;
		private String password;
		private Role userRole;
		private ProfileImage profileImage;
		
		public UserBuilder slug(String slug) {
			this.slug = slug;
			return this;
		}
		
		public UserBuilder userId(Long userId) {
			this.userId = userId;
			return this;
		}
		
		public UserBuilder username(String username) {
			this.username = username;
			return this;
		}
		
		public UserBuilder hasProfileImage(Boolean hasProfileImage) {
			this.hasProfileImage = hasProfileImage;
			return this;
		}
		
		public UserBuilder email(String email) {
			this.email = email;
			return this;
		}
		
		public UserBuilder institution(String institution) {
			this.institution = institution;
			return this;
		}
		
		public UserBuilder password(String password) {
			this.password = password;
			return this;
		}
		
		public UserBuilder userRole(Role userRole) {
			this.userRole = userRole;
			return this;
		}
		
		public UserBuilder profileImage(ProfileImage profileImage) {
			this.profileImage = profileImage;
			return this;
		}
		
		public User build() {
			return new User(this);
		}
	}

}