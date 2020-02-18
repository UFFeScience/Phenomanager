package com.uff.model.invoker.repository.core;

import java.util.Calendar;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import com.uff.model.invoker.domain.core.BaseApiEntity;

@NoRepositoryBean
public interface BaseRepository<ENTITY extends BaseApiEntity> extends JpaRepository<ENTITY, Long> {
	
	@Transactional
	@Modifying(clearAutomatically = true)
    @Query("UPDATE #{#entityName} as E SET E.active = false, E.deleteDate = :deleteDate WHERE E.slug = :slug")
    Integer logicDelete(@Param("slug") String slug, @Param("deleteDate") Calendar deleteDate);

	void deleteBySlug(String slug);

	ENTITY findOneBySlug(String slug);
	
}