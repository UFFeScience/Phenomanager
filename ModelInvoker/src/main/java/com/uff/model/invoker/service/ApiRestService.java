package com.uff.model.invoker.service;

import java.util.Calendar;
import java.util.List;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.uff.model.invoker.domain.BaseApiEntity;
import com.uff.model.invoker.repository.BaseRepository;
import com.uff.model.invoker.util.KeyUtils;

@Service
public abstract class ApiRestService<ENTITY extends BaseApiEntity, REPOSITORY extends BaseRepository<ENTITY>> {
	
	protected abstract REPOSITORY getRepository();
	protected abstract Class<ENTITY> getEntityClass();
	
	public ENTITY findBySlug(String slug) {
		return (ENTITY) getRepository().findOneBySlug(slug);
	}
	
	public Page<ENTITY> findAll(Pageable pageable) {
		return getRepository().findAll(pageable);
	}
	
	public Page<ENTITY> findAll(Example<ENTITY> example, Pageable pageable) {
		return getRepository().findAll(example, pageable);
	}
	
	public ENTITY update(ENTITY entity) {
      entity.setUpdateDate(Calendar.getInstance());
      
      if (entity.getActive() != null && entity.getActive()) {
         entity.setDeleteDate(null);
      }
      
      return getRepository().saveAndFlush(entity);
   }

   public void delete(String slug) {
      getRepository().deleteBySlug(slug);
   }
   
   public void deleteInBatch(List<ENTITY> entities) {
      getRepository().deleteInBatch(entities);
   }

	public ENTITY save(ENTITY entity) {
		if (entity.getSlug() == null || "".equals(entity.getSlug())) {
			entity.setSlug(KeyUtils.generate());
		}
		entity.setInsertDate(Calendar.getInstance());
		entity.setUpdateDate(entity.getInsertDate());
		entity.setActive(Boolean.TRUE);
		entity.setDeleteDate(null);

		return (ENTITY) getRepository().saveAndFlush(entity);
	}
	
}