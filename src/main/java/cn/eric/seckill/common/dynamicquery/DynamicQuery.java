package cn.eric.seckill.common.dynamicquery;

import java.util.List;

/**
 * @author Eric
 * @version 1.0
 * @ClassName: DynamicQuery
 * @Description: TODO
 * @company lsj
 * @date 2019/7/30 16:18
 **/
public interface DynamicQuery {

    void save(Object entity);

    void update(Object entity);

    <T> void delete(Class<T> entityClass,Object entityId);

    <T> void delete(Class<T> entityClass,Object[] entityIds);

    /**
     * 查询对象列表，返回List
     * @author eric
     * @date 16:21 2019/7/30
     * @param nativeSql
     * @param params
     * @throws
     * @return java.util.List<T>
     **/
    <T> List<T> nativeQueryList(String nativeSql, Object... params);

    /**
     * 查询对象列表，返回Map
     * @author eric
     * @date 16:21 2019/7/30
     * @param nativeSql
     * @param params
     * @throws
     * @return java.util.List<T> List<Map<key,value>>
     **/
    <T> List<T> nativeQueryListMap(String nativeSql, Object... params);


    /**
     * 查询对象列表，返回List<组合对象>
     * @author Eric
     * @date 16:23 2019/7/30
     * @param resultClass
     * @param nativeSql
     * @param params
     * @throws
     * @return java.util.List<T>
     **/
    <T> List<T> nativeQueryListModel(Class<T> resultClass, String nativeSql, Object... params);

    /**
     * 执行nativeSql统计查询
     * @param nativeSql
     * @param params 占位符参数(例如?1)绑定的参数值
     * @return 统计条数
     */
    Object nativeQueryObject(String nativeSql, Object... params);

    /**
     * 执行nativeSql统计查询
     * @param nativeSql
     * @param params 占位符参数(例如?1)绑定的参数值
     * @return 统计条数
     */
    Object[] nativeQueryArray(String nativeSql, Object... params);

    /**
     * 执行nativeSql的update,delete操作
     * @param nativeSql
     * @param params
     * @return
     */
    int nativeExecuteUpdate(String nativeSql, Object... params);
}
