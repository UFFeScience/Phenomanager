package com.uff.phenomanager.domain;

import java.math.BigDecimal;
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
	private Map<String, BigDecimal> sum = new HashMap<String, BigDecimal>();
	
	@Transient
	private Map<String, BigDecimal> avg = new HashMap<String, BigDecimal>();
	
	@Transient
	private Map<String, Long> count = new HashMap<String, Long>();
	
	@Transient
	private Map<String, Long> countDistinct = new HashMap<String, Long>();
	
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
		this.countDistinct = builder.countDistinct;
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
	
	public Map<String, BigDecimal> getSum() {
		return sum;
	}

	public void setSum(Map<String, BigDecimal> sum) {
		this.sum = sum;
	}

	public Map<String, BigDecimal> getAvg() {
		return avg;
	}

	public void setAvg(Map<String, BigDecimal> avg) {
		this.avg = avg;
	}

	public Map<String, Long> getCount() {
		return count;
	}

	public void setCount(Map<String, Long> count) {
		this.count = count;
	}
	
	public Map<String, Long> getCountDistinct() {
		return countDistinct;
	}

	public void setCountDistinct(Map<String, Long> countDistinct) {
		this.countDistinct = countDistinct;
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
		private Map<String, BigDecimal> sum = new HashMap<String, BigDecimal>();
		private Map<String, BigDecimal> avg = new HashMap<String, BigDecimal>();
		private Map<String, Long> count = new HashMap<String, Long>();
		private Map<String, Long> countDistinct = new HashMap<String, Long>();
		
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
		
		public BaseApiEntityBuilder sum(Map<String, BigDecimal> sum) {
			this.sum = sum;
			return this;
		}
		
		public BaseApiEntityBuilder avg(Map<String, BigDecimal> avg) {
			this.avg = avg;
			return this;
		}
		
		public BaseApiEntityBuilder count(Map<String, Long> count) {
			this.count = count;
			return this;
		}
		
		public BaseApiEntityBuilder countDistinct(Map<String, Long> countDistinct) {
			this.countDistinct = countDistinct;
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

		public Map<String, BigDecimal> getSum() {
			return sum;
		}

		public void setSum(Map<String, BigDecimal> sum) {
			this.sum = sum;
		}

		public Map<String, BigDecimal> getAvg() {
			return avg;
		}

		public void setAvg(Map<String, BigDecimal> avg) {
			this.avg = avg;
		}

		public Map<String, Long> getCount() {
			return count;
		}

		public void setCount(Map<String, Long> count) {
			this.count = count;
		}

		public Map<String, Long> getCountDistinct() {
			return countDistinct;
		}

		public void setCountDistinct(Map<String, Long> countDistinct) {
			this.countDistinct = countDistinct;
		}
	}
	
}
