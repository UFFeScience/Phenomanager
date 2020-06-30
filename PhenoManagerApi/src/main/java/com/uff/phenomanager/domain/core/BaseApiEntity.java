package com.uff.phenomanager.domain.core;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@MappedSuperclass
@JsonInclude(Include.NON_EMPTY)
public class BaseApiEntity {
	
	@Id
	@GeneratedValue(generator = "pooled")
	@GenericGenerator(name = "pooled", strategy = "org.hibernate.id.enhanced.TableGenerator", parameters = {
	        @Parameter(name = "value_column_name", value = "sequence_next_hi_value"),
	        @Parameter(name = "prefer_entity_table_as_segment_value", value = "true"),
	        @Parameter(name = "optimizer", value = "pooled-lo"),
	        @Parameter(name = "increment_size", value = "100")})
	@Column(name = "id", nullable = false, updatable = false)
	private Long id;
	
	@Column(name = "slug", nullable = false, updatable = false, length = 32)
	private String slug;
	
	@Column(name = "active", nullable = false)
	private Boolean active;
	
	@Column(name = "creation_date", nullable = false, updatable = false)
	private Calendar insertDate;
	
	@Column(name = "update_date", nullable = false)
	private Calendar updateDate;
	
	@Column(name = "delete_date")
	private Calendar deleteDate;
	
	@Transient
	private Map<String, Object> sum = new HashMap<String, Object>();
	
	@Transient
	private Map<String, Object> avg = new HashMap<String, Object>();
	
	@Transient
	private Map<String, Object> count = new HashMap<String, Object>();
	
	public BaseApiEntity() {}

	public BaseApiEntity(BaseApiEntityBuilder builder) {
		this.id = builder.id;
		this.slug = builder.slug;
		this.active = builder.active;
		this.insertDate = builder.insertDate;
		this.updateDate = builder.updateDate;
		this.deleteDate = builder.removeDate;
		this.sum = builder.sum;
		this.avg = builder.avg;
		this.count = builder.count;
	}
	
	@JsonIgnore
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Calendar getInsertDate() {
		return insertDate;
	}

	public void setInsertDate(Calendar insertDate) {
		this.insertDate = insertDate;
	}

	public Calendar getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Calendar updateDate) {
		this.updateDate = updateDate;
	}

	public Calendar getDeleteDate() {
		return deleteDate;
	}

	public void setDeleteDate(Calendar deleteDate) {
		this.deleteDate = deleteDate;
	}
	
	public Map<String, Object> getSum() {
		return sum;
	}

	public void setSum(Map<String, Object> sum) {
		this.sum = sum;
	}
	
	@SuppressWarnings("unchecked")
	public void addSum(Map<String, Object> sum) {
		Map.Entry<String, Object> sumEntry = sum.entrySet().iterator().next();
		Map<String, Object> sumCurrent = this.sum;
		
		while (sumCurrent.get(sumEntry.getKey()) != null) {
			sumCurrent = (Map<String, Object>) sumCurrent.get(sumEntry.getKey());
			sum = (Map<String, Object>) sumEntry.getValue();
			sumEntry = sum.entrySet().iterator().next();
		}
		
		sumCurrent.put(sumEntry.getKey(), sumEntry.getValue());
	}

	public Map<String, Object> getAvg() {
		return avg;
	}

	public void setAvg(Map<String, Object> avg) {
		this.avg = avg;
	}
	
	@SuppressWarnings("unchecked")
	public void addAvg(Map<String, Object> avg) {
		Map.Entry<String, Object> avgEntry = avg.entrySet().iterator().next();
		Map<String, Object> avgCurrent = this.avg;
		
		while (avgCurrent.get(avgEntry.getKey()) != null) {
			avgCurrent = (Map<String, Object>) avgCurrent.get(avgEntry.getKey());
			avg = (Map<String, Object>) avgEntry.getValue();
			avgEntry = avg.entrySet().iterator().next();
		}
		
		avgCurrent.put(avgEntry.getKey(), avgEntry.getValue());
	}

	public Map<String, Object> getCount() {
		return count;
	}

	public void setCount(Map<String, Object> count) {
		this.count = count;
	}
	
	@SuppressWarnings("unchecked")
	public void addCount(Map<String, Object> count) {
		Map.Entry<String, Object> countEntry = count.entrySet().iterator().next();
		Map<String, Object> countCurrent = this.count;
		
		while (countCurrent.get(countEntry.getKey()) != null) {
			countCurrent = (Map<String, Object>) countCurrent.get(countEntry.getKey());
			count = (Map<String, Object>) countEntry.getValue();
			countEntry = count.entrySet().iterator().next();
		}
		
		countCurrent.put(countEntry.getKey(), countEntry.getValue());
	}
	
	public static BaseApiEntityBuilder builder() {
		return new BaseApiEntityBuilder();
	}
	
	public static class BaseApiEntityBuilder {
		
		private Long id;
		private String slug;
		private Boolean active = Boolean.TRUE;
		private Calendar insertDate;
		private Calendar updateDate;
		private Calendar removeDate;
		private Map<String, Object> sum = new HashMap<String, Object>();
		private Map<String, Object> avg = new HashMap<String, Object>();
		private Map<String, Object> count = new HashMap<String, Object>();
		
		public BaseApiEntityBuilder id(Long id) {
			this.id = id;
			return this;
		}
		
		public BaseApiEntityBuilder slug(String slug) {
			this.slug = slug;
			return this;
		}
		
		public BaseApiEntityBuilder active(Boolean active) {
			this.active = active;
			return this;
		}
		
		public BaseApiEntityBuilder insertDate(Calendar insertDate) {
			this.insertDate = insertDate;
			return this;
		}
		
		public BaseApiEntityBuilder updateDate(Calendar updateDate) {
			this.updateDate = updateDate;
			return this;
		}
		
		public BaseApiEntityBuilder removeDate(Calendar removeDate) {
			this.removeDate = removeDate;
			return this;
		}
		
		public BaseApiEntityBuilder sum(Map<String, Object> sum) {
			this.sum = sum;
			return this;
		}
		
		public BaseApiEntityBuilder avg(Map<String, Object> avg) {
			this.avg = avg;
			return this;
		}
		
		public BaseApiEntityBuilder count(Map<String, Object> count) {
			this.count = count;
			return this;
		}
		
		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getSlug() {
			return slug;
		}

		public void setSlug(String slug) {
			this.slug = slug;
		}

		public Boolean getActive() {
			return active;
		}

		public void setActive(Boolean active) {
			this.active = active;
		}

		public Calendar getInsertDate() {
			return insertDate;
		}

		public void setInsertDate(Calendar insertDate) {
			this.insertDate = insertDate;
		}

		public Calendar getUpdateDate() {
			return updateDate;
		}

		public void setUpdateDate(Calendar updateDate) {
			this.updateDate = updateDate;
		}

		public Calendar getRemoveDate() {
			return removeDate;
		}

		public void setRemoveDate(Calendar removeDate) {
			this.removeDate = removeDate;
		}

		public Map<String, Object> getSum() {
			return sum;
		}

		public void setSum(Map<String, Object> sum) {
			this.sum = sum;
		}

		public Map<String, Object> getAvg() {
			return avg;
		}

		public void setAvg(Map<String, Object> avg) {
			this.avg = avg;
		}

		public Map<String, Object> getCount() {
			return count;
		}

		public void setCount(Map<String, Object> count) {
			this.count = count;
		}
	}
	
}
