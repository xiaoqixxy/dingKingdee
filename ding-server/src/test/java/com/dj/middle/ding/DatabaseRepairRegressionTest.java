package com.dj.middle.ding;

import com.dj.middle.ding.domain.*;
import com.dj.middle.ding.mapper.*;
import com.dj.middle.ding.service.impl.DingOrderServiceImpl;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class DatabaseRepairRegressionTest {
    @Test
    void businessMappersParseAndCreationTimeIsPersisted() throws Exception {
        Configuration config = new Configuration();
        for (String name : new String[]{"DingOrder", "DingTenant", "DingInvoice", "DingSyncLog",
                "DingPaymentLog", "DingPackageChangeLog", "DingSubscription", "DingDashboard"}) {
            String resource = "mapper/ding/" + name + "Mapper.xml";
            try (InputStream in = getClass().getClassLoader().getResourceAsStream(resource)) {
                assertNotNull(in, resource);
                new XMLMapperBuilder(in, config, resource, config.getSqlFragments()).parse();
            }
        }
        DingOrder order = new DingOrder();
        order.setDingCorpId("test-corp");
        order.setOrderNo("test-order");
        String sql = config.getMappedStatement(DingOrderMapper.class.getName() + ".insertDingOrder")
                .getBoundSql(order).getSql();
        assertTrue(sql.contains("create_time"));
        assertTrue(sql.contains("COALESCE"));
        DingSubscription filter = new DingSubscription();
        filter.setStatus(1);
        filter.setProductType(2);
        sql = config.getMappedStatement(DingSubscriptionMapper.class.getName() + ".selectDingSubscriptionList")
                .getBoundSql(filter).getSql();
        assertTrue(sql.contains("s.status ="));
        assertTrue(sql.contains("s.product_type ="));
    }

    @Test
    void newAnnualSubscriptionUsesProductTypeInsteadOfNewPurchaseOrderType() throws Exception {
        assertProductType(false);
    }

    @Test
    void existingAnnualSubscriptionUsesProductTypeInsteadOfNewPurchaseOrderType() throws Exception {
        assertProductType(true);
    }

    private void assertProductType(boolean existing) throws Exception {
        DingProduct product = new DingProduct();
        product.setId(1L);
        product.setProductType(2);
        DingSubscription previous = new DingSubscription();
        AtomicReference<DingSubscription> saved = new AtomicReference<>();
        DingProductMapper products = (DingProductMapper) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{DingProductMapper.class}, (p, m, a) -> product);
        DingSubscriptionMapper subscriptions = (DingSubscriptionMapper) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{DingSubscriptionMapper.class}, (p, m, a) -> {
                    if (m.getName().startsWith("select")) return existing ? previous : null;
                    saved.set((DingSubscription) a[0]);
                    return 1;
                });
        DingOrderServiceImpl service = new DingOrderServiceImpl();
        inject(service, "dingProductMapper", products);
        inject(service, "dingSubscriptionMapper", subscriptions);
        DingOrder order = new DingOrder();
        order.setProductId(1L);
        order.setOrderType(1);
        order.setDingCorpId("test-corp");
        service.createSubscription(order);
        assertNotNull(saved.get());
        assertEquals(2, saved.get().getProductType());
    }

    private void inject(Object object, String name, Object value) throws Exception {
        var field = object.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(object, value);
    }
}
