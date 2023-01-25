package chat.cherish.xxim.core.listener;

import pb.Core;

interface IReceivePush {
    void onPushMsgDataList(Core.MsgDataList msgDataList);

    void onPushNoticeData(Core.NoticeData noticeData);
}

public abstract class ReceivePushListener implements IReceivePush {
    @Override
    public void onPushMsgDataList(Core.MsgDataList msgDataList) {
    }

    @Override
    public void onPushNoticeData(Core.NoticeData noticeData) {
    }
}
