package auto.script.shizuku;

// 这个接口定义了我们的高权限服务能做什么
interface IAssistService {

    /**
     * 工作流 1: 打开一个 App
     * @param packageName 要打开的应用包名
     */
    void openApp(String packageName);

    void returnApp();

    /**
     * 工作流 2 & 3: 获取当前界面的 XML 布局
     * @return 包含界面布局的 XML 字符串
     */
    String getUiXml(String filename);

    /**
     * 工作流 4: 模拟点击
     * @param x 屏幕 x 坐标
     * @param y 屏幕 y 坐标
     */
    void tap(int x, int y);

    /**
     * 工作流 5: 模拟滑动
     */
    void swipe(int x1, int y1, int x2, int y2, int duration);

    /**
     * 工作流 6: 模拟返回
     */
    void back();

    /**
     * 附加功能: 停止 UserService 进程
     * 在 AIDL 中，"destroy" 有一个特殊的含义 (IBinder.FIRST_CALL_TRANSACTION + 1)
     * 但 Shizuku 建议我们自己实现一个退出方法
     */
    void exit();

    void getXMLAndTap(String filename, String targetResourceId, String targetClass, String targetText, String contentDesc);

    Rect findNodeBounds(String xmlString, String resourceId, String className, String text, String contentDesc);

    Rect getBoundsByAccessibilityNodeInfo(in AccessibilityNodeInfo node);

    Rect findNodeBoundsByResourceId(String xmlString, String targetId);

    Rect parseBounds(String boundsString);

    void parseRemainingTimeAndSave(String xmlString);

}