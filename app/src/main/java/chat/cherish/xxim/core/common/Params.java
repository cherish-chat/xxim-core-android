package chat.cherish.xxim.core.common;

public class Params {
    public String platform;
    public String deviceId;
    public String deviceModel;
    public String osVersion;
    public String appVersion;
    public String language;

    public Params(String platform, String deviceId, String deviceModel, String osVersion, String appVersion, String language) {
        this.platform = platform;
        this.deviceId = deviceId;
        this.deviceModel = deviceModel;
        this.osVersion = osVersion;
        this.appVersion = appVersion;
        this.language = language;
    }
}
