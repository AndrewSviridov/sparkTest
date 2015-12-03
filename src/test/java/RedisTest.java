import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import redis.clients.jedis.Jedis;


public class RedisTest {

	@Test
	public void test() throws IOException {
		Jedis jedis = new Jedis("10.250.42.249", 6379);
//		long result = jedis.lpush("redisbook", "hello redis!");
//		System.out.println(jedis.lpop("redisbook1"));
		
		FileInputStream fisTargetFile = new FileInputStream(new File("D:/00.Source/600.Scala/RealtimeProcessor/src/main/resources/rule_event_1.drl"));

		System.out.println("Resuddddddddddlt111333 : ");
		String targetFileStr = IOUtils.toString(fisTargetFile, "UTF-8");
		//System.out.println("DRL : " + targetFileStr);
		
		long result = jedis.lpush("workflowId:ruldFile", targetFileStr);
		
		System.out.println("Result : " + jedis.lrange("workflowId:ruldFile", 0, 0));
//		
//		System.out.println("Result : " + result);
	}
	
	@Test
	public void testNull() throws IOException {
		Jedis jedis = new Jedis("10.250.42.249", 6379);
		String result = jedis.lpop("test");
		
		System.out.println("Result : " + result);
	}
}
