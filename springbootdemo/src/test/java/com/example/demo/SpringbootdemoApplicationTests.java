package com.example.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringbootdemoApplicationTests {

	@Test
	public void contextLoads() {
	}
	@Autowired
	private Sender sender;

	@Test
	public void sendTest() throws Exception {
		while (true){
			System.out.println("====================BEGIN=====================");
			int i=3;
			String send_userId = "jchong";
			String time = "20180419";
			String msg =  i+"."+time+"."+send_userId;
			String[] msgneed = msg.split("\\.");
			System.out.println("i:"+msgneed[0]);
			int picNum = Integer.parseInt(msgneed[0]);
			for (int j=0;j<picNum;j++){
				System.out.println("啦啦啦");
			}
			System.out.println("time:"+msgneed[1]);
			System.out.println("send_userId:"+msgneed[2]);
			sender.sendWeb(msg);
			System.out.println("====================END=====================");
			Thread.sleep(2000);
		}
	}
}
