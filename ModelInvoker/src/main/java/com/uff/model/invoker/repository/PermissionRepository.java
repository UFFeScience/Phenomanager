package com.uff.model.invoker.repository;

import com.uff.model.invoker.domain.Permission;
import com.uff.model.invoker.repository.core.BaseRepository;

public interface PermissionRepository extends BaseRepository<Permission>, PermissionRepositoryCustom {}