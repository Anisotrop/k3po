/**
 * Copyright (c) 2007-2013, Kaazing Corporation. All rights reserved.
 */

package com.kaazing.netty.test;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.ChildChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.socket.DatagramChannel;
import org.jboss.netty.channel.socket.ServerSocketChannel;
import org.jboss.netty.channel.socket.SocketChannel;

import com.kaazing.netty.channel.ChannelAddress;

public class RobotScriptChannelRecorder implements ChannelDownstreamHandler, ChannelUpstreamHandler {

    private List<String> script;

    public RobotScriptChannelRecorder() {
        script = Collections.synchronizedList(new ArrayList<String>());
    }

    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {

        if (e instanceof ChildChannelStateEvent) {
            ChildChannelStateEvent event = (ChildChannelStateEvent) e;
            Channel child = event.getChildChannel();
            if (child.isOpen()) {
                script.add("child open");
            } else {
                script.add("child closed");
            }
        } else if (e instanceof ChannelStateEvent) {
            ChannelStateEvent event = (ChannelStateEvent) e;
            Object value = event.getValue();
            switch (event.getState()) {
            case OPEN:
                if (Boolean.TRUE.equals(value)) {
                    script.add("open");
                } else {
                    script.add("closed");
                }
                break;
            case BOUND:
                if (value != null) {
                    script.add("bound");
                } else {
                    script.add("unbound");
                }
                break;
            case CONNECTED:
                if (value != null) {
                    script.add("connected");
                } else {
                    script.add("disconnected");
                }
                break;
            }
        } else if (e instanceof MessageEvent) {
            MessageEvent event = (MessageEvent) e;
            ChannelBuffer message = (ChannelBuffer) event.getMessage();

            script.add(String.format("read %s", ChannelBuffers.hexDump(message)));
        }

        ctx.sendUpstream(e);
    }

    @Override
    public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {

        if (e instanceof ChannelStateEvent) {
            ChannelStateEvent event = (ChannelStateEvent) e;
            Object value = event.getValue();
            switch (event.getState()) {
            case OPEN:
                if (Boolean.TRUE.equals(value)) {
                    script.add("open");
                } else {
                    script.add("close");
                }
                break;
            case BOUND:
                if (value != null) {
                    URI location = toLocationURI(e.getChannel(), value);
                    script.add(String.format("bind %s", location));
                } else {
                    script.add("unbind");
                }
                break;
            case CONNECTED:
                if (value != null) {
                    URI location = toLocationURI(e.getChannel(), value);
                    script.add(String.format("connect %s", location));
                } else {
                    script.add("disconnect");
                }
                break;
            }
        } else if (e instanceof MessageEvent) {
            MessageEvent event = (MessageEvent) e;
            ChannelBuffer message = (ChannelBuffer) event.getMessage();

            script.add(String.format("write %s", ChannelBuffers.hexDump(message)));
        }

        ctx.sendDownstream(e);
    }

    private URI toLocationURI(Channel channel, Object value) {
        if (value instanceof ChannelAddress) {
            ChannelAddress remoteAddress = (ChannelAddress) value;
            return remoteAddress.getLocation();
        } else if (value instanceof InetSocketAddress) {
            InetSocketAddress remoteAddress = (InetSocketAddress) value;
            if (channel instanceof SocketChannel || channel instanceof ServerSocketChannel) {
                return URI.create(String.format("tcp://%s:%s", remoteAddress.getAddress().getHostAddress(),
                        remoteAddress.getPort()));
            } else if (channel instanceof DatagramChannel) {
                return URI.create(String.format("udp://%s:%s", remoteAddress.getAddress().getHostAddress(),
                        remoteAddress.getPort()));
            }
        }

        throw new IllegalArgumentException("Unrecognized value: " + value);
    }

    public List<String> getScript() {
        return script;
    }
}
