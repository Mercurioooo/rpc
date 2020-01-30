package src;

import java.net.InetSocketAddress;

public class ClientTest {
	public static void main(String[] args) throws ClassNotFoundException {
		HelloServiceInterface service = Client.getRemoteProxyObj(
				Class.forName("src.HelloServiceInterface") , 
				new InetSocketAddress("127.0.0.1", 9999)) ;	
		System.out.println( service.sayHi("zs")  ) ;
	}
}
