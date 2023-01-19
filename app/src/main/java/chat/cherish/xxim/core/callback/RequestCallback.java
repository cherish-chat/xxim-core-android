package chat.cherish.xxim.core.callback;

interface IRequest<T> {
    void onSuccess(T t);

    void onError(int code, String error);
}

public abstract class RequestCallback<T> implements IRequest<T> {
    @Override
    public void onSuccess(T t) {
    }

    @Override
    public void onError(int code, String error) {
    }
}
