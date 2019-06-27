package gr.vaggelis.myapplication.build;


import android.os.Environment;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.converters.basic.BooleanConverter;
import com.thoughtworks.xstream.converters.basic.DateConverter;
import com.thoughtworks.xstream.converters.extended.EncodedByteArrayConverter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import gr.vaggelis.myapplication.BuildConfig;

public class Constructor {
    private static final String DATE_FMT = "yyyy-MM-dd'T'HH:mm:ss";
    private final static String ROOT = Environment.getExternalStorageDirectory().getPath() + "/Android/data/!gr.invision.iservice";
    private final static String DATAFILES = ROOT + "/DataFiles";
    private final static String DATAFILES_OUT = DATAFILES + "/Out";


    /**
     * Main builder function constructs the request data for the request
     * with given command id and any pod object for its additional data
     *
     * @param commandId    command id
     * @param functionData The function data object
     * @param <T>          generic object
     * @return String
     */
    public <T> String createRequestData(int commandId, T functionData) {

        return constructRequestData(commandId, functionData);
    }

    private <T> String constructRequestData(int commandId, T functionData) {

        AppRequestData appRequestData = new AppRequestData();

        appRequestData.appName = "";
        appRequestData.appVersion = BuildConfig.VERSION_NAME;
        appRequestData.deviceID = "";
        appRequestData.IPv4 = "";
        appRequestData.IPv6 = "";
        appRequestData.messageID = 0L;
        appRequestData.messageSubmitDate = new Date();

        appRequestData.actualData = constructInputData(commandId, functionData);

        return serializeFromObjectToString(appRequestData);
    }

    private <T> String constructInputData(int commandId, T functionData) {
        InputData inputData = new InputData();

        inputData.commandData.uiLanguage = "";
        inputData.commandData.deviceId = "";
        inputData.commandData.userId = 0;
        inputData.commandData.shiftId = 0;
        inputData.commandData.shiftDate = new Date();
        inputData.commandData.subEntityId = 0;
        inputData.commandData.messageSubmitDate = new Date();
        inputData.commandData.messageId = 0L;
        inputData.commandData.messageAppName = "";
        inputData.commandData.uiAppVersion = "" + BuildConfig.VERSION_NAME;
        inputData.commandData.serviceAppVersion = "";
        inputData.commandData.gpsLong = 0L;
        inputData.commandData.gpsLat = 0L;
        inputData.commandData.commandId = commandId;

        inputData.functionData.functionParams = constructFunctionData(functionData);

        return serializeFromObjectToString(inputData);
    }

    /**
     * Serializes the request file rf to String
     *
     * @param rf request file
     * @return xml string
     */
    private <T> String constructFunctionData(T rf) {

        XStream xstream = new XStream();
        xstream.autodetectAnnotations(true);
        xstream.registerConverter(new DateConverter(DATE_FMT, null));
        return xstream.toXML(rf);
    }

    /**
     * Generic method to auto serialize an object
     *
     * @param object The object to be serializable
     * @param <T>    Generic object
     * @return The string value from the serialization
     */
    private <T> String serializeFromObjectToString(T object) {
        XStream xstream = new XStream();
        xstream.autodetectAnnotations(true);
        xstream.registerConverter(new DateConverter(DATE_FMT, null));
        xstream.registerConverter((SingleValueConverter) new EncodedByteArrayConverter());
        xstream.registerConverter(new BooleanConverter("1", "0", false));
        xstream.ignoreUnknownElements();

        return xstream.toXML(object);
    }

    /**
     * Utility to write bytes to file
     *
     * @param path     path
     * @param filename filename
     * @param data     an array of bytes
     */
    public void writeFile(String path, String filename, byte[] data) {
        try {
            File f = new File(path, filename);
            OutputStream out;
            out = new BufferedOutputStream(new FileOutputStream(f));
            out.write(data);
            out.close();
        } catch (IOException ignore) {
        }
    }

    /**
     * Create data files out folder
     */
    public static void createDatafilesOut() {
        File f;
        f = new File(DATAFILES_OUT);
        if (!f.exists())
            f.mkdirs();
    }

    /**
     * Get data files out folder
     *
     * @return path
     */
    public String getDatafilesOut() {
        return DATAFILES_OUT;
    }
}
