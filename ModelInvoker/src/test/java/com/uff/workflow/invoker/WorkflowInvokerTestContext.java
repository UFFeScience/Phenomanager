package com.uff.workflow.invoker;

import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

public class WorkflowInvokerTestContext {

	@Bean(destroyMethod = "shutdown")
	public EmbeddedDatabase dataSource() {
	    return new EmbeddedDatabaseBuilder().
	            setType(EmbeddedDatabaseType.H2).
	            addScript("db/migration/creates.sql").	            
	            build();
	}
	
}
