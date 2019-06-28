package com.uff.phenomanager.domain.api.scimanager;

import java.io.Serializable;

public class ProfileImage implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Long profileImageId;
	private byte[] profileImageContent;
	private User user;
	
	public ProfileImage() {}
	
	public ProfileImage(ProfileImageBuilder builder) {
		this.profileImageId = builder.profileImageId;
		this.profileImageContent = builder.profileImageContent;
		this.user = builder.user;
	}
	
	public Long getProfileImageId() {
		return profileImageId;
	}

	public void setProfileImageId(Long profileImageId) {
		this.profileImageId = profileImageId;
	}

	public byte[] getProfileImageContent() {
		return profileImageContent;
	}

	public void setProfileImageContent(byte[] profileImageContent) {
		this.profileImageContent = profileImageContent;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public static ProfileImageBuilder builder() {
		return new ProfileImageBuilder();
	}
	
	public static class ProfileImageBuilder {
		
		private Long profileImageId;
		private byte[] profileImageContent;
		private User user;
		
		public ProfileImageBuilder profileImageId(Long profileImageId) {
			this.profileImageId = profileImageId;
			return this;
		}
		
		public ProfileImageBuilder profileImageContent(byte[] profileImageContent) {
			this.profileImageContent = profileImageContent;
			return this;
		}
		
		public ProfileImageBuilder user(User user) {
			this.user = user;
			return this;
		}
		
		public ProfileImage build() {
			return new ProfileImage(this);
		}
	}

}