/**
 * Copyright (c) 2007-2013, Kaazing Corporation. All rights reserved.
 */

package com.kaazing.robot.behavior;

public class ConnectTestBehavior {
    // extends BehaviorContext {
    //
    // private static final Logger logger =
    // Logger.getLogger(ConnectTestBehavior.class);
    // private static final String DEFAULT_NAME = "ConnectTestBehavior";
    //
    // private String name = DEFAULT_NAME;
    // private boolean prepared = false;
    //
    // public ConnectTestBehavior(UUID guid,
    // URI uri)
    // throws Exception {
    //
    // super(guid, uri);
    // }
    //
    // @Override
    // public String getName() {
    // return name;
    // }
    //
    // @Override
    // public void setName(String name) {
    // this.name = name;
    // }
    //
    // @Override
    // public void setUp()
    // throws Exception {
    //
    // // If already prepared, do nothing
    // if (prepared == true) {
    // return;
    // }
    //
    // Class<? extends ChannelFactory> channelFactoryClass = null;
    //
    // String className = transport.getConnectSocketFactoryClassName();
    // Class<?> connectFactoryClass = Class.forName(className);
    // channelFactoryClass = (Class<? extends ChannelFactory>)
    // connectFactoryClass;
    //
    // ChannelFactory factory = null;
    //
    // try {
    // Constructor constructor = connectFactoryClass.getConstructor(new Class[]
    // { Executor.class, Executor.class });
    //
    // Executor bossExecutor = Executors.newCachedThreadPool();
    // Executor workerExecutor = Executors.newCachedThreadPool();
    // factory = (ChannelFactory) constructor.newInstance(bossExecutor,
    // workerExecutor);
    //
    // } catch (NoSuchMethodException nsme) {
    // // Try obtaining an instance using the no-arg constructor
    // factory = channelFactoryClass.newInstance();
    // }
    //
    // setBootstrap(new ClientBootstrap(factory));
    // setTransportConnectOptions();
    //
    // if (transport.isLocalTransport()) {
    // // The 'local' transport uses in-VM pipes, and thus Netty has
    // // a special LocalAddress class to represent this "address".
    //
    // LocalAddress remoteAddr = new LocalAddress(uri.getSchemeSpecificPart());
    // getBootstrap().setOption("remoteAddress", remoteAddr);
    //
    // } else {
    // // Let the kernel pick our outgoing device and port
    // InetSocketAddress localAddr = new InetSocketAddress(0);
    // getBootstrap().setOption("localAddress", localAddr);
    //
    // InetSocketAddress remoteAddr = new InetSocketAddress(uri.getHost(),
    // uri.getPort());
    // getBootstrap().setOption("remoteAddress", remoteAddr);
    // }
    //
    // getBootstrap().setPipelineFactory(getDefaultPipelineFactory());
    // prepared = true;
    // }
    //
    // @Override
    // public void tearDown()
    // throws Exception {
    //
    // Bootstrap boot = getBootstrap();
    // if (boot != null) {
    // boot.releaseExternalResources();
    // }
    // }
    //
    // @Override
    // public ChannelFuture start()
    // throws Exception {
    //
    // started = System.currentTimeMillis();
    //
    // ClientBootstrap client = (ClientBootstrap) getBootstrap();
    // return client.connect();
    // }
}
