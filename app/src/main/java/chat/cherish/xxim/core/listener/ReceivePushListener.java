package chat.cherish.xxim.core.listener;

import pb.Core;

interface IReceivePush {
    void onPushMsgDataList(Core.MsgDataList msgDataList);

    void onPushNoticeDataList(Core.NoticeDataList noticeDataList);
}

public abstract class ReceivePushListener implements IReceivePush {
    @Override
    public void onPushMsgDataList(Core.MsgDataList msgDataList) {
    }

    @Override
    public void onPushNoticeDataList(Core.NoticeDataList noticeDataList) {
    }
}
