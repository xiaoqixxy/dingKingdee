package com.dj.middle.admin;

import com.dj.middle.admin.mapper.AdminRecordMapper;
import com.dj.middle.ding.domain.DingSubscription;
import com.dj.middle.ding.mapper.DingSubscriptionMapper;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class EnterpriseListJoinTest {
    private JdbcDataSource source() {
        var source=new JdbcDataSource();
        source.setURL("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        new JdbcTemplate(source).execute("CREATE TABLE ding_tenant(id bigint primary key,ding_corp_id varchar(64) unique,corp_name varchar(100))");
        new JdbcTemplate(source).execute("INSERT INTO ding_tenant VALUES(1,'known','企业名称')");
        return source;
    }

    @Test void recordPaginationRetainsOrphansAndFiltersBeforeJoining() {
        var source=source(); var jdbc=new JdbcTemplate(source);
        jdbc.execute("CREATE TABLE ding_sync_log(id bigint,ding_corp_id varchar(64),ding_form_id varchar(64),sync_count int,status int,error_msg varchar(100),create_time timestamp,trace_id varchar(64),execution_id bigint)");
        jdbc.execute("INSERT INTO ding_sync_log VALUES(1,'known','form',1,1,null,'2026-09-28',null,null),(2,'missing','form',2,1,null,'2026-09-28',null,null)");
        var config=new Configuration(new Environment("test",new JdbcTransactionFactory(),source));
        config.addMapper(AdminRecordMapper.class);
        try(var session=new SqlSessionFactoryBuilder().build(config).openSession()) {
            var mapper=session.getMapper(AdminRecordMapper.class);
            var query=new AdminRecordQuery();query.setPageSize(1);
            assertEquals(2,mapper.count("syncLog",query));
            var first=mapper.list("syncLog",query);
            assertEquals(1,first.size()); assertEquals("missing",first.get(0).get("ding_corp_id"));
            assertNull(first.get(0).get("corp_name"));
            query.setPageNum(2);
            assertEquals("企业名称",mapper.list("syncLog",query).get(0).get("corp_name"));
            query.setPageNum(1);query.setDingCorpId("known");
            assertEquals(1,mapper.count("syncLog",query));
            assertEquals("企业名称",mapper.list("syncLog",query).get(0).get("corp_name"));
        }
    }

    @Test void subscriptionNameMappingPreservesCorpIdLookup() throws Exception {
        var source=source(); var jdbc=new JdbcTemplate(source);
        jdbc.execute("CREATE TABLE ding_product(id bigint,product_name varchar(100))");
        jdbc.execute("CREATE TABLE ding_subscription(id bigint,ding_corp_id varchar(64),product_id bigint,product_type int,start_time timestamp,end_time timestamp,auto_renew int,used_form_count int,month_used_sync int,status int,ext1 varchar(100),ext2 varchar(100),ext3 varchar(100))");
        jdbc.execute("INSERT INTO ding_product VALUES(1,'套餐')");
        jdbc.execute("INSERT INTO ding_subscription(id,ding_corp_id,product_id,status) VALUES(1,'known',1,1),(2,'missing',1,1)");
        var config=new Configuration(new Environment("test",new JdbcTransactionFactory(),source));
        try(var xml=getClass().getResourceAsStream("/mapper/ding/DingSubscriptionMapper.xml")) {
            new XMLMapperBuilder(xml,config,"DingSubscriptionMapper.xml",config.getSqlFragments()).parse();
        }
        try(var session=new SqlSessionFactoryBuilder().build(config).openSession()) {
            var mapper=session.getMapper(DingSubscriptionMapper.class);
            assertEquals(2,mapper.selectDingSubscriptionList(new DingSubscription()).size());
            var known=mapper.selectDingSubscriptionByCorpId("known");
            assertEquals("企业名称",known.getCorpName());
            assertEquals("套餐",known.getProductName());
            assertNull(mapper.selectDingSubscriptionByCorpId("missing").getCorpName());
        }
    }
}
