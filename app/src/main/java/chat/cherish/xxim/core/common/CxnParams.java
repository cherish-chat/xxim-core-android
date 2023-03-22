package chat.cherish.xxim.core.common;

public class CxnParams {
    public String platform;
    public String deviceId;
    public String deviceModel;
    public String osVersion;
    public String appVersion;
    public String language;
    public String networkUsed;
    public String ext;

    public CxnParams(String platform, String deviceId, String deviceModel, String osVersion,
                     String appVersion, String language, String networkUsed, String ext) {
        this.platform = platform;
        this.deviceId = deviceId;
        this.deviceModel = deviceModel;
        this.osVersion = osVersion;
        this.appVersion = appVersion;
        this.language = language;
        this.networkUsed = networkUsed;
        this.ext = ext;
    }
}
