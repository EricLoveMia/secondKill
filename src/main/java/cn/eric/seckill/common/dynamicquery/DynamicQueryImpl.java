package cn.eric.seckill.common.dynamicquery;

import org.hibernate.SQLQuery;
import org.hibernate.query.internal.NativeQueryImpl;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

/**
 * @author Eric
 * @version 1.0
 * @ClassName: DynamicQueryImpl
 * @Description: TODO
 * @company lsj
 * @date 2019/7/30 16:24
 **/
@Repository
public class DynamicQueryImpl implements DynamicQuery {

    Logger logger = LoggerFactory.getLogger(DynamicQueryImpl.class);

    @PersistenceContext
    private EntityManager em;

    public EntityManager getEntityManager() {
        return em;
    }

    @Override
    public void save(Object entity) {
        em.persist(entity);
    }

    @Override
    public void update(Object entity) {
        em.merge(entity);
    }

    @Override
    public <T> void delete(Class<T> entityClass, Object entityId) {
        delete(entityClass, new Object[] { entityId });
    }

    @Override
    public <T> void delete(Class<T> entityClass, Object[] entityIds) {
        for (Object id : entityIds) {
            em.remove(em.getReference(entityClass, id));
        }
    }

    private Query createNativeQuery(String sql, Object... params) {
        Query query = em.createNativeQuery(sql);
        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                // 与Hibernate 不同,jpa query从位置1开始
                query.setParameter(i + 1, params[i]);
            }
        }
        return query;
    }

    /** hibernate 5.2 之后，SQLQuery.class、setResultTransformer方法已作废 */
    @Override
    public <T> List<T> nativeQueryList(String nativeSql, Object... params) {
        Query q = createNativeQuery(nativeSql,params);
        q.unwrap(NativeQueryImpl.class).setResultTransformer(Transformers.TO_LIST);
        return q.getResultList();
    }

    @Override
    public <T> List<T> nativeQueryListMap(String nativeSql, Object... params) {
        Query q = createNativeQuery(nativeSql,params);
        q.unwrap(NativeQueryImpl.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        return q.getResultList();
    }

    @Override
    public <T> List<T> nativeQueryListModel(Class<T> resultClass, String nativeSql, Object... params) {
        Query q = createNativeQuery(nativeSql, params);;
        q.unwrap(NativeQueryImpl.class).setResultTransformer(Transformers.aliasToBean(resultClass));
        return q.getResultList();
    }

    @Override
    public Object nativeQueryObject(String nativeSql, Object... params) {
        return createNativeQuery(nativeSql, params).getSingleResult();
    }

    @Override
    public Object[] nativeQueryArray(String nativeSql, Object... params) {
        return (Object[]) createNativeQuery(nativeSql, params).getSingleResult();
    }

    @Override
    public int nativeExecuteUpdate(String nativeSql, Object... params) {
        return createNativeQuery(nativeSql, params).executeUpdate();
    }
}
