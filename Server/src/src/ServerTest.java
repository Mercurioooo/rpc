package src;

public class ServerTest {
	public static void main(String[] args) {
		//开启一个线程
		new Thread(new Runnable() {
			@Override
			public void run() {
				//服务中心
				Server server = new Server(9999);
				//将HelloService接口及实现类 注册到 服务中心
				server.register(HelloServiceInterface.class, HelloService.class);
				server.start(); 
			}
		}).start();//start()
	}
}
