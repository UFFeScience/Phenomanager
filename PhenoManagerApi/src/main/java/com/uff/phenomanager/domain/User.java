package com.uff.phenomanager.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "user_account")
public class User extends BaseApiEntity {
	
	@Column(name = "name", length = 80)
	private String name;
	
	@Column(name = "institution_name", length = 100)
	private String institutionName;
	
	@Column(name = "profile_image_file_id")
	private String profileImageFileId;

	@Column(name = "email", unique = true, length = 150)
	private String email;
	
	@Column(name = "password", columnDefinition = "text")
	private String password;
	
	@Column(name = "role")
	@Enumerated(EnumType.STRING)
	private Role role;
	
	@Transient
	private String imageContentText;
	
	public User() {}
	
	public User(UserBuilder builder) {
		this.name = builder.name;
		this.profileImageFileId = builder.profileImageFileId;
		this.email = builder.email;
		this.institutionName = builder.institutionName;
		this.password = builder.password;
		this.role = builder.role;
		this.setId(builder.getId());
		this.setSlug(builder.getSlug());
		this.setActive(builder.getActive());
		this.setInsertDate(builder.getInsertDate());
		this.setUpdateDate(builder.getUpdateDate());
		this.setDeleteDate(builder.getRemoveDate());
		this.setSum(builder.getSum());
		this.setAvg(builder.getAvg());
		this.setCount(builder.getCount());
		this.setCountDistinct(builder.getCountDistinct());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getProfileImageFileId() {
		return profileImageFileId;
	}

	public void setProfileImageFileId(String profileImageFileId) {
		this.profileImageFileId = profileImageFileId;
	}

	public String getInstitutionName() {
		return institutionName;
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

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}
	
	public void setInstitutionName(String institutionName) {
		this.institutionName = institutionName;
	}
	
	public String getImageContentText() {
		return imageContentText;
	}

	public void setImageContentText(String imageContentText) {
		this.imageContentText = imageContentText;
	}

	public static UserBuilder builder() {
		return new UserBuilder();
	}
	
	public static class UserBuilder extends BaseApiEntityBuilder {
		
		private String name;
		private String profileImageFileId;
		private String email;
		private String institutionName;
		private String password;
		private Role role;
		
		public UserBuilder name(String name) {
			this.name = name;
			return this;
		}
		
		public UserBuilder profileImageFileId(String profileImageFileId) {
			this.profileImageFileId = profileImageFileId;
			return this;
		}
		
		public UserBuilder email(String email) {
			this.email = email;
			return this;
		}
		
		public UserBuilder institutionName(String institutionName) {
			this.institutionName = institutionName;
			return this;
		}
		
		public UserBuilder password(String password) {
			this.password = password;
			return this;
		}
		
		public UserBuilder role(Role role) {
			this.role = role;
			return this;
		}
		
		public User build() {
			return new User(this);
		}
	}

}