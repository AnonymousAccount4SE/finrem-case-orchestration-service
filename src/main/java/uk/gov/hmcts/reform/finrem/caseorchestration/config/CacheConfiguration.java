package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

@Configuration
public class CacheConfiguration {

    public static final String REQUEST_SCOPED_CACHE_MANAGER = "requestScopeCacheManager";

    public static final String ORGANISATION_CACHE = "organisationCache";
    public static final String SYS_USER_CACHE = "systemUserCache";
    public static final String BARRISTER_USER_CACHE = "barristerUserCache";
    public static final String USER_ROLES_CACHE = "userRolesCache";

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public CacheManager requestScopeCacheManager() {
        final SimpleCacheManager simpleCacheManager = new SimpleCacheManager();
        simpleCacheManager.setCaches(List.of(
            new ConcurrentMapCache(ORGANISATION_CACHE),
            new ConcurrentMapCache(SYS_USER_CACHE),
            new ConcurrentMapCache(BARRISTER_USER_CACHE),
            new ConcurrentMapCache(USER_ROLES_CACHE)));
        simpleCacheManager.initializeCaches();
        return simpleCacheManager;
    }
}
