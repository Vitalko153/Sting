package server;

import server.handler.ByteBufInputHandler;
import server.handler.OutputHandler;
import server.handler.StringInputHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

    public class NettyBaseServer {
        public NettyBaseServer() {
            EventLoopGroup auth = new NioEventLoopGroup(1); // light
            EventLoopGroup worker = new NioEventLoopGroup();

            try {
                ServerBootstrap bootstrap = new ServerBootstrap();

                bootstrap.group(auth, worker)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer() {
                            @Override
                            protected void initChannel(Channel channel) throws Exception {
                                channel.pipeline().addLast(
                                        new ByteBufInputHandler(), // in - 1
                                        new OutputHandler(), // out - 1
                                        new StringInputHandler() // in - 2
                                );
                            }
                        });
                ChannelFuture future = bootstrap.bind(6003).sync();
                System.out.println("Server started");
                future.channel().closeFuture().sync();
                System.out.println("Server finished");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                auth.shutdownGracefully();
                worker.shutdownGracefully();
            }
        }

        public static void main(String[] args) {
            new NettyBaseServer();
        }
    }
