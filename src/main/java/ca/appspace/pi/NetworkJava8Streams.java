package ca.appspace.pi;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class NetworkJava8Streams {

	public static void main(String... args) throws IOException {

		final ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.configureBlocking(true);
		serverSocketChannel.bind(new InetSocketAddress(8080));
		
		Stream<SocketChannel> socketStream = Stream.generate(new Supplier<SocketChannel>() {
			@Override
			public SocketChannel get() {
				try {
					return serverSocketChannel.accept();
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
		});
		
		Supplier<SocketAddress> addressSupplier = new Supplier<SocketAddress>() {
			@Override
			public SocketAddress get() {
				// TODO Auto-generated method stub
				return null;
			}
		};
		
		socketStream.forEach(new Consumer<SocketChannel>() {
			@Override
			public void accept(SocketChannel socket) {
				try {
					System.out.println("Connection from "+socket.getRemoteAddress());
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		});
	}

}
