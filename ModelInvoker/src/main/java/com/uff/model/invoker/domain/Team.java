package com.uff.model.invoker.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.uff.model.invoker.domain.core.BaseApiEntity;

@Entity
@Table(name = "team")
public class Team extends BaseApiEntity {
		
	@Column(name = "name", unique = true, length = 80)
	private String name;
	
	@ManyToMany
    @JoinTable(name = "team_user_account", joinColumns =
    {@JoinColumn(name = "id_user_account")}, inverseJoinColumns =
   	{@JoinColumn(name = "id_team")})
	@Fetch(FetchMode.SUBSELECT)
	private Set<User> teamUsers; 
	
	public Team() {}
	
	public Team(TeamBuilder builder) {
		this.name = builder.name;
		this.teamUsers = builder.teamUsers;
		this.setId(builder.getId());
		this.setSlug(builder.getSlug());
		this.setActive(builder.getActive());
		this.setInsertDate(builder.getInsertDate());
		this.setUpdateDate(builder.getUpdateDate());
		this.setDeleteDate(builder.getRemoveDate());
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<User> getTeamUsers() {
		if (teamUsers == null) {
			teamUsers = new HashSet<User>();
		}
		
		return teamUsers;
	}

	public void setTeamUsers(Set<User> teamUsers) {
		this.teamUsers = teamUsers;
	}

	public static TeamBuilder builder() {
		return new TeamBuilder();
	}
	
	public static class TeamBuilder extends BaseApiEntityBuilder {
		
		private String name;
		private Set<User> teamUsers; 
		
		public TeamBuilder name(String name) {
			this.name = name;
			return this;
		}
		
		public TeamBuilder teamUsers(Set<User> teamUsers) {
			this.teamUsers = teamUsers;
			return this;
		}
		
		public Team build() {
			return new Team(this);
		}
	}
	
}