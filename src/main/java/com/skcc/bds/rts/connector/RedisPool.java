package com.skcc.bds.rts.connector;

import java.util.Map;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool.Config;

//import com.zoona.bd.realtime.RedisLogger.JedisPoolConfig;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisPool {
	
	JedisPool pool;
	
	public RedisPool(String ip, int port) {
		try
		{
		//Config config = new Config();
			pool =new JedisPool(new JedisPoolConfig(), ip, port);
		}catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
	}
	
	public Jedis getRedisPool() {
		return pool.getResource();
	}
	
	public void returnRedisPool(Jedis jedis){
		pool.returnResource(jedis);
	}
	
	public String getList(Jedis jedis, String key) {
		String result = jedis.lpop(key);
		if(result == null) result = "false";
		return result;
	}
	
}
