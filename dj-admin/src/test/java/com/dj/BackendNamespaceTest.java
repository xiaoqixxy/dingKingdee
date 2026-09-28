package com.dj;

import com.dj.common.config.DjConfig;
import com.dj.common.constant.CacheConstants;
import com.dj.common.core.domain.model.LoginUser;
import com.dj.framework.config.FastJson2JsonRedisSerializer;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class BackendNamespaceTest {
    @Test void mapperXmlNamespacesAndDomainAliasesResolve() throws Exception {
        Configuration configuration = new Configuration();
        for (String packageName : new String[]{"common.core.domain", "system.domain", "quartz.domain",
                "generator.domain", "middle.ding.domain", "middle.kingdee.domain"}) {
            configuration.getTypeAliasRegistry().registerAliases("com.dj." + packageName);
        }
        var resources = new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/**/*Mapper.xml");
        assertTrue(resources.length > 20, "Must include all backend modules, not just admin");
        for (var resource : resources) {
            try (var input = resource.getInputStream()) {
                new XMLMapperBuilder(input, configuration, resource.toString(), configuration.getSqlFragments()).parse();
            }
        }
        configuration.getMappedStatementNames(); // Resolve pending cross-mapper references.
        assertTrue(configuration.hasStatement("com.dj.system.mapper.SysUserMapper.selectUserList"));
        assertTrue(configuration.hasStatement("com.dj.middle.ding.mapper.DingSyncLogMapper.selectDingSyncLogList"));
    }

    @Test void projectConfigurationBindsToDjPrefix() {
        Binder binder = new Binder(new MapConfigurationPropertySource(Map.of("dj.name", "DJ", "dj.version", "3.9.1")));
        DjConfig config = binder.bind("dj", DjConfig.class).get();
        assertEquals("DJ", config.getName());
        assertEquals("3.9.1", config.getVersion());
    }

    @Test void renamedLoginTypeRoundTripsInIsolatedCacheNamespace() {
        var serializer = new FastJson2JsonRedisSerializer<Object>(Object.class);
        LoginUser user = new LoginUser();
        user.setUserId(42L);
        var account = new com.dj.common.core.domain.entity.SysUser();
        account.setUserId(42L);
        account.setUserName("namespace-test");
        user.setUser(account);
        byte[] bytes = serializer.serialize(user);
        LoginUser restored = assertInstanceOf(LoginUser.class, serializer.deserialize(bytes));
        assertEquals(42L, restored.getUserId());
        assertTrue(new String(bytes, java.nio.charset.StandardCharsets.UTF_8).contains("com.dj."));
        assertEquals("dj:login_tokens:", CacheConstants.LOGIN_TOKEN_KEY);
        assertEquals("dj:sys_dict:", CacheConstants.SYS_DICT_KEY);
    }
}
