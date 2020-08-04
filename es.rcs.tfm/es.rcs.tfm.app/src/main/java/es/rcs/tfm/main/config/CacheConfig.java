package es.rcs.tfm.main.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import es.rcs.tfm.db.DbNames;
import es.rcs.tfm.main.AppNames;

@EnableCaching
@Configuration( 
		AppNames.BBDD_CACHE_CONFIG )
public class CacheConfig {

	@Bean( DbNames.DB_CACHE_MANAGER )
	public CacheManager cacheManager() {
		
		ConcurrentMapCacheManager bean = new ConcurrentMapCacheManager();
		return bean;
		
	}
	
}
