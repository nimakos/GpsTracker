package gr.invision.gpstracker.build;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.Date;

@XStreamAlias("AppRequestData")
public class AppRequestData {
    @XStreamAlias("AppName")
    public String appName;
    @XStreamAlias("AppVersion")
    public String appVersion;
    @XStreamAlias("DeviceID")
    public String deviceID;
    @XStreamAlias("IPv4")
    public String IPv4;
    @XStreamAlias("IPv6")
    public String IPv6;
    @XStreamAlias("MessageID")
    public Long messageID;
    @XStreamAlias("MessageSubmitDate")
    public Date messageSubmitDate;
    @XStreamAlias("ActualData")
    public String actualData;
}
