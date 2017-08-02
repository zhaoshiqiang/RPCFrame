package server;

/**
 * 监听context创建和释放事件的接口
 * Created by zhaoshiqiang on 2017/6/18.
 */
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
