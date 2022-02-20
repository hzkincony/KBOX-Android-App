package com.kincony.KControl.net.socket;

import com.kincony.KControl.utils.LogUtils;
import com.kincony.KControl.utils.ThreadUtils;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class NettyClient {

    private boolean isStart;
    private Channel clientChannel;
    private NioEventLoopGroup workerGroup;

    public void connectServer(String host, int port) {
        if (isStart) return;

        isStart = true;
        workerGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)//保存连接
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)//连接超时时间
                .handler(new ChannelInitializer<SocketChannel>() {
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
                                        LogUtils.INSTANCE.d("NettyClient channelRead0 localAddress=" + ctx.channel().localAddress());
                                        LogUtils.INSTANCE.d("NettyClient channelRead0 remoteAddress=" + ctx.channel().remoteAddress());
                                        LogUtils.INSTANCE.d("NettyClient channelRead0 msg=" + msg);
                                        if (mCallback != null) {
                                            mCallback.onReadBefore(msg);
                                            mCallback.onRead(msg);
                                        }
                                    }

                                    @Override
                                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                        LogUtils.INSTANCE.d("NettyClient channelActive localAddress=" + ctx.channel().localAddress());
                                        LogUtils.INSTANCE.d("NettyClient channelActive remoteAddress=" + ctx.channel().remoteAddress());
                                        if (mCallback != null) mCallback.onActive();
                                        super.channelActive(ctx);
                                    }

                                    @Override
                                    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                        LogUtils.INSTANCE.d("NettyClient channelInactive localAddress=" + ctx.channel().localAddress());
                                        LogUtils.INSTANCE.d("NettyClient channelInactive remoteAddress=" + ctx.channel().remoteAddress());
                                        if (mCallback != null) mCallback.onInactive();
                                        super.channelInactive(ctx);
                                    }

                                    @Override
                                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                        LogUtils.INSTANCE.d("NettyClient exceptionCaught localAddress=" + ctx.channel().localAddress());
                                        LogUtils.INSTANCE.d("NettyClient exceptionCaught remoteAddress=" + ctx.channel().remoteAddress());
                                        LogUtils.INSTANCE.d("NettyClient exceptionCaught cause=" + cause);
                                        close();
                                        if (mCallback != null) mCallback.onError(cause);
                                        super.exceptionCaught(ctx, cause);
                                    }
                                });
                    }
                });

        bootstrap.connect(host, port).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                clientChannel = future.channel();
                if (future.isSuccess()) {
                    if (mCallback != null) mCallback.onConnectSuccess();
                    LogUtils.INSTANCE.d("NettyClient connect success " + host + ":" + port);
                } else {
                    if (mCallback != null) mCallback.onConnectError(future.cause());
                    LogUtils.INSTANCE.d("NettyClient connect error=" + future.cause());
                    close();
                }
            }
        });
    }

    public void close() {
        if (!isStart) return;
        isStart = false;
        if (clientChannel != null) clientChannel.close();
        clientChannel = null;
        workerGroup.shutdownGracefully();
        workerGroup = null;
        LogUtils.INSTANCE.d("NettyClient stop");
    }

    public void write(String message) {
        if (clientChannel != null) {
            if (mCallback != null) mCallback.onWrite(message);
            clientChannel.writeAndFlush(message);
        }
    }

    private Callback mCallback;

    public void setCallback(Callback callback) {
        if (callback == null) {
            mCallback = null;
            return;
        }
        mCallback = new Callback() {
            @Override
            public void onConnectSuccess() {
                ThreadUtils.mainThread().execute(() -> callback.onConnectSuccess());
            }

            @Override
            public void onConnectError(Throwable throwable) {
                ThreadUtils.mainThread().execute(() -> callback.onConnectError(throwable));
            }

            @Override
            public void onActive() {
                ThreadUtils.mainThread().execute(() -> callback.onActive());
            }

            @Override
            public void onInactive() {
                ThreadUtils.mainThread().execute(() -> callback.onInactive());
            }

            @Override
            public void onError(Throwable throwable) {
                ThreadUtils.mainThread().execute(() -> callback.onError(throwable));
            }

            @Override
            public void onReadBefore(String message) {
                callback.onReadBefore(message);
            }

            @Override
            public void onRead(String message) {
                ThreadUtils.mainThread().execute(() -> callback.onRead(message));
            }

            @Override
            public void onWrite(String message) {
                ThreadUtils.mainThread().execute(() -> callback.onWrite(message));
            }
        };
    }

    public interface Callback {
        void onConnectSuccess();

        void onConnectError(Throwable throwable);

        void onActive();

        void onInactive();

        void onError(Throwable throwable);

        void onReadBefore(String message);

        void onRead(String message);

        void onWrite(String message);
    }
}
