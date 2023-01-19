package chat.cherish.xxim.core.listener;

interface IConnect {
    void onConnecting();

    void onSuccess();

    void onClose(int code, String error);
}

public abstract class ConnectListener implements IConnect {

    @Override
    public void onConnecting() {
    }

    @Override
    public void onSuccess() {
    }

    @Override
    public void onClose(int code, String error) {
    }
}
