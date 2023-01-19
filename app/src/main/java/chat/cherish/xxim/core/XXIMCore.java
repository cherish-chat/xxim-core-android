package chat.cherish.xxim.core;

import android.text.TextUtils;
import android.util.ArrayMap;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import chat.cherish.xxim.core.callback.RequestCallback;
import chat.cherish.xxim.core.common.Params;
import chat.cherish.xxim.core.common.Protocol;
import chat.cherish.xxim.core.listener.ConnectListener;
import chat.cherish.xxim.core.listener.ReceivePushListener;
import pb.Core;

public class XXIMCore {
    private Params params;
    private int requestTimeout;
    private ConnectListener connectListener;
    private ReceivePushListener receivePushListener;

    public void init(
            Params params,
            int requestTimeout,
            ConnectListener connectListener,
            ReceivePushListener receivePushListener
    ) {
        this.params = params;
        this.requestTimeout = requestTimeout;
        this.connectListener = connectListener;
        this.receivePushListener = receivePushListener;
    }

    private WebSocketClient client;
    private Timer timer;
    private TimerTask timerTask;
    private Map<String, Object> responseMap;

    // 登录
    public void connect(
            String wsUrl,
            String token,
            String userId,
            String networkUsed
    ) {
        try {
            if (connectListener != null) {
                connectListener.onConnecting();
            }
            String str = wsUrl + Protocol.webSocket + "?";
            str += "token=" + token;
            str += "&userId=" + userId;
            str += "&networkUsed=" + networkUsed;
            str += "&deviceId=" + params.deviceId;
            str += "&platform=" + params.platform;
            str += "&deviceModel=" + params.deviceModel;
            str += "&osVersion=" + params.osVersion;
            str += "&appVersion=" + params.appVersion;
            str += "&language=" + params.language;
            client = new WebSocketClient(new URI(str)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                }

                @Override
                public void onMessage(String message) {
                    if (TextUtils.equals(message, "connected")) {
                        if (connectListener != null) {
                            connectListener.onSuccess();
                        }
                        startPing();
                    }
                }

                @Override
                public void onMessage(ByteBuffer buffer) {
                    handleMessage(buffer);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    disconnect();
                    if (connectListener != null) {
                        connectListener.onClose(code, reason);
                    }
                }

                @Override
                public void onError(Exception ex) {
                    disconnect();
                    if (connectListener != null) {
                        connectListener.onClose(CloseFrame.ABNORMAL_CLOSE, ex.getMessage());
                    }
                }
            };
            client.connect();
            responseMap = new ArrayMap<>();
        } catch (URISyntaxException e) {
            if (connectListener != null) {
                connectListener.onClose(CloseFrame.ABNORMAL_CLOSE, e.getMessage());
            }
        }
    }

    // 登出
    public void disconnect() {
        stopPing();
        if (client != null) {
            client.close();
        }
        if (responseMap != null) {
            responseMap.clear();
            responseMap = null;
        }
    }

    /// 是否登录
    public boolean isConnect() {
        return client != null && client.isClosed();
    }

    private void startPing() {
        stopPing();
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                client.sendPing();
            }
        };
        timer.schedule(timerTask, 0, 2000);
    }

    private void stopPing() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    private void handleMessage(ByteBuffer buffer) {
        try {
            Core.PushBody body = Core.PushBody.parseFrom(buffer.array());
            if (body.getEvent() == Core.PushEvent.PushMsgDataList) {
                if (receivePushListener != null) {
                    receivePushListener.onPushMsgDataList(
                            Core.MsgDataList.parseFrom(body.getData())
                    );
                }
            } else if (body.getEvent() == Core.PushEvent.PushNoticeDataList) {
                if (receivePushListener != null) {
                    receivePushListener.onPushNoticeDataList(
                            Core.NoticeDataList.parseFrom(body.getData())
                    );
                }
            } else if (body.getEvent() == Core.PushEvent.PushResponseBody) {
                Core.ResponseBody response = Core.ResponseBody.parseFrom(body.getData());
                String reqId = response.getReqId();
                Core.ResponseBody.Code code = response.getCode();
                if (response.getEvent() == Core.ActiveEvent.SyncConvSeq) {
                    Core.BatchGetConvSeqResp resp = Core.BatchGetConvSeqResp.parseFrom(
                            response.getData()
                    );
                    if (responseMap != null && responseMap.containsKey(reqId)) {
                        RequestCallback<Core.BatchGetConvSeqResp> callback =
                                (RequestCallback<Core.BatchGetConvSeqResp>) responseMap.get(reqId);
                        if (code == Core.ResponseBody.Code.Success) {
                            callback.onSuccess(resp);
                        } else {
                            callback.onError(code.getNumber(), code.name());
                        }
                        responseMap.remove(reqId);
                    }
                } else if (response.getEvent() == Core.ActiveEvent.SyncMsgList) {
                    Core.GetMsgListResp resp = Core.GetMsgListResp.parseFrom(
                            response.getData()
                    );
                    if (responseMap != null && responseMap.containsKey(reqId)) {
                        RequestCallback<Core.GetMsgListResp> callback =
                                (RequestCallback<Core.GetMsgListResp>) responseMap.get(reqId);
                        if (code == Core.ResponseBody.Code.Success) {
                            callback.onSuccess(resp);
                        } else {
                            callback.onError(code.getNumber(), code.name());
                        }
                        responseMap.remove(reqId);
                    }
                } else if (response.getEvent() == Core.ActiveEvent.GetMsgById) {
                    Core.GetMsgByIdResp resp = Core.GetMsgByIdResp.parseFrom(
                            response.getData()
                    );
                    if (responseMap != null && responseMap.containsKey(reqId)) {
                        RequestCallback<Core.GetMsgByIdResp> callback =
                                (RequestCallback<Core.GetMsgByIdResp>) responseMap.get(reqId);
                        if (code == Core.ResponseBody.Code.Success) {
                            callback.onSuccess(resp);
                        } else {
                            callback.onError(code.getNumber(), code.name());
                        }
                        responseMap.remove(reqId);
                    }
                } else if (response.getEvent() == Core.ActiveEvent.SendMsgList) {
                    Core.SendMsgListResp resp = Core.SendMsgListResp.parseFrom(
                            response.getData()
                    );
                    if (responseMap != null && responseMap.containsKey(reqId)) {
                        RequestCallback<Core.SendMsgListResp> callback =
                                (RequestCallback<Core.SendMsgListResp>) responseMap.get(reqId);
                        if (code == Core.ResponseBody.Code.Success) {
                            callback.onSuccess(resp);
                        } else {
                            callback.onError(code.getNumber(), code.name());
                        }
                        responseMap.remove(reqId);
                    }
                } else if (response.getEvent() == Core.ActiveEvent.AckNotice) {
                    Core.AckNoticeDataResp resp = Core.AckNoticeDataResp.parseFrom(
                            response.getData()
                    );
                    if (responseMap != null && responseMap.containsKey(reqId)) {
                        RequestCallback<Core.AckNoticeDataResp> callback =
                                (RequestCallback<Core.AckNoticeDataResp>) responseMap.get(reqId);
                        if (code == Core.ResponseBody.Code.Success) {
                            callback.onSuccess(resp);
                        } else {
                            callback.onError(code.getNumber(), code.name());
                        }
                        responseMap.remove(reqId);
                    }
                } else if (response.getEvent() == Core.ActiveEvent.CustomRequest) {
                    if (responseMap != null && responseMap.containsKey(reqId)) {
                        RequestCallback<ByteString> callback =
                                (RequestCallback<ByteString>) responseMap.get(reqId);
                        if (code == Core.ResponseBody.Code.Success) {
                            callback.onSuccess(response.getData());
                        } else {
                            callback.onError(code.getNumber(), code.name());
                        }
                        responseMap.remove(reqId);
                    }
                }
            }
        } catch (InvalidProtocolBufferException ignored) {
        }
    }

    // 批量获取会话序列
    public void batchGetConvSeq(
            String reqId,
            Core.BatchGetConvSeqReq req,
            RequestCallback<Core.BatchGetConvSeqResp> callback
    ) {
        if (responseMap != null) {
            responseMap.put(reqId, callback);
        }
        Core.RequestBody request = Core.RequestBody.newBuilder()
                .setEvent(Core.ActiveEvent.SyncConvSeq)
                .setReqId(reqId)
                .setData(req.toByteString())
                .build();
        if (client != null) {
            client.send(request.toByteArray());
        }
        handleTimeout(reqId, callback);
    }

    // 批量获取消息列表-会话ID
    public void batchGetMsgListByConvId(
            String reqId,
            Core.BatchGetMsgListByConvIdReq req,
            RequestCallback<Core.GetMsgListResp> callback
    ) {
        if (responseMap != null) {
            responseMap.put(reqId, callback);
        }
        Core.RequestBody request = Core.RequestBody.newBuilder()
                .setEvent(Core.ActiveEvent.SyncMsgList)
                .setReqId(reqId)
                .setData(req.toByteString())
                .build();
        if (client != null) {
            client.send(request.toByteArray());
        }
        handleTimeout(reqId, callback);
    }

    // 获取消息-消息ID
    public void getMsgById(
            String reqId,
            Core.GetMsgByIdReq req,
            RequestCallback<Core.GetMsgByIdResp> callback
    ) {
        if (responseMap != null) {
            responseMap.put(reqId, callback);
        }
        Core.RequestBody request = Core.RequestBody.newBuilder()
                .setEvent(Core.ActiveEvent.GetMsgById)
                .setReqId(reqId)
                .setData(req.toByteString())
                .build();
        if (client != null) {
            client.send(request.toByteArray());
        }
        handleTimeout(reqId, callback);
    }

    // 发送消息列表
    public void sendMsgList(
            String reqId,
            Core.SendMsgListReq req,
            RequestCallback<Core.SendMsgListResp> callback
    ) {
        if (responseMap != null) {
            responseMap.put(reqId, callback);
        }
        Core.RequestBody request = Core.RequestBody.newBuilder()
                .setEvent(Core.ActiveEvent.SendMsgList)
                .setReqId(reqId)
                .setData(req.toByteString())
                .build();
        if (client != null) {
            client.send(request.toByteArray());
        }
        handleTimeout(reqId, callback);
    }

    // 确认消费通知
    public void ackNoticeData(
            String reqId,
            Core.AckNoticeDataReq req,
            RequestCallback<Core.AckNoticeDataResp> callback
    ) {
        if (responseMap != null) {
            responseMap.put(reqId, callback);
        }
        Core.RequestBody request = Core.RequestBody.newBuilder()
                .setEvent(Core.ActiveEvent.AckNotice)
                .setReqId(reqId)
                .setData(req.toByteString())
                .build();
        if (client != null) {
            client.send(request.toByteArray());
        }
        handleTimeout(reqId, callback);
    }

    // 自定义请求
    public void customRequest(
            String reqId,
            ByteString byteString,
            RequestCallback<ByteString> callback
    ) {
        if (responseMap != null) {
            responseMap.put(reqId, callback);
        }
        Core.RequestBody request = Core.RequestBody.newBuilder()
                .setEvent(Core.ActiveEvent.CustomRequest)
                .setReqId(reqId)
                .setData(byteString)
                .build();
        if (client != null) {
            client.send(request.toByteArray());
        }
        handleTimeout(reqId, callback);
    }

    private void handleTimeout(String reqId, RequestCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(requestTimeout);
                    if (responseMap != null && responseMap.containsKey(reqId)) {
                        callback.onError(
                                Core.ResponseBody.Code.UnknownError_VALUE,
                                Core.ResponseBody.Code.UnknownError.name()
                        );
                        responseMap.remove(reqId);
                    }
                } catch (InterruptedException ignored) {
                }
            }
        }).start();
    }
}
