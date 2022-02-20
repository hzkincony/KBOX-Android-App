package com.kincony.KControl.net.socket;

import com.kincony.KControl.utils.LogUtils;
import com.kincony.KControl.utils.ThreadUtils;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class NettyServer {

    private boolean isStart;
    private Channel serverChannel;
    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;

    public void startServer(int port) {
        if (isStart) return;

        isStart = true;
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 10)//服务端可连接队列
                .childOption(ChannelOption.SO_KEEPALIVE, true)//保存连接
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new LengthFieldBasedFrameDecoder(20*1024*1024, 0, 4, 0, 4))
                                .addLast(new LengthFieldPrepender(4))
                                .addLast(new StringDecoder())
                                .addLast(new StringEncoder())
                                .addLast(new SimpleChannelInboundHandler<String>() {
                                    @Override
                                    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
                                        LogUtils.INSTANCE.d("NettyServer channelRead0 localAddress=" + ctx.channel().localAddress());
                                        LogUtils.INSTANCE.d("NettyServer channelRead0 remoteAddress=" + ctx.channel().remoteAddress());
                                        LogUtils.INSTANCE.d("NettyServer channelRead0 msg=" + msg);
                                        if (mCallback != null) mCallback.onRead(ctx.channel(), msg);
                                    }

                                    @Override
                                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                        LogUtils.INSTANCE.d("NettyServer channelActive localAddress=" + ctx.channel().localAddress());
                                        LogUtils.INSTANCE.d("NettyServer channelActive remoteAddress=" + ctx.channel().remoteAddress());
                                        if (mCallback != null) mCallback.onActive(ctx.channel());
                                        super.channelActive(ctx);
                                    }

                                    @Override
                                    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                        LogUtils.INSTANCE.d("NettyServer channelInactive localAddress=" + ctx.channel().localAddress());
                                        LogUtils.INSTANCE.d("NettyServer channelInactive remoteAddress=" + ctx.channel().remoteAddress());
                                        if (mCallback != null) mCallback.onInactive(ctx.channel());
                                        super.channelInactive(ctx);
                                    }

                                    @Override
                                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                        LogUtils.INSTANCE.d("NettyServer exceptionCaught localAddress=" + ctx.channel().localAddress());
                                        LogUtils.INSTANCE.d("NettyServer exceptionCaught remoteAddress=" + ctx.channel().remoteAddress());
                                        LogUtils.INSTANCE.d("NettyServer exceptionCaught cause=" + cause);
                                        if (mCallback != null)
                                            mCallback.onError(ctx.channel(), cause);
                                        ctx.channel().close();
                                        super.exceptionCaught(ctx, cause);
                                    }
                                });
                    }
                });

        serverBootstrap.bind(port).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                serverChannel = future.channel();
                if (future.isSuccess()) {
                    if (mCallback != null) mCallback.onStartSuccess();
                    LogUtils.INSTANCE.d("NettyServer start success " + port);
                } else {
                    if (mCallback != null) mCallback.onStartError(future.cause());
                    LogUtils.INSTANCE.d("NettyServer start error=" + future.cause());
                    stopServer();
                }
            }
        });
    }

    public void stopServer() {
        if (!isStart) return;
        isStart = false;
        if (serverChannel != null) serverChannel.close();
        serverChannel = null;
        bossGroup.shutdownGracefully();
        bossGroup = null;
        workerGroup.shutdownGracefully();
        workerGroup = null;
        LogUtils.INSTANCE.d("NettyServer stop");
    }

    private Callback mCallback;

    public void setCallback(Callback callback) {
        if (callback == null) {
            mCallback = null;
            return;
        }
        mCallback = new Callback() {
            @Override
            public void onStartSuccess() {
                ThreadUtils.mainThread().execute(() -> callback.onStartSuccess());
            }

            @Override
            public void onStartError(Throwable throwable) {
                ThreadUtils.mainThread().execute(() -> callback.onStartError(throwable));
            }

            @Override
            public void onActive(Channel channel) {
                ThreadUtils.mainThread().execute(() -> callback.onActive(channel));
            }

            @Override
            public void onInactive(Channel channel) {
                ThreadUtils.mainThread().execute(() -> callback.onInactive(channel));
            }

            @Override
            public void onError(Channel channel, Throwable throwable) {
                ThreadUtils.mainThread().execute(() -> callback.onError(channel, throwable));
            }

            @Override
            public void onRead(Channel channel, String message) {
                ThreadUtils.mainThread().execute(() -> callback.onRead(channel, message));
            }
        };
    }

    public interface Callback {
        void onStartSuccess();

        void onStartError(Throwable throwable);

        void onActive(Channel channel);

        void onInactive(Channel channel);

        void onError(Channel channel, Throwable throwable);

        void onRead(Channel channel, String message);
    }
}
