# RPCFrame
## 简介

SimpleNet提供了一个RPC网络服务的框架，方便搭建基于消息(message)的网络服务。这个网络框架的基本特性是：
- RPC目前仅仅支持tcp/ip，不支持udp。
- RPC适合“请求-应答”的服务模式，并且“请求”和“应答”的数据量都不大。与之相对的例如ftp服务中的文件传输，是不适合用RPC实现的，可能额外的开销会大一些。
- 基于mina实现，网络i/o部分完全使用的non-blocking模式，具有较好的扩展性，能够满足比较高的连接数，请求数的要求。
- 用context实现客户端识别与客户端退出信息监听。
- 客户端对连接状态灵活的监听（通过IHandlerListener实现）。
- 客户端支持远程接口的异步调用。

## 快速上手

### hello
我们从设计一个最简单的服务"Hello"开始，这个服务目前只有一个要求：我们告诉服务器一个名字，服务器返回"Hello ${name}!"。我们按照接口定义，服务器实现和客户端程序实现三个步骤来实现并且调用这个服务。

#### 接口定义 
我们先定义接口如下：

```
    public interface IHello {
        
    /**
     *
     * @param name
     * @return "Hello ${name}!".
     * @throws RPCException
     */
    String hello(String name) throws RPCException;
    }

```

RPC中服务器和客户端之间协议的确定就通过接口来完成：服务器实现这个接口并且提供服务，客户端通过这个接口来调用服务器。
对于上面的接口，有几个地方需要注意：

数据类型：  
这个接口和一般的接口看起来没有任何区别，但是我们一定要记住，这个接口是需要被远程调用的，其中参数和返回值都是需要在网络上传输的，这个就对实际可以支持的参数类型有了限制。目前我们能够支持的参数类型包括：
- 所有原始的数据类型；
- String类型；
- 所有的IWritable的类型；
- 前面三种类型的数组类型。  
除此以外的其他类型是无法传输的。  

RPCException：  
接口中的所有方法必须声明抛出RPCException。rpc接口由于实际需要经过网络传输等等过程，可能会在传输过程中遇到各种错误，例如网络错误，这些错误都会作为RPCException抛出；接口中声明抛出RPCException，就是要求客户端调用者必须处理RPC调用过程中的这些错误。

#### 服务器实现
下面我们来实现这个接口：

```
public class HelloImpl implements IHello {
    @Override
    public String hello(String name) {
        Context context = ThreadState.getContext();
        if (context != null){
            context.put("name",name);
        }
        return "hello " + name +" !";
    }
}
```
上面的类实现了IHello的接口，我们在实现中没有声明抛出"RPCException"，这个是允许的。
下面，我们编写一个包含main()方法的主程序，来创建并且运行rpc服务器：

```
public class PRCFrameTestServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length < 1) {
            System.out.println("args: port");
            return;
        }
        int port = Integer.parseInt(args[0]);
        Server server =new Server(port,new HelloImpl());
        server.start();
        System.out.println("HelloServer started on port " + port);
        server.join();

    }
}
```
我们注意看一下HelloMain的实现，有如下几点需要解释的地方：  
- 我们用RPC.createServer(port,impl)创建了一个rpc服务器，这个新创建的服务器只有一个线程来进行select所有的连接，并且完成连接上的I/O操作；另外有一个线程来处理所有的rpc请求。RPC.createServer()还有很多其他的实现，可以传入更多的控制参数，从而控制线程数目。我们后面会详细介绍这些参数都是什么意思，应该如何选择你的参数以满足应用的性能要求。
- 我们在server.start()之后调用了server.join()，server.join()的作用是让主线程等待服务器线程的退出。在rpc服务器中，所有的线程都是daemon线程，我们知道如果一个虚拟机中只剩下daemon线程的时候，虚拟机就会退出；所以，如果没有这行server.join()，随着main()函数执行完成，主线程退出，虚拟机也就结束了，服务就立即终止了。

#### 客户端实现
我们来编写一个客户端程序，从命令行上接受服务器的地址以及名字，并且答应服务器的返回值，代码如下：

```
public class RPCFrameTestClient {

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("args: hostname port name");
            return;
        }
        InetSocketAddress addr = new InetSocketAddress(args[0], Integer.parseInt(args[1]));
        IHello hello = new RPCClient<IHello>(addr, IHello.class).getProxy();
        Object result = hello.hello(args[2]);
        System.out.println(result);
    }
}
```
这个代码很简单，我们通过RPC.getProxy(addr,IHello.class)得到了一个IHello的实现，并且直接调用了这个方法。 在这个代码中，需要注意如下几点：
- 我们没有指定诸如timeout这样的参数，在没有指定的情况下，使用的是默认的参数，调用的timeout是30秒。当然，RPC.getProxy()也支持传入callTimeout参数，如果我们不希望有timeout，那么应该传入Long.MAX_VALUE，让接口等待返回值直到海枯石烂。
- 我们用RPC.closeProxy()方法关闭了获得的proxy对象，同时也关闭了下面的网络连接。

运行这个方法，去连一下上面的服务器试试，看看打印了什么。可能大家会注意到请求过程中服务器并没有打印连接日志，如果想开启这个日志，尝试在RPC.createServer()给verbose参数传入true。    
好，到这里为止，我们实现了一个服务器，并且用客户端去调用了它。

### 客户端的识别

我们现在发现，这个服务器有一点不好：这个傻东西记不住我的名字，我希望的是，我第一次和它说hello的时候告诉他我的名字，下次我hello的时候，即使不说名字，它也应该知道我的名字并且依旧返回"Hello ${name}!"。好，让我们来动手做一下修改。

接口定义
我们首先在接口IHello中增加一个不带名字的hello方法，如下：

```
/**
*  不带名字的hello接口，应该返回"Hello ${name}!"，其中name是上次hello的时候记住的.
* @return
* @throws RPCException
*/
String hello() throws RPCException;
```
#### 接口实现 
下面，我们需要做如下的实现：

修改原有的hello(name)方法，让服务器记住我的名字；
实现新的hello()方法，返回记住的名字；
我们面临的问题是，我们应该如何记住客户端传递过来的名字？   
**错误的办法**：在HelloImpl中增加一个name域，然后把名字存在这里，如下：

```
private String name;
public String hello(String name) {
    this.name = name;
    return "Hello " + name + "!";
}
```
这个办法是错误的，原因在于：服务器端的同一个HelloImpl对象是被所有的客户端调用的，所以这里的name可能一会儿是客户端a修改, 一会儿又被客户端b修改，服务器就变成了一个只能认识一个人的白痴了。
RPC接口的实现对象会被多个客户端调用，也因此可能被多个线程调用，这就要求这个对象在实现的时候遵循如下的要求：   
- 每个rpc方法都必须是线程安全的；   

如果大家做过http servlet的开发，就很容易明白这个道理，事实上HttpServlet#doGet()方法也有同样的要求。   
**正确的办法**：使用Context来记录请求的状态。    
为了便于服务器记录客户端的状态，每个客户端在服务器都会拥有一个Context容器。这个容器在客户端连接创建的时候创建，当客户端退出的时候销毁。我们在服务器端的方法中可以通过如下的代码获得当前连接对应的容器：

```
Context context = ThreadState.getContext();
```
正如RPCThreadState这个类名传达的意思那样，这个方法背后的实现实际上是获取当前线程使用的Context，而RPC框架代码在所有的rpc方法开始以前，设置了当前线程的Context为当前连接的Context，从而保证了这个方法可以获得当前连接对应的Context。通过RPCThreadState我们还可以获得当前的网络连接，这个对于想在方法中获得连接的一些详细信息（例如客户端地址）是非常有用的。    
有了Context，我们想记住客户端的名字就变得简单了，代码如下：

```
public class HelloImpl implements IHello {
    @Override
    public String hello(String name) {
        Context context = ThreadState.getContext();
        if (context != null){
            context.put("name",name);
        }
        return "hello " + name +" !";
    }

    @Override
    public String hello() {
        Context context = ThreadState.getContext();
        if (context != null){
            return "hello" + context.get("name");
        }else {
            return "Hello, may I known your name?";
        }
    }

}
```
我们可以修改一下客户端的代码，在hello(name)以后再hello一次，看看服务器返回点什么？

### 客户端退出信息监听
好，服务器已经可以记住我了，这是一个好消息。现在服务器希望增加一个新能力，就是想知道我在这里逗留了多长时间。这实际上就是要求服务器能够在我来的时候以及我走的时候都得到通知，这个需求可以通过实现下面的回调接口完成：

```
public interface IContextListener {
    /**
     * Context被创建事件
     * @param context
     */
    void onContextCreate(Context context);

    /**
     * Context被销毁事件
     * @param context
     */
    void onContextDestory(Context context);
}
```
我们可以让HelloImpl实现这两个接口，从而保证HelloImpl能够得到通知，代码如下：

```
public class HelloImpl implements IHello , IContextListener{
    ...

    @Override
    public void onContextCreate(Context context) {
        context.put("_start", System.currentTimeMillis());
    }

    @Override
    public void onContextDestory(Context context) {
        long start = (Long) context.get("_start");
         long end = System.currentTimeMillis();
         String name = (String) context.get("name");
         if (name == null) {
             System.out.println("unknown user stayed here for " + (end-start) + "ms.");
         } else {
             System.out.println(name + " stayed here for " + (end-start) + "ms.");
         }
    }
}
```
好，再用客户端连一下服务器，看看服务器是不是可以在你离开的时候知道你的逗留时间

### 高级功能

#### 客户端连接管理
在前面的说明中，我们一直都回避了如下的问题：一个客户端会拥有多少个到服务器的并发连接。 为了解释这个问题，我们需要区分两个概念：Client和Connection。这两个概念之间的关系如下
- Client是一个逻辑单位，每个我们通过RPC.getProxy()得到的对象都对应于一个唯一的Client；而Connection是一个物理单位，对应于到服务器的一条连接。
- 一个Client可能会对应于一个或者多个Connection。
- Connection在服务器端的对应就是Session，而Client在服务器端的对应是Context；**服务器端的Context和Client一一对应，而和Connection没有对应关系**。
- Client随着其中的第一个Connection的打开而打开，随着其中最后一个Connection的关闭而关闭。

在前面的例子中，我们都没有指定Client会使用的Connection的数目，默认的情况下就仅仅会打开一条连接。同时，RPC类中也提供了指定Connection数目的proxy创建方法，如下

```
public RPCClient(InetSocketAddress addr, Class<T> cls, int connectionCount)；
```
Client的请求并不会独占连接，所有的请求在Client上都是异步发送，异步接收的。也就是说，当你做一个rpc调用的时候，请求仅仅是被加入到了一个Connection的请求队列中，在连接可写的时候发送到服务器，请求的返回也是异步的，请求并不会独占连接。
一般情况下，在请求量不是很大的时候，一个Connection就能达到比较高的并发度了；当然，当并发度很高的时候，提高Connection的数目会有一定用处（这个没有仔细测试过）。

#### 服务器线程数

服务器的线程数设置是一个直接影响服务器并发能力的关键参数。在前面的例子中，在没有设置的情况下，我们都使用了默认的线程数设置（一般是1）；现在，我们来详细讨论一下服务器的线程模式和线程数设置。

在RPC服务器中，使用了如下的线程：

- 使用了一个独立的线程来在server socket上accept新的连接，这个线程的名字是"socketAcceptor-..."。
- 所有的连接被均匀分配到n个selector上，每个selector对应于一个独立线程，这个线程负责处理这个selector中连接的所有I/O。连接上的I/O包括数据的读取，从字节数据到rpc参数的解析（decode/encode），以及调用结果的输出。如果打印服务器的stack trace，你会看到一些名字是"socketAcceptorIoProcessor-..."的线程，这些线程中有一个是前面accept连接的，其他的就是这n个线程。这里线程的数目n我们用一个参数ioWorkerNumber来记录。
- 处理请求的线程池。所有的rpc请求在参数解析完成以后，会被加入到一个线程池中等待处理。这个线程池中的线程的名字是"processThread-..."，我们可以在stacktrace中看到。这里线程池的大小我们用processorNumber来记录。这个线程池有一个请求队列，请求队列的长度由maxQueueSize控制，当maxQueueSize=-1时，使用LinkedBlockingDeque作为请求队列，没有限制请求队列的长度。当当maxQueueSize>0时，使用ArrayBlockingQueue作为请求队列，长度为maxQueueSize

很显然，ioWorkerNumber决定了服务器的并发i/o能力，而processorNumber决定了服务器的并发请求处理能力。从而我们可以大致按照下面的办法来设置这两个数字：

- 如果服务器的并发连接数很大，可以考虑增大ioWorkerNumber，但是这个数目最终不应该很大，例如不会超过10。
- 如果并发请求数比较大，或者每个请求消耗的时间比较长（这个实际上也是最终导致并发请求数比较大），或者我们实际观测到线程池的请求队列中一直存在请求，应该增大processorNumber。

ioWorkerNumber和processorNumber参数在RPC.getServer()方法中都可以设置。
#### 异步rpc调用
除了上面的简单的rpc调用办法以外，我们也可以通过RPCClient的invoke()方法来进行异步rpc调用，例如

```
RPCClient client = new RPCClient(addr);
client.open();
Method helloMethod = IHello.class.getMethod("hello", String.class);
Future future = client.invoke(helloMethod, new Object[]{"zhaoshiqiang"});
future.get();
```

异步rpc调用对于需要同时调用多个服务器上的方法的应用会非常有效，因为客户端可以先向所有的服务器发送请求，然后顺序等待服务器返回。

#### 请求超时控制
在创建RPC proxy的时候，我们可以指定calltimeout，这个值表示如果在这段时间内请求没有返回，就会抛出RPCTimeoutException。如果不指定这个值，默认的timeout时间是Long.MAX_VALUE，也就是没有timeout。当然，在异步调用的接口中，还可以为请求指定timeout，这个对于处理一些timeout和基本设置不一样的请求会有用。
