package test;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import server.util.ServerUtils;

public class BlockingQueueTest {
	public static void main(String[] args) {
		String msg = ServerUtils.getEncryptValue("wogud00!" + "b90f0703d2f115dd");
		System.out.println(msg);
	}
}
