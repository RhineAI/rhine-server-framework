package com.rhine.framework.util

import cn.hutool.json.JSONUtil
import org.springframework.data.redis.connection.DataType
import org.springframework.data.redis.core.Cursor
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ScanOptions
import org.springframework.data.redis.core.ZSetOperations.TypedTuple
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Redis工具类
 *
 * @author WangFan
 * @date 2018-02-24 下午03:09:50
 * @version 1.1 (GitHub文档: https://github.com/whvcse/RedisUtil )
 */

@Component
class RedisUtil(private val redisTemplate: RedisTemplate<String, Any>) {

    /** -------------------key相关操作---------------------  */
    /**
     * 删除key
     *
     * @param key
     */
    fun delete(key: String) {
        redisTemplate.delete(key)
    }

    /**
     * 批量删除key
     *
     * @param keys
     */
    fun delete(keys: Collection<String?>) {
        redisTemplate.delete(keys)
    }

    /**
     * 序列化key
     *
     * @param key
     * @return
     */
    fun dump(key: String): ByteArray {
        return redisTemplate.dump(key)
    }

    /**
     * 是否存在key
     *
     * @param key
     * @return
     */
    fun hasKey(key: String): Boolean {
        return redisTemplate.hasKey(key)
    }

    /**
     * 设置过期时间
     *
     * @param key
     * @param timeout
     * @param unit
     * @return
     */
    fun expire(key: String, timeout: Long, unit: TimeUnit?): Boolean {
        return redisTemplate.expire(key, timeout, unit!!)
    }

    /**
     * 设置过期时间
     *
     * @param key
     * @param date
     * @return
     */
    fun expireAt(key: String, date: Date?): Boolean {
        return redisTemplate.expireAt(key, date!!)
    }

    /**
     * 查找匹配的key
     *
     * @param pattern
     * @return
     */
    fun keys(pattern: String): Set<String> {
        return redisTemplate.keys(pattern)
    }

    /**
     * 将当前数据库的 key 移动到给定的数据库 db 当中
     *
     * @param key
     * @param dbIndex
     * @return
     */
    fun move(key: String, dbIndex: Int): Boolean {
        return redisTemplate.move(key, dbIndex)
    }

    /**
     * 移除 key 的过期时间，key 将持久保持
     *
     * @param key
     * @return
     */
    fun persist(key: String): Boolean {
        return redisTemplate.persist(key)
    }

    /**
     * 返回 key 的剩余的过期时间
     *
     * @param key
     * @param unit
     * @return
     */
    fun getExpire(key: String, unit: TimeUnit?): Long {
        return redisTemplate.getExpire(key, unit!!)
    }

    /**
     * 返回 key 的剩余的过期时间
     *
     * @param key
     * @return
     */
    fun getExpire(key: String): Long {
        return redisTemplate.getExpire(key)
    }

    /**
     * 从当前数据库中随机返回一个 key
     *
     * @return
     */
    fun randomKey(): String {
        return redisTemplate.randomKey()
    }

    /**
     * 修改 key 的名称
     *
     * @param oldKey
     * @param newKey
     */
    fun rename(oldKey: String, newKey: String) {
        redisTemplate.rename(oldKey, newKey)
    }

    /**
     * 仅当 newkey 不存在时，将 oldKey 改名为 newkey
     *
     * @param oldKey
     * @param newKey
     * @return
     */
    fun renameIfAbsent(oldKey: String, newKey: String): Boolean {
        return redisTemplate.renameIfAbsent(oldKey, newKey)
    }

    /**
     * 返回 key 所储存的值的类型
     *
     * @param key
     * @return
     */
    fun type(key: String): DataType {
        return redisTemplate.type(key)
    }

    /** -------------------string相关操作---------------------  */
    /**
     * 设置指定 key 的值
     *
     * @param key
     * @param value
     */
    fun set(key: String?, value: String?) {
        redisTemplate.opsForValue()[key!!] = value!!
    }

    /**
     * 获取指定 key 的值
     *
     * @param key
     * @return
     */
    fun get(key: String?): Any? {
        return redisTemplate.opsForValue()[key!!]
    }

    /**
     * 返回 key 中字符串值的子字符
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    fun getRange(key: String?, start: Long, end: Long): String? {
        return redisTemplate.opsForValue()[key!!, start, end]
    }

    /**
     * 将给定 key 的值设为 value ，并返回 key 的旧值(old value)
     *
     * @param key
     * @param value
     * @return
     */
    fun getAndSet(key: String?, value: String?): Any? {
        return redisTemplate.opsForValue().getAndSet(key!!, value!!)
    }

    /**
     * 对 key 所储存的字符串值，获取指定偏移量上的位(bit)
     *
     * @param key
     * @param offset
     * @return
     */
    fun getBit(key: String?, offset: Long): Boolean? {
        return redisTemplate.opsForValue().getBit(key!!, offset)
    }

    /**
     * 批量获取
     *
     * @param keys
     * @return
     */
    fun multiGet(keys: Collection<String?>?): List<Any>? {
        return redisTemplate.opsForValue().multiGet(keys!!)
    }

    /**
     * 设置ASCII码, 字符串'a'的ASCII码是97, 转为二进制是'01100001', 此方法是将二进制第offset位值变为value
     *
     * @param key
     * @param postion 位置
     * @param value   值,true为1, false为0
     * @return
     */
    fun setBit(key: String?, offset: Long, value: Boolean): Boolean {
        return redisTemplate.opsForValue().setBit(key!!, offset, value)!!
    }

    /**
     * 将值 value 关联到 key ，并将 key 的过期时间设为 timeout
     *
     * @param key
     * @param value
     * @param timeout 过期时间
     * @param unit    时间单位, 天:TimeUnit.DAYS 小时:TimeUnit.HOURS 分钟:TimeUnit.MINUTES
     * 秒:TimeUnit.SECONDS 毫秒:TimeUnit.MILLISECONDS
     */
    fun setEx(key: String?, value: String?, timeout: Long, unit: TimeUnit?) {
        redisTemplate.opsForValue().set(key!!,value!!,timeout,unit!!)
    }

    /**
     * 只有在 key 不存在时设置 key 的值
     *
     * @param key
     * @param value
     * @return 之前已经存在返回false, 不存在返回true
     */
    fun setIfAbsent(key: String?, value: String?): Boolean {
        return redisTemplate.opsForValue().setIfAbsent(key!!, value!!)!!
    }

    /**
     * 用 value 参数覆写给定 key 所储存的字符串值，从偏移量 offset 开始
     *
     * @param key
     * @param value
     * @param offset 从指定位置开始覆写
     */
    fun setRange(key: String?, value: String?, offset: Long) {
        redisTemplate.opsForValue()[key!!, value!!] = offset
    }

    /**
     * 获取字符串的长度
     *
     * @param key
     * @return
     */
    fun size(key: String?): Long? {
        return redisTemplate.opsForValue().size(key!!)
    }

    /**
     * 批量添加
     *
     * @param maps
     */
    fun multiSet(maps: Map<String?, String?>?) {
        redisTemplate.opsForValue().multiSet(maps!!)
    }

    /**
     * 同时设置一个或多个 key-value 对，当且仅当所有给定 key 都不存在
     *
     * @param maps
     * @return 之前已经存在返回false, 不存在返回true
     */
    fun multiSetIfAbsent(maps: Map<String?, String?>?): Boolean {
        return redisTemplate.opsForValue().multiSetIfAbsent(maps!!)!!
    }

    /**
     * 增加(自增长), 负数则为自减
     *
     * @param key
     * @param value
     * @return
     */
    fun incrBy(key: String?, increment: Long): Long? {
        return redisTemplate.opsForValue().increment(key!!, increment)
    }

    /**
     * @param key
     * @param value
     * @return
     */
    fun incrByFloat(key: String?, increment: Double): Double? {
        return redisTemplate.opsForValue().increment(key!!, increment)
    }

    /**
     * 追加到末尾
     *
     * @param key
     * @param value
     * @return
     */
    fun append(key: String?, value: String?): Int? {
        return redisTemplate.opsForValue().append(key!!, value!!)
    }

    /** -------------------hash相关操作-------------------------  */
    /**
     * 获取存储在哈希表中指定字段的值
     *
     * @param key
     * @param field
     * @return
     */
    fun hGet(key: String?, field: String?): Any? {
        return redisTemplate.opsForHash<Any, Any>()[key!!, field!!]
    }

    /**
     * 获取所有给定字段的值
     *
     * @param key
     * @return
     */
    fun hGetAll(key: String?): Map<Any, Any> {
        return redisTemplate.opsForHash<Any, Any>().entries(key!!)
    }

    /**
     * 获取所有给定字段的值
     *
     * @param key
     * @param fields
     * @return
     */
    fun hMultiGet(key: String?, fields: Collection<Any>?): List<Any> {
        return redisTemplate.opsForHash<Any, Any>().multiGet(key!!, fields!!)
    }

    fun hPut(key: String?, hashKey: String, value: String) {
        redisTemplate.opsForHash<Any, Any>().put(key!!, hashKey, value)
    }

    fun hPutAll(key: String?, maps: Map<String, String>?) {
        redisTemplate.opsForHash<Any, Any>().putAll(key!!, maps!!)
    }

    /**
     * 仅当hashKey不存在时才设置
     *
     * @param key
     * @param hashKey
     * @param value
     * @return
     */
    fun hPutIfAbsent(key: String?, hashKey: String, value: String): Boolean {
        return redisTemplate.opsForHash<Any, Any>().putIfAbsent(key!!, hashKey, value)
    }

    /**
     * 删除一个或多个哈希表字段
     *
     * @param key
     * @param fields
     * @return
     */
    fun hDelete(key: String?, vararg fields: Any?): Long {
        return redisTemplate.opsForHash<Any, Any>().delete(key!!, *fields)
    }

    /**
     * 查看哈希表 key 中，指定的字段是否存在
     *
     * @param key
     * @param field
     * @return
     */
    fun hExists(key: String?, field: String?): Boolean {
        return redisTemplate.opsForHash<Any, Any>().hasKey(key!!, field!!)
    }

    /**
     * 为哈希表 key 中的指定字段的整数值加上增量 increment
     *
     * @param key
     * @param field
     * @param increment
     * @return
     */
    fun hIncrBy(key: String?, field: Any, increment: Long): Long {
        return redisTemplate.opsForHash<Any, Any>().increment(key!!, field, increment)
    }

    /**
     * 为哈希表 key 中的指定字段的整数值加上增量 increment
     *
     * @param key
     * @param field
     * @param delta
     * @return
     */
    fun hIncrByFloat(key: String?, field: Any, delta: Double): Double {
        return redisTemplate.opsForHash<Any, Any>().increment(key!!, field, delta)
    }

    /**
     * 获取所有哈希表中的字段
     *
     * @param key
     * @return
     */
    fun hKeys(key: String?): Set<Any> {
        return redisTemplate.opsForHash<Any, Any>().keys(key!!)
    }

    /**
     * 获取哈希表中字段的数量
     *
     * @param key
     * @return
     */
    fun hSize(key: String?): Long {
        return redisTemplate.opsForHash<Any, Any>().size(key!!)
    }

    /**
     * 获取哈希表中所有值
     *
     * @param key
     * @return
     */
    fun hValues(key: String?): List<Any> {
        return redisTemplate.opsForHash<Any, Any>().values(key!!)
    }

    /**
     * 迭代哈希表中的键值对
     *
     * @param key
     * @param options
     * @return
     */
    fun hScan(key: String?, options: ScanOptions?): Cursor<Map.Entry<Any, Any>> {
        return redisTemplate.opsForHash<Any, Any>().scan(key!!, options!!)
    }

    /** ------------------------list相关操作----------------------------  */
    /**
     * 通过索引获取列表中的元素
     *
     * @param key
     * @param index
     * @return
     */
    fun lIndex(key: String?, index: Long): Any? {
        return redisTemplate.opsForList().index(key!!, index)
    }

    /**
     * 获取列表指定范围内的元素
     *
     * @param key
     * @param start 开始位置, 0是开始位置
     * @param end   结束位置, -1返回所有
     * @return
     */
    fun lRange(key: String?, start: Long, end: Long): List<Any>? {
        return redisTemplate.opsForList().range(key!!, start, end)
    }

    /**
     * 存储在list头部
     *
     * @param key
     * @param value
     * @return
     */
    fun lLeftPush(key: String?, value: String?): Long? {
        return redisTemplate.opsForList().leftPush(key!!, value!!)
    }

    /**
     * @param key
     * @param value
     * @return
     */
    fun lLeftPushAll(key: String?, vararg value: String?): Long? {
        return redisTemplate.opsForList().leftPushAll(key!!, *value)
    }

    /**
     * @param key
     * @param value
     * @return
     */
    fun lLeftPushAll(key: String?, value: Collection<String?>?): Long? {
        return redisTemplate.opsForList().leftPushAll(key!!, value!!)
    }

    /**
     * 当list存在的时候才加入
     *
     * @param key
     * @param value
     * @return
     */
    fun lLeftPushIfPresent(key: String?, value: String?): Long? {
        return redisTemplate.opsForList().leftPushIfPresent(key!!, value!!)
    }

    /**
     * 如果pivot存在,再pivot前面添加
     *
     * @param key
     * @param pivot
     * @param value
     * @return
     */
    fun lLeftPush(key: String?, pivot: String?, value: String?): Long? {
        return redisTemplate.opsForList().leftPush(key!!, pivot!!, value!!)
    }

    /**
     * @param key
     * @param value
     * @return
     */
    fun lRightPush(key: String?, value: String?): Long? {
        return redisTemplate.opsForList().rightPush(key!!, value!!)
    }

    /**
     * @param key
     * @param value
     * @return
     */
    fun lRightPushAll(key: String?, vararg value: String?): Long? {
        return redisTemplate.opsForList().rightPushAll(key!!, *value)
    }

    /**
     * @param key
     * @param value
     * @return
     */
    fun lRightPushAll(key: String?, value: Collection<String?>?): Long? {
        return redisTemplate.opsForList().rightPushAll(key!!, value!!)
    }

    /**
     * 为已存在的列表添加值
     *
     * @param key
     * @param value
     * @return
     */
    fun lRightPushIfPresent(key: String?, value: String?): Long? {
        return redisTemplate.opsForList().rightPushIfPresent(key!!, value!!)
    }

    /**
     * 在pivot元素的右边添加值
     *
     * @param key
     * @param pivot
     * @param value
     * @return
     */
    fun lRightPush(key: String?, pivot: String?, value: String?): Long? {
        return redisTemplate.opsForList().rightPush(key!!, pivot!!, value!!)
    }

    /**
     * 通过索引设置列表元素的值
     *
     * @param key
     * @param index 位置
     * @param value
     */
    fun lSet(key: String?, index: Long, value: String?) {
        redisTemplate.opsForList()[key!!, index] = value!!
    }

    /**
     * 移出并获取列表的第一个元素
     *
     * @param key
     * @return 删除的元素
     */
    fun lLeftPop(key: String?): Any? {
        return redisTemplate.opsForList().leftPop(key!!)
    }

    /**
     * 移出并获取列表的第一个元素， 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止
     *
     * @param key
     * @param timeout 等待时间
     * @param unit    时间单位
     * @return
     */
    fun lBLeftPop(key: String?, timeout: Long, unit: TimeUnit?): Any? {
        return redisTemplate.opsForList().leftPop(key!!, timeout, unit!!)
    }

    /**
     * 移除并获取列表最后一个元素
     *
     * @param key
     * @return 删除的元素
     */
    fun lRightPop(key: String?): Any? {
        return redisTemplate.opsForList().rightPop(key!!)
    }

    /**
     * 移出并获取列表的最后一个元素， 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止
     *
     * @param key
     * @param timeout 等待时间
     * @param unit    时间单位
     * @return
     */
    fun lBRightPop(key: String?, timeout: Long, unit: TimeUnit?): Any? {
        return redisTemplate.opsForList().rightPop(key!!, timeout, unit!!)
    }

    /**
     * 移除列表的最后一个元素，并将该元素添加到另一个列表并返回
     *
     * @param sourceKey
     * @param destinationKey
     * @return
     */
    fun lRightPopAndLeftPush(sourceKey: String?, destinationKey: String?): Any? {
        return redisTemplate.opsForList().rightPopAndLeftPush(
            sourceKey!!,
            destinationKey!!
        )
    }

    /**
     * 从列表中弹出一个值，将弹出的元素插入到另外一个列表中并返回它； 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止
     *
     * @param sourceKey
     * @param destinationKey
     * @param timeout
     * @param unit
     * @return
     */
    fun lBRightPopAndLeftPush(
        sourceKey: String?, destinationKey: String?,
        timeout: Long, unit: TimeUnit?,
    ): Any? {
        return redisTemplate.opsForList().rightPopAndLeftPush(
            sourceKey!!,
            destinationKey!!, timeout, unit!!
        )
    }

    /**
     * 删除集合中值等于value得元素
     *
     * @param key
     * @param index index=0, 删除所有值等于value的元素; index>0, 从头部开始删除第一个值等于value的元素;
     * index<0, 从尾部开始删除第一个值等于value的元素;
     * @param value
     * @return
     */
    fun lRemove(key: String?, index: Long, value: String?): Long? {
        return redisTemplate.opsForList().remove(key!!, index, value!!)
    }

    /**
     * 裁剪list
     *
     * @param key
     * @param start
     * @param end
     */
    fun lTrim(key: String?, start: Long, end: Long) {
        redisTemplate.opsForList().trim(key!!, start, end)
    }

    /**
     * 获取列表长度
     *
     * @param key
     * @return
     */
    fun lLen(key: String?): Long? {
        return redisTemplate.opsForList().size(key!!)
    }

    /** --------------------set相关操作--------------------------  */
    /**
     * set添加元素
     *
     * @param key
     * @param values
     * @return
     */
    fun sAdd(key: String?, vararg values: String?): Long? {
        return redisTemplate.opsForSet().add(key!!, *values)
    }

    /**
     * set移除元素
     *
     * @param key
     * @param values
     * @return
     */
    fun sRemove(key: String?, vararg values: Any?): Long? {
        return redisTemplate.opsForSet().remove(key!!, *values)
    }

    /**
     * 移除并返回集合的一个随机元素
     *
     * @param key
     * @return
     */
    fun sPop(key: String?): Any? {
        return redisTemplate.opsForSet().pop(key!!)
    }

    /**
     * 将元素value从一个集合移到另一个集合
     *
     * @param key
     * @param value
     * @param destKey
     * @return
     */
    fun sMove(key: String?, value: String?, destKey: String?): Boolean? {
        return redisTemplate.opsForSet().move(key!!, value!!, destKey!!)
    }

    /**
     * 获取集合的大小
     *
     * @param key
     * @return
     */
    fun sSize(key: String?): Long? {
        return redisTemplate.opsForSet().size(key!!)
    }

    /**
     * 判断集合是否包含value
     *
     * @param key
     * @param value
     * @return
     */
    fun sIsMember(key: String?, value: Any?): Boolean? {
        return redisTemplate.opsForSet().isMember(key!!, value!!)
    }

    /**
     * 获取两个集合的交集
     *
     * @param key
     * @param otherKey
     * @return
     */
    fun sIntersect(key: String?, otherKey: String?): Set<Any>? {
        return redisTemplate.opsForSet().intersect(key!!, otherKey!!)
    }

    /**
     * 获取key集合与多个集合的交集
     *
     * @param key
     * @param otherKeys
     * @return
     */
    fun sIntersect(key: String?, otherKeys: Collection<String?>?): Set<Any>? {
        return redisTemplate.opsForSet().intersect(key!!, otherKeys!!)
    }

    /**
     * key集合与otherKey集合的交集存储到destKey集合中
     *
     * @param key
     * @param otherKey
     * @param destKey
     * @return
     */
    fun sIntersectAndStore(key: String?, otherKey: String?, destKey: String?): Long? {
        return redisTemplate.opsForSet().intersectAndStore(
            key!!, otherKey!!,
            destKey!!
        )
    }

    /**
     * key集合与多个集合的交集存储到destKey集合中
     *
     * @param key
     * @param otherKeys
     * @param destKey
     * @return
     */
    fun sIntersectAndStore(
        key: String?, otherKeys: Collection<String?>?,
        destKey: String?,
    ): Long? {
        return redisTemplate.opsForSet().intersectAndStore(
            key!!, otherKeys!!,
            destKey!!
        )
    }

    /**
     * 获取两个集合的并集
     *
     * @param key
     * @param otherKeys
     * @return
     */
    fun sUnion(key: String?, otherKeys: String?): Set<Any>? {
        return redisTemplate.opsForSet().union(key!!, otherKeys!!)
    }

    /**
     * 获取key集合与多个集合的并集
     *
     * @param key
     * @param otherKeys
     * @return
     */
    fun sUnion(key: String?, otherKeys: Collection<String?>?): Set<Any>? {
        return redisTemplate.opsForSet().union(key!!, otherKeys!!)
    }

    /**
     * key集合与otherKey集合的并集存储到destKey中
     *
     * @param key
     * @param otherKey
     * @param destKey
     * @return
     */
    fun sUnionAndStore(key: String?, otherKey: String?, destKey: String?): Long? {
        return redisTemplate.opsForSet().unionAndStore(key!!, otherKey!!, destKey!!)
    }

    /**
     * key集合与多个集合的并集存储到destKey中
     *
     * @param key
     * @param otherKeys
     * @param destKey
     * @return
     */
    fun sUnionAndStore(
        key: String?, otherKeys: Collection<String?>?,
        destKey: String?,
    ): Long? {
        return redisTemplate.opsForSet().unionAndStore(key!!, otherKeys!!, destKey!!)
    }

    /**
     * 获取两个集合的差集
     *
     * @param key
     * @param otherKey
     * @return
     */
    fun sDifference(key: String?, otherKey: String?): Set<Any>? {
        return redisTemplate.opsForSet().difference(key!!, otherKey!!)
    }

    /**
     * 获取key集合与多个集合的差集
     *
     * @param key
     * @param otherKeys
     * @return
     */
    fun sDifference(key: String?, otherKeys: Collection<String?>?): Set<Any>? {
        return redisTemplate.opsForSet().difference(key!!, otherKeys!!)
    }

    /**
     * key集合与otherKey集合的差集存储到destKey中
     *
     * @param key
     * @param otherKey
     * @param destKey
     * @return
     */
    fun sDifference(key: String?, otherKey: String?, destKey: String?): Long? {
        return redisTemplate.opsForSet().differenceAndStore(
            key!!, otherKey!!,
            destKey!!
        )
    }

    /**
     * key集合与多个集合的差集存储到destKey中
     *
     * @param key
     * @param otherKeys
     * @param destKey
     * @return
     */
    fun sDifference(
        key: String?, otherKeys: Collection<String?>?,
        destKey: String?,
    ): Long? {
        return redisTemplate.opsForSet().differenceAndStore(
            key!!, otherKeys!!,
            destKey!!
        )
    }

    /**
     * 获取集合所有元素
     *
     * @param key
     * @param otherKeys
     * @param destKey
     * @return
     */
    fun setMembers(key: String?): Set<Any>? {
        return redisTemplate.opsForSet().members(key!!)
    }

    /**
     * 随机获取集合中的一个元素
     *
     * @param key
     * @return
     */
    fun sRandomMember(key: String?): Any {
        return redisTemplate.opsForSet().randomMember(key!!)
    }

    /**
     * 随机获取集合中count个元素
     *
     * @param key
     * @param count
     * @return
     */
    fun sRandomMembers(key: String?, count: Long): List<Any>? {
        return redisTemplate.opsForSet().randomMembers(key!!, count)
    }

    /**
     * 随机获取集合中count个元素并且去除重复的
     *
     * @param key
     * @param count
     * @return
     */
    fun sDistinctRandomMembers(key: String?, count: Long): Set<Any>? {
        return redisTemplate.opsForSet().distinctRandomMembers(key!!, count)
    }

    /**
     * @param key
     * @param options
     * @return
     */
    fun sScan(key: String?, options: ScanOptions?): Cursor<Any> {
        return redisTemplate.opsForSet().scan(key!!, options!!)
    }

    /**------------------zSet相关操作-------------------------------- */
    /**
     * 添加元素,有序集合是按照元素的score值由小到大排列
     *
     * @param key
     * @param value
     * @param score
     * @return
     */
    fun zAdd(key: String?, value: String?, score: Double): Boolean? {
        return redisTemplate.opsForZSet().add(key!!, value!!, score)
    }

    /**
     * @param key
     * @param values
     * @return
     */
    fun zAdd(key: String?, values: Set<TypedTuple<Any?>?>?): Long? {
        return redisTemplate.opsForZSet().add(key!!, values!!)
    }

    /**
     * @param key
     * @param values
     * @return
     */
    fun zRemove(key: String?, vararg values: Any?): Long? {
        return redisTemplate.opsForZSet().remove(key!!, *values)
    }

    /**
     * 增加元素的score值，并返回增加后的值
     *
     * @param key
     * @param value
     * @param delta
     * @return
     */
    fun zIncrementScore(key: String?, value: String?, delta: Double): Double? {
        return redisTemplate.opsForZSet().incrementScore(key!!, value!!, delta)
    }

    /**
     * 返回元素在集合的排名,有序集合是按照元素的score值由小到大排列
     *
     * @param key
     * @param value
     * @return 0表示第一位
     */
    fun zRank(key: String?, value: Any?): Long? {
        return redisTemplate.opsForZSet().rank(key!!, value!!)
    }

    /**
     * 返回元素在集合的排名,按元素的score值由大到小排列
     *
     * @param key
     * @param value
     * @return
     */
    fun zReverseRank(key: String?, value: Any?): Long? {
        return redisTemplate.opsForZSet().reverseRank(key!!, value!!)
    }

    /**
     * 获取集合的元素, 从小到大排序
     *
     * @param key
     * @param start 开始位置
     * @param end   结束位置, -1查询所有
     * @return
     */
    fun zRange(key: String?, start: Long, end: Long): Set<Any>? {
        return redisTemplate.opsForZSet().range(key!!, start, end)
    }

    /**
     * 获取集合元素, 并且把score值也获取
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    fun zRangeWithScores(
        key: String?, start: Long,
        end: Long,
    ): Set<TypedTuple<Any>>? {
        return redisTemplate.opsForZSet().rangeWithScores(key!!, start, end)
    }

    /**
     * 根据Score值查询集合元素
     *
     * @param key
     * @param min 最小值
     * @param max 最大值
     * @return
     */
    fun zRangeByScore(key: String?, min: Double, max: Double): Set<Any>? {
        return redisTemplate.opsForZSet().rangeByScore(key!!, min, max)
    }

    /**
     * 根据Score值查询集合元素, 从小到大排序
     *
     * @param key
     * @param min 最小值
     * @param max 最大值
     * @return
     */
    fun zRangeByScoreWithScores(
        key: String?,
        min: Double, max: Double,
    ): Set<TypedTuple<Any>>? {
        return redisTemplate.opsForZSet().rangeByScoreWithScores(key!!, min, max)
    }

    /**
     * @param key
     * @param min
     * @param max
     * @param start
     * @param end
     * @return
     */
    fun zRangeByScoreWithScores(
        key: String?,
        min: Double, max: Double, start: Long, end: Long,
    ): Set<TypedTuple<Any>>? {
        return redisTemplate.opsForZSet().rangeByScoreWithScores(
            key!!, min, max,
            start, end
        )
    }

    /**
     * 获取集合的元素, 从大到小排序
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    fun zReverseRange(key: String?, start: Long, end: Long): Set<Any>? {
        return redisTemplate.opsForZSet().reverseRange(key!!, start, end)
    }

    /**
     * 获取集合的元素, 从大到小排序, 并返回score值
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    fun zReverseRangeWithScores(
        key: String?,
        start: Long, end: Long,
    ): Set<TypedTuple<Any>>? {
        return redisTemplate.opsForZSet().reverseRangeWithScores(
            key!!, start,
            end
        )
    }

    /**
     * 根据Score值查询集合元素, 从大到小排序
     *
     * @param key
     * @param min
     * @param max
     * @return
     */
    fun zReverseRangeByScore(
        key: String?, min: Double,
        max: Double,
    ): Set<Any>? {
        return redisTemplate.opsForZSet().reverseRangeByScore(key!!, min, max)
    }

    /**
     * 根据Score值查询集合元素, 从大到小排序
     *
     * @param key
     * @param min
     * @param max
     * @return
     */
    fun zReverseRangeByScoreWithScores(
        key: String?, min: Double, max: Double,
    ): Set<TypedTuple<Any>>? {
        return redisTemplate.opsForZSet().reverseRangeByScoreWithScores(
            key!!,
            min, max
        )
    }

    /**
     * @param key
     * @param min
     * @param max
     * @param start
     * @param end
     * @return
     */
    fun zReverseRangeByScore(
        key: String?, min: Double,
        max: Double, start: Long, end: Long,
    ): Set<Any>? {
        return redisTemplate.opsForZSet().reverseRangeByScore(
            key!!, min, max,
            start, end
        )
    }

    /**
     * 根据score值获取集合元素数量
     *
     * @param key
     * @param min
     * @param max
     * @return
     */
    fun zCount(key: String?, min: Double, max: Double): Long? {
        return redisTemplate.opsForZSet().count(key!!, min, max)
    }

    /**
     * 获取集合大小
     *
     * @param key
     * @return
     */
    fun zSize(key: String?): Long? {
        return redisTemplate.opsForZSet().size(key!!)
    }

    /**
     * 获取集合大小
     *
     * @param key
     * @return
     */
    fun zZCard(key: String?): Long? {
        return redisTemplate.opsForZSet().zCard(key!!)
    }

    /**
     * 获取集合中value元素的score值
     *
     * @param key
     * @param value
     * @return
     */
    fun zScore(key: String?, value: Any?): Double? {
        return redisTemplate.opsForZSet().score(key!!, value!!)
    }

    /**
     * 移除指定索引位置的成员
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    fun zRemoveRange(key: String?, start: Long, end: Long): Long? {
        return redisTemplate.opsForZSet().removeRange(key!!, start, end)
    }

    /**
     * 根据指定的score值的范围来移除成员
     *
     * @param key
     * @param min
     * @param max
     * @return
     */
    fun zRemoveRangeByScore(key: String?, min: Double, max: Double): Long? {
        return redisTemplate.opsForZSet().removeRangeByScore(key!!, min, max)
    }

    /**
     * 获取key和otherKey的并集并存储在destKey中
     *
     * @param key
     * @param otherKey
     * @param destKey
     * @return
     */
    fun zUnionAndStore(key: String?, otherKey: String?, destKey: String?): Long? {
        return redisTemplate.opsForZSet().unionAndStore(key!!, otherKey!!, destKey!!)
    }

    /**
     * @param key
     * @param otherKeys
     * @param destKey
     * @return
     */
    fun zUnionAndStore(
        key: String?, otherKeys: Collection<String?>?,
        destKey: String?,
    ): Long? {
        return redisTemplate.opsForZSet()
            .unionAndStore(key!!, otherKeys!!, destKey!!)
    }

    /**
     * 交集
     *
     * @param key
     * @param otherKey
     * @param destKey
     * @return
     */
    fun zIntersectAndStore(
        key: String?, otherKey: String?,
        destKey: String?,
    ): Long? {
        return redisTemplate.opsForZSet().intersectAndStore(
            key!!, otherKey!!,
            destKey!!
        )
    }

    /**
     * 交集
     *
     * @param key
     * @param otherKeys
     * @param destKey
     * @return
     */
    fun zIntersectAndStore(
        key: String?, otherKeys: Collection<String?>?,
        destKey: String?,
    ): Long? {
        return redisTemplate.opsForZSet().intersectAndStore(
            key!!, otherKeys!!,
            destKey!!
        )
    }

    /**
     * @param key
     * @param options
     * @return
     */
    fun zScan(key: String?, options: ScanOptions?): Cursor<TypedTuple<Any>> {
        return redisTemplate.opsForZSet().scan(key!!, options!!)
    }

    fun getTTL(key: String): Long {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS)
    }

    fun acquireLock(key: String, expireMillis: Long): Boolean {
        val value = "locked:${System.currentTimeMillis()}"
        return redisTemplate.opsForValue().setIfAbsent(key, value, expireMillis, TimeUnit.MILLISECONDS) ?: false
    }

    fun releaseLock(key: String) {
        redisTemplate.delete(key)
    }

    fun getBean(key: String, clazz: Class<*>): Any {
        val json = get(key).toString()
        if (JSONUtil.isTypeJSON(json)){
            return JSONUtil.toBean(json, clazz)
        }
        else return json
    }
}