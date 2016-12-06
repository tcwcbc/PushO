package test;

import java.nio.ByteOrder;

import util.Utils;

public class MessageProtocolTest {
	public static void main(String[] args) {
		new MsgBasedOnProtocol();
	}
	
	
	static class MsgBasedOnProtocol{
		private static final String name = "이름이";
		private static final String passwd = "비밀번호";
		public MsgBasedOnProtocol() {
			
			
			
//			byteTest();
		}
		
		private void byteTest() {
			byte[] bName = name.getBytes();
			byte[] bPasswd = passwd.getBytes();
			byte[] ret = new byte[bName.length+bPasswd.length];
			System.arraycopy(bPasswd, 0, ret, 0, bPasswd.length);
			System.arraycopy(bName, 0, ret, bPasswd.length, bName.length);
			
			System.out.println(
					Utils.intTobyte(1202012312).length
					);
		}
	}
}
