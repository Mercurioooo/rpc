package src;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
	private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	//这个用来存放服务名和对应的接口
	private static HashMap<String,Class> serviceRegister = new HashMap<>();
	private static int port;
	private static boolean isRunning = false;
	public Server(int port){
	    this.port = port;
	}	
	public void start() {//while(true){start();}
		
		//start ->线程对象  
		ServerSocket server = null ;
		try {
			server = new ServerSocket();
			server.bind(new InetSocketAddress(port));
		
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		isRunning = true ; //服务已经启动
		while(true) {
			//具体的服务内容：接收客户端请求，处理请求，并返回结果
				//100 :1  1   1 ...1  -->如果想让多个 客户端请求并发执行 
				//-> 多线程
				System.out.println("start  server....");
				//客户端每次请求一次连接（发出一次请求），则服务端 从连接池中 
				//获取一个线程对象去处理
				Socket socket = null ;
				try {
					 socket = server.accept();// 等待客户端连接
				} catch (IOException e) {
					e.printStackTrace();
				}
				//启动线程 去处理客户请求
				executor.execute(new ServiceTask(socket) );
			
		}
	}
	
	public void stop() {
		isRunning = false;
		executor.shutdown();
	}
	
	public void register(Class service,Class serviceImpl) {
		serviceRegister.put(service.getName(), serviceImpl);
	}
	
	private static class ServiceTask implements Runnable{
		private Socket socket ; 
		public ServiceTask() {};
		public ServiceTask(Socket socket) {
			this.socket = socket ;
		}
		@Override
		public void run() {
			ObjectOutputStream output = null;
			ObjectInputStream input = null;
			try {
				  input = new ObjectInputStream( socket.getInputStream());
				  //因为ObjectInputStream对发送数据的顺序严格要求,因此需要参照发送的顺序逐个接受
				  String serviceName = input.readUTF();
				  String methodName = input.readUTF();
				  Class[] parameterTypes = (Class[])input.readObject();
				  Object[] arguments = (Object[])input.readObject();
				  //根据客户请求,在map(serviceRegister)找到具体接口
				  Class ServiceClass = serviceRegister.get(serviceName);
				  Method method = ServiceClass.getMethod(methodName,parameterTypes);
				  //执行该方法,需要类,和参数列表
				  Object result = method.invoke(ServiceClass.newInstance(),arguments);
				  //将方法执行完毕的返回值,传给客户端
				  output = new ObjectOutputStream(socket.getOutputStream());
				  output.writeObject(result);
				
			}catch (Exception e) {
				e.printStackTrace();
			}finally {
				try {
					if (output != null) output.close();
					if (input != null) input.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
