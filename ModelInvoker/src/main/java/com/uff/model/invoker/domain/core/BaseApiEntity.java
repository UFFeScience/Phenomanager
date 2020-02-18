package com.uff.model.invoker.domain.core;

import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

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
	
	public BaseApiEntity() {}

	public BaseApiEntity(BaseApiEntityBuilder builder) {
		this.id = builder.id;
		this.slug = builder.slug;
		this.active = builder.active;
		this.insertDate = builder.insertDate;
		this.updateDate = builder.updateDate;
		this.deleteDate = builder.removeDate;
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
	}
	
}
