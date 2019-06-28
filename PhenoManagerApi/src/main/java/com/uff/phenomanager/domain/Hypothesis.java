package com.uff.phenomanager.domain;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "hypothesis")
public class Hypothesis extends ResearchObject {
	
	@Column(name = "ranking")
	private Long ranking;
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "id_phenomenon")
	private Phenomenon phenomenon;
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "id_parent_hypothesis", referencedColumnName = "id")
	private Hypothesis parentHypothesis;
	
	@Column(name = "state")
	@Enumerated(EnumType.STRING)
	private HypothesisState state = HypothesisState.FORMULATED;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "hypothesis")
	@Cascade(CascadeType.DELETE)
	@JsonManagedReference(value = "hypothesisPermissions")
	private Set<Permission> permissions;
	
	public Hypothesis() {
		super();
	}
	
	public Hypothesis(HypothesisBuilder builder) {
		this.ranking = builder.ranking;
		this.phenomenon = builder.phenomenon;
		this.parentHypothesis = builder.parentHypothesis;
		this.permissions = builder.permissions;
		this.state = builder.state;
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
	
	public Long getRanking() {
		return ranking;
	}

	public void setRanking(Long ranking) {
		this.ranking = ranking;
	}

	public Phenomenon getPhenomenon() {
		return phenomenon;
	}

	public void setPhenomenon(Phenomenon phenomenon) {
		this.phenomenon = phenomenon;
	}

	public Hypothesis getParentHypothesis() {
		return parentHypothesis;
	}

	public void setParentHypothesis(Hypothesis parentHypothesis) {
		this.parentHypothesis = parentHypothesis;
	}

	public Set<Permission> getPermissions() {
		return permissions;
	}

	public void setPermissions(Set<Permission> permissions) {
		this.permissions = permissions;
	}
	
	public HypothesisState getState() {
		return state;
	}

	public void setState(HypothesisState state) {
		this.state = state;
	}

	public static HypothesisBuilder builder() {
		return new HypothesisBuilder();
	}
	
	public static class HypothesisBuilder extends ResearchObjectBuilder {
		
		private Long ranking;
		private Phenomenon phenomenon;
		private Hypothesis parentHypothesis;
		private HypothesisState state = HypothesisState.FORMULATED;
		private Set<Permission> permissions;
		
		public HypothesisBuilder ranking(Long ranking) {
			this.ranking = ranking;
			return this;
		}
		
		public HypothesisBuilder state(HypothesisState state) {
			this.state = state;
			return this;
		}
		
		public HypothesisBuilder phenomenon(Phenomenon phenomenon) {
			this.phenomenon = phenomenon;
			return this;
		}
		
		public HypothesisBuilder parentHypothesis(Hypothesis parentHypothesis) {
			this.parentHypothesis = parentHypothesis;
			return this;
		}
		
		public HypothesisBuilder permissions(Set<Permission> permissions) {
			this.permissions = permissions;
			return this;
		}
		
		public Hypothesis build() {
			return new Hypothesis(this);
		}
	}

}