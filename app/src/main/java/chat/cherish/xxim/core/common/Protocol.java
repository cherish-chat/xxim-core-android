package chat.cherish.xxim.core.common;

public class Protocol {
    public static String webSocket = "/ws"; // 建立连接
    public static String setCxnParams = "/v1/conn/white/setCxnParams"; // 设置连接参数
    public static String setUserParams = "/v1/conn/white/setUserParams"; // 设置用户参数
    public static String batchGetConvSeq = "/v1/msg/batchGetConvSeq"; // 批量获取会话序列
    public static String batchGetMsgListByConvId = "/v1/msg/batchGetMsgListByConvId"; // 批量获取消息列表
    public static String getMsgById = "/v1/msg/getMsgById"; // 获取消息
    public static String sendMsgList = "/v1/msg/sendMsgList"; // 发送消息列表
    public static String sendReadMsg = "/v1/msg/sendReadMsg"; // 发送已读消息
    public static String sendEditMsg = "/v1/msg/sendEditMsg"; // 发送编辑消息
    public static String ackNoticeData = "/v1/notice/ackNoticeData"; // 确认消费通知
}
