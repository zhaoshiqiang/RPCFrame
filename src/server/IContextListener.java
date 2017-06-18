package server;

/**
 * 监听context创建和释放事件的接口
 * Created by zhaoshiqiang on 2017/6/18.
 */
public interface IContextListener {

    void onContextCreate(Context context);
    void onContextDestory(Context context);
}
