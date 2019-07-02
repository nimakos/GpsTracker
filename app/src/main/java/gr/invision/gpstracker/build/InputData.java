package gr.invision.gpstracker.build;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.Date;

/**
 * POD object representing the parameter data
 * of the data lists WebService
 */
@XStreamAlias("InputData")
public class InputData {

    public static class CommandData {

        @XStreamAlias("MessageAppName")
        public String messageAppName = "";
        @XStreamAlias("UIAppVersion")
        public String uiAppVersion = "";
        @XStreamAlias("ServiceAppVersion")
        public String serviceAppVersion = "";
        @XStreamAlias("DeviceID")
        public String deviceId = "";
        @XStreamAlias("UILanguage")
        public String uiLanguage = "";
        @XStreamAlias("UserID")
        public int userId;
        @XStreamAlias("CommandID")
        public int commandId;
        @XStreamAlias("SubEntityID")
        public int subEntityId;
        @XStreamAlias("ShiftID")
        public int shiftId;
        @XStreamAlias("ShiftDate")
        public Date shiftDate = new Date();
        @XStreamAlias("MessageSubmitDate")
        public Date messageSubmitDate;
        @XStreamAlias("MessageID")
        public Long messageId;
        @XStreamAlias("GPSLat")
        public double gpsLat;
        @XStreamAlias("GPSLong")
        public double gpsLong;
        @XStreamAlias("ExtraData")
        public String ExtraData = "***,***@";
    }
    @XStreamAlias("CommandData")
    public CommandData commandData = new CommandData();

    public static class FunctionData {

        @XStreamAlias("FunctionParams")
        public String functionParams = "";
    }
    @XStreamAlias("FunctionData")
    public FunctionData functionData = new FunctionData();
}
