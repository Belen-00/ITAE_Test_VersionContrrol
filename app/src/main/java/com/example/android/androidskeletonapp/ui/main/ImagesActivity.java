package com.example.android.androidskeletonapp.ui.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

import com.example.android.androidskeletonapp.R;
import com.example.android.androidskeletonapp.data.Sdk;
import com.example.android.androidskeletonapp.ui.base.ListActivity;
import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.hisp.dhis.android.core.arch.helpers.DateUtils;
import org.hisp.dhis.android.core.arch.helpers.FileResizerHelper;
import org.hisp.dhis.android.core.arch.helpers.FileResourceDirectoryHelper;
import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.dataset.DataSetInstance;
import org.hisp.dhis.android.core.dataset.DataSetInstanceCollectionRepository;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentCreateProjection;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventCreateProjection;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitCollectionRepository;
import org.hisp.dhis.android.core.program.ProgramStageDataElement;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceCreateProjection;
import org.hisp.dhis.android.core.user.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import io.reactivex.Single;
import sun.misc.BASE64Decoder;


public class ImagesActivity extends ListActivity implements AdapterView.OnItemSelectedListener{

    private boolean serverUp = false;
    private Toolbar toolbar;
    int port = 5000;
    WifiManager wifiManager = null;
    String ipS;
    TextView tv1 = null;
    long check;
    DatagramSocket socket = null;
    //String serverUp = "false";

    //DHIS Organizational units
    private String[] unitUids = null;
    private String unitUid = "e4lf0LXlPgL";
    //DHIS2 Data elements IDs
    String typeDelem = "T7fT5GkL2vl";
    String devIdDelem = "gO9YcByzYJI";
    String dateDelem = "BdaDfOBsUqu";
    String sessionDelem = "biq1EWTxQoQ";
    String totDelem = "ORtUYrVgg47";
    String image0Delem = "cuf2mWJllQW";
    String[] metaDelem = new String[]{
            "UcJlS5JX0Ny", "jhZpu3Z05RK","gKq8xWWUbeP", "c5zi5o2L8ty"};
    String imgIDsDelem = "vnE3kwSZvxz";

    //DHIS2 program Images Capture
    String progCapture = "hBYC9yc44iC";
        //DHIS2 TrackerElements
        String pidAtri = "kbW6APe33De";
        String locadAtri = "Zc3q4Yw7RMG";

    //DHIS2 program Images Sync
    String progSync = "f0Z8L1MW3Jv";
        //DHIS2 TrackerElements
        String devAtri = "XNnOnFWklVT";
        String typeAtri = "UK3Djn9uxXE";
        String dateAtri = "S7jyFxkIw1p";

    //User info
    String uss = null;
    String password = null;
   // String orgUnit = null;

    private View.OnClickListener serverListener = new View.OnClickListener() {
        public void onClick(View v) {
            //SharedPreferences sharedpreferences = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
            //serverUp = sharedpreferences.getString("up", "");
            if(serverUp){
                //Log.d("SERVER UP2", serverUp.);
                stopServer();
                serverUp = false;
            }else if(!serverUp){
                //Log.d("SERVER UP3", serverUp);
                startServer(port);
                serverUp = true;
            }
        }
    };

    public static Intent getIntent(Context context) {
        return new Intent(context,ImagesActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUp(R.layout.images_activity, R.id.imagesToolbar, R.id.imagesProgress);

        port = 5000;
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        long ip = wifiInfo.getIpAddress();
        ipS = longToIp(ip);

        tv1 = findViewById(R.id.imagesProgress);
        Button serverButton = findViewById(R.id.serverButton);
        serverButton.setOnClickListener(serverListener);

        if(socket == null){
            Log.d("SOCKET", "null");
            startUDP();
        }

        SharedPreferences sharedpreferences = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        uss = sharedpreferences.getString("user", "");
        password = sharedpreferences.getString("pass", "");

       /* Log.d("SERVER UP", String.valueOf(serverUp));
        if(mHttpServer != null){
            Button b1 = findViewById(R.id.serverButton);
            b1.setText(R.string.stop_server);
            tv1.append("\n"+ "Started. IP = "+ipS);
        }*/
        /*serverUp = sharedpreferences.getString("up", "");
        Log.d("SERVER UP", serverUp);
        if(serverUp.equals("true")){
            tv1.append("\n"+ "Started. IP = "+ipS);
            Button b1 = findViewById(R.id.serverButton);
            b1.setText(R.string.stop_server);
        }else if(serverUp.equals("false")){
            Button b1 = findViewById(R.id.serverButton);
            b1.setText(R.string.start_server);
        }*/

        Spinner orgunits = findViewById(R.id.orgunits);
        orgunits.setOnItemSelectedListener(this);

        List<OrganisationUnit> unitsList = Sdk.d2().organisationUnitModule().organisationUnits().byLevel().eq(3).blockingGet();
        String[] unitNames = new String[unitsList.size()];
        unitUids = new String[unitsList.size()];
        for(int i = 0; i<unitsList.size(); i++){
            String name = unitsList.get(i).name();
            String uid = unitsList.get(i).uid();
            unitNames[i]=name;
            unitUids[i] = uid;
        }
        ArrayAdapter units = new ArrayAdapter(this,android.R.layout.simple_spinner_item,unitNames);
        units.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        orgunits.setAdapter(units);
    }

    private void sendResponse(HttpExchange httpExchange, String response) throws IOException {
        httpExchange.sendResponseHeaders(200, Long.parseLong(String.valueOf(response.length())));
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private HttpServer mHttpServer = null;
    private void startServer(int port){
        try {
            mHttpServer = HttpServer.create(new InetSocketAddress(port),0);
            mHttpServer.createContext("/", rootHandler);
            HttpContext pingContext = mHttpServer.createContext("/ping", pingHandler);
            HttpContext syncContext = mHttpServer.createContext("/sync", syncHandler);
            HttpContext imagesContext = mHttpServer.createContext("/images", imagesHandler);
            mHttpServer.createContext("/check", checkHandler);
            mHttpServer.createContext("/end", endHandler);

            pingContext.setAuthenticator(new BasicAuthenticator("get") {
                @Override
                public boolean checkCredentials(String user, String pwd) {
                    return user.equals(uss) && pwd.equals(password);
                }
            });
            syncContext.setAuthenticator(new BasicAuthenticator("get") {
                @Override
                public boolean checkCredentials(String user, String pwd) {
                    return user.equals(uss) && pwd.equals(password);
                }
            });
            imagesContext.setAuthenticator(new BasicAuthenticator("get") {
                @Override
                public boolean checkCredentials(String user, String pwd) {
                    return user.equals(uss) && pwd.equals(password);
                }
            });

            mHttpServer.setExecutor(null); // creates a default executor
            mHttpServer.start();

            Button b1 = findViewById(R.id.serverButton);
            b1.setText(R.string.stop_server);
            tv1.append("\n"+ "Started. IP = "+ipS);

            /*serverUp = true;
            SharedPreferences sharedpreferences = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = sharedpreferences.edit();
            edit.putString("up", "true");
            edit.commit();*/
        } catch (IOException e) {
            e.printStackTrace();
            tv1.append("\n"+ "Server already running on IP = "+ipS+"\n"+"If you are experincing any errors, please try to stop the app and reopen it again.");
        }
    }

    private void stopServer(){
        if(mHttpServer != null){
            mHttpServer.stop(0);
            tv1.append("\n"+"Stopped" );
            Button b1 = findViewById(R.id.serverButton);
            b1.setText(R.string.start_server);

            /*SharedPreferences sharedpreferences = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = sharedpreferences.edit();
            edit.putString("up", "false");
            edit.commit();*/
        }
    }

    private void startUDP() {
        String udpBroadcast = "UDPBroadcast";
        boolean shouldRestartSocketListen = true;
        tv1.append("\n"+"UDP server listening" );

        String[] ipParts = ipS.split("\\.");
        String ipBroadcast = ipParts[0]+"."+ipParts[1]+"."+ipParts[2]+".255";
        Log.d("IP BROADCAST", ipBroadcast);

        Thread UDPBroadcastThread = new Thread(new Runnable() {
            public void run() {
                try {
                    InetAddress broadcastIP = InetAddress.getByName(ipBroadcast);
                    Integer port = 4455;
                    while (shouldRestartSocketListen) {
                        byte[] recvBuf = new byte[15000];
                        if (socket == null || socket.isClosed()) {
                            socket = new DatagramSocket(port, broadcastIP);
                            socket.setBroadcast(true);
                        }

                        DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                        Log.e("UDP", "Waiting for UDP broadcast");
                        socket.receive(packet);

                        String senderIP = packet.getAddress().getHostAddress();
                        String message = new String(packet.getData()).trim();

                        Log.e("UDP", "Got UDP broadcast from " + senderIP + "  port:" + packet.getPort()+ "  " + message);
                        sendUDP(packet);
                        socket.close();
                    }
                } catch (Exception e) {
                    Log.i("UDP", "no longer listening for UDP broadcasts cause of error " + e.getMessage());
                   // tv1.append("\n"+"UDP server not available. Please stop the app and restart it again" );
                }
            }
        });
        UDPBroadcastThread.start();
    }

    public void sendUDP(DatagramPacket p){
        byte[] buf = ipS.getBytes();
        new Thread(new Runnable() {
            public void run() {
                try {
                    InetAddress serverAddress = p.getAddress();
                    DatagramSocket socket2 = new DatagramSocket();
                    if (!socket2.getBroadcast()) socket2.setBroadcast(true);
                        DatagramPacket packetBack = new DatagramPacket(buf, buf.length,
                                serverAddress, 5544);
                        socket2.send(packetBack);
                        socket2.close();
                } catch (final UnknownHostException e) {
                    Log.i("UDP sender1", "no longer listening for UDP broadcasts cause of error " + e.getMessage());
                    tv1.append("\n"+"UDP server error. Please stop the app and restart it again" );
                    e.printStackTrace();
                } catch (final SocketException e) {
                    Log.i("UDP sender2", "no longer listening for UDP broadcasts cause of error " + e.getMessage());
                    tv1.append("\n"+"UDP server error. Please stop the app and restart it again" );
                    e.printStackTrace();
                } catch (final IOException e) {
                    Log.i("UDP sender3", "no longer listening for UDP broadcasts cause of error " + e.getMessage());
                    tv1.append("\n"+"UDP server error. Please stop the app and restart it again" );
                    e.printStackTrace();
                }
            }
        }).start();
    }

    HttpHandler rootHandler = new HttpHandler() {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String request = httpExchange.getRequestMethod();
            switch (request) {
                case "GET":
                    sendResponse(httpExchange, "Welcome to the images exchange server");
            }
        }
    };

    HttpHandler pingHandler = new HttpHandler() {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String request = httpExchange.getRequestMethod();
            switch (request) {
                case "GET":
                    try {
                        InetAddress a = InetAddress.getLocalHost();
                        InetAddress[] b = InetAddress.getAllByName("");
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                    JSONObject json = new JSONObject();
                    try {
                        json.put("ip", ipS);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        sendResponse(httpExchange, "{error: 6, Parsing send JSON error, couldn't create a response}");
                    }
                    sendResponse(httpExchange, json.toString());
            }
        }
    };

    HttpHandler syncHandler = new HttpHandler() {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String request = httpExchange.getRequestMethod();
            switch (request) {
                case "GET":
                    sendResponse(httpExchange, "Sync handler");

                case "POST":
                    InputStream body = httpExchange.getRequestBody();
                    String text = streamToString(body);
                    String devID = null;
                    String type = null;

                    try {
                        JSONObject bodyJson = new JSONObject(text);
                        devID = bodyJson.getString("devID");
                        type = bodyJson.getString("type");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        sendResponse(httpExchange, "{error 5: Parsing received JSON error}");
                    }
                    try {
                        String lastSync = getLastSync(devID,type);
                        if(lastSync.equals("-1")){
                            JSONObject answer = new JSONObject();
                            answer.put("Error 4", "Couldn't get the last synchronization date");
                            sendResponse(httpExchange, answer.toString());
                            Log.e("SYNC ERROR","getLastSync returned value -1");
                            return;
                        }
                        JSONObject answer = new JSONObject();
                        answer.put("devID", devID);
                        answer.put("type", type);
                        answer.put("lastSync", lastSync);
                        sendResponse(httpExchange, answer.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        sendResponse(httpExchange, "{error: 6, Parsing send JSON error, couldn't create a response}");
                    }
            }
        }
    };

    HttpHandler imagesHandler = new HttpHandler() {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String request = httpExchange.getRequestMethod();
            switch (request){
                case "GET":
                    sendResponse(httpExchange, "Welcome to my images handler");

                case "POST":
                    InputStream body = httpExchange.getRequestBody();
                    String text = streamToString(body);
                    String pid = null;
                    String date = null;
                    String devID = null;
                    String type = null;
                    String sessionID = null;
                    String img64 = null;
                    String checkVal = null;
                    String metadata = null;
                    //writeTv1("Image 1");
                    try {
                        JSONObject bodyJson = new JSONObject(text);
                        pid = bodyJson.getString("pid");
                        date = bodyJson.getString("date");
                        devID = bodyJson.getString("devID");
                        type = bodyJson.getString("type");
                        sessionID = bodyJson.getString("sessionID");
                        img64 = bodyJson.getString("im");
                        checkVal = bodyJson.getString("check");
                        metadata = bodyJson.getString("meta");
                    }catch(JSONException e) {
                        e.printStackTrace();
                        sendResponse(httpExchange, "{error 5: Parsing received JSON error}");
                    }
                    try{
                        byte[] imgBytes = toBytes(img64);
                        if(imgBytes==null){
                            Log.e("ERROR 8", "Image format base64 incorrect. Expected something like: data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAA");
                            JSONObject answer = new JSONObject();
                            answer.put("Error 8", "base64 format incorrect. Expected something like: data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAA ");
                            sendResponse(httpExchange, answer.toString());
                            return;
                        }
                        byte[] checkim = toBytesCheck(img64);
                        long checkIMG = getCRC32Checksum(checkim);
                        Log.d("CHECKSUUUUM", String.valueOf(checkIMG));
                        if(!(checkIMG == Long.parseLong(checkVal))){
                            JSONObject answer = new JSONObject();
                            answer.put("Error 1", "Checksum mismatch, image corrupted");
                            sendResponse(httpExchange, answer.toString());
                            Log.e("CHECKSUM", "IMAGE CORRUPTED");
                            return;
                        }else {
                            File f = toPNG(imgBytes, pid);
                            String imgId = addImageToDB(f);
                            String enrollmentID = getEnrrolmentUid(pid);
                            String eventID = getEventID(enrollmentID, sessionID, devID, type);
                            if(f==null || imgId == null || enrollmentID == null || eventID == null){
                                JSONObject answer = new JSONObject();
                                answer.put("Error 7", "DHIS2 registering error");
                                sendResponse(httpExchange, answer.toString());
                                Log.e("ERROR", "DHIS2 error");
                            }
                            try {
                                if(eventID.equals("-1")){
                                    addEvent(enrollmentID, imgId,date, devID, type, sessionID, metadata);
                                }else{
                                    addImage(imgId, eventID, date);
                                }
                            } catch (Exception e){
                                Log.e("ADDING IMAGE","ERROR");
                                e.printStackTrace();
                                JSONObject answer = new JSONObject();
                                answer.put("Error 2", "Unable to register the participant");
                                sendResponse(httpExchange, answer.toString());
                                return;
                            }
                            JSONObject json = new JSONObject();
                            json.put("state", "OK");
                            sendResponse(httpExchange, json.toString());
                            Log.d("/images ANSWER:", json.toString());
                            setLastSync(devID,type,date);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        sendResponse(httpExchange, "{error: 6, Parsing send JSON error, couldn't create a response}");
                        return;
                    }
            }
        }
    };

    HttpHandler checkHandler = new HttpHandler() {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String request = httpExchange.getRequestMethod();
            switch (request) {
                case "GET":
                    sendResponse(httpExchange, "Check handler");

                case "POST":
                    InputStream body = httpExchange.getRequestBody();
                    String text = streamToString(body);
                    try {
                        JSONObject bodyJson = new JSONObject(text);
                        String pid = bodyJson.getString("pid");
                        String sessionID = bodyJson.getString("sessionID");
                        String devID = bodyJson.getString("devID");
                        String type = bodyJson.getString("type");

                        String imgs = finalCheck(pid, sessionID, devID, type);
                        byte[] i = imgs.getBytes();
                        Long checkImg = getCRC32Checksum(i);
                        JSONObject answer = new JSONObject();
                        answer.put("pid", pid);
                        answer.put("imgs", imgs);
                        answer.put("check", String.valueOf(checkImg));
                        sendResponse(httpExchange, answer.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        sendResponse(httpExchange, "{error: 6, Parsing send JSON error, couldn't create a response}");
                    }
            }
        }
    };

    HttpHandler endHandler = new HttpHandler() {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String request = httpExchange.getRequestMethod();
            switch (request) {
                case "GET":
                    JSONObject answer = new JSONObject();
                    try {
                        answer.put("state", "ok");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    sendResponse(httpExchange, "End handler");
                    SharedPreferences sharedpreferences = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
                    SharedPreferences.Editor edit = sharedpreferences.edit();
                    edit.putString("sync", "false");
                    edit.commit();
            }
        }
    };

    private byte[] toBytes(String base64) throws IOException {
        boolean coma = base64.contains(",");
        if (!coma){
            return null;
        }
        String im = base64.split(",")[1];
        BASE64Decoder decoder = new BASE64Decoder();
        byte[] b = decoder.decodeBuffer(im);
        Log.d("IMAGEEE", im);
        return b; //im.getBytes();
    }

    private byte[] toBytesCheck(String base64check) throws IOException {
        boolean coma = base64check.contains(",");
        if (!coma){
            return null;
        }
        String im = base64check.split(",")[1];
        return im.getBytes();
    }

    private File toPNG(byte[] b1, String pid) throws IOException {
        InputStream inputStream  = new ByteArrayInputStream(b1);
        Bitmap bitmap  = BitmapFactory.decodeStream(inputStream);
        File dest = new File(FileResourceDirectoryHelper.
                getFileResourceDirectory(getApplicationContext()), pid+".jpg");
        try {
            FileOutputStream out = new FileOutputStream(dest);
            bitmap.compress(Bitmap.CompressFormat.JPEG,90,out);
            out.flush();
            out.close();
        }catch (Exception e){
            Log.e("IMAGE CONVERSION","ERROR");
            e.printStackTrace();
            return null;
        }
        return dest;
    }

    private long getCRC32Checksum(byte[] bytes) {
        Checksum crc32 = new CRC32();
        crc32.update(bytes, 0, bytes.length);
        return crc32.getValue();
    }

    private String addImageToDB(File file){
        String imgId = null;
        try {
            imgId = Sdk.d2().fileResourceModule().fileResources().blockingAdd(file);
        }catch (Exception e){
            Log.e("DHIS2 IMAGE TO DATABASE","ERROR");
            e.printStackTrace();
            return null;
        }
        return imgId;
    }

    private String getEnrrolmentUid(String pid){
        String enrollmentID = null;
        List<TrackedEntityInstance> instances = Sdk.d2().trackedEntityModule().trackedEntityInstanceQuery()
                .byAttribute(pidAtri).eq(pid)
                .blockingGet();
        if(instances.isEmpty()){
            enrollmentID = enroll(pid);
            return enrollmentID;
        }else{
            String trackerID = instances.get(0).uid();
            List<Enrollment> enrollment = Sdk.d2().enrollmentModule().enrollments().
                    byTrackedEntityInstance().eq(trackerID).blockingGet();
            if(enrollment == null){
                return null;
            }
            return enrollment.get(0).uid();
        }
    }

    private String enroll(String pid){
        String enrollmentID = null;
        try {
            SharedPreferences sharedpreferences = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
            unitUid = sharedpreferences.getString("unit", "");
            Log.d("ORGUNIT1", unitUid);
            String trackedUid = Sdk.d2().trackedEntityModule().trackedEntityInstances()
                    .blockingAdd(TrackedEntityInstanceCreateProjection.
                            create(unitUid, "RohgTo6O2d1"));
            Sdk.d2().trackedEntityModule().trackedEntityAttributeValues()
                    .value(pidAtri, trackedUid).blockingSet(pid);
            enrollmentID = Sdk.d2().enrollmentModule().enrollments().
                    blockingAdd(EnrollmentCreateProjection.create(unitUid,progCapture, trackedUid));
            Sdk.d2().enrollmentModule().enrollments().uid(enrollmentID).setEnrollmentDate(new Date());
        }catch (Exception e){
            Log.e("DHIS2 ENROLLING","ERROR");
            e.printStackTrace();
            return null;
        }
        return enrollmentID;
    }


    private void addEvent(String enrollmentID, String imgId, String millis, String devID, String type, String sessionID, String metadata){
        try {
            SharedPreferences sharedpreferences = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
            unitUid = sharedpreferences.getString("unit", "");
            Log.d("ORGUNIT2", unitUid);
            String eventUid = Sdk.d2().eventModule().events().blockingAdd(EventCreateProjection
                    .create(enrollmentID,progCapture, "E55q4n17CGm", unitUid, "HllvX50cXC0"));
            Sdk.d2().eventModule().events().uid(eventUid).setEventDate(new Date());
            Sdk.d2().trackedEntityModule().trackedEntityDataValues().value(eventUid, image0Delem).blockingSet(imgId);
            Date d = millisToDate(millis);
            String date2 = d.toString();
            Sdk.d2().trackedEntityModule().trackedEntityDataValues().value(eventUid, dateDelem).blockingSet(date2);
            Sdk.d2().trackedEntityModule().trackedEntityDataValues().value(eventUid, typeDelem).blockingSet(type);
            Sdk.d2().trackedEntityModule().trackedEntityDataValues().value(eventUid, devIdDelem).blockingSet(devID);
            Sdk.d2().trackedEntityModule().trackedEntityDataValues().value(eventUid, totDelem).blockingSet("1");
            Sdk.d2().trackedEntityModule().trackedEntityDataValues().value(eventUid, sessionDelem).blockingSet(sessionID);
            String imID = millis+";";
            Sdk.d2().trackedEntityModule().trackedEntityDataValues().value(eventUid, imgIDsDelem).blockingSet(imID);
            addMetadata(metadata, eventUid);
            Log.d("EVENTUID",eventUid);
        }catch (Exception e){
            Log.e("DHIS2 ADDING EVENT","ERROR");
            e.printStackTrace();
        }
    }

    private void addMetadata(String metadata, String eventID){
        for(int i = 0; i<metaDelem.length; i++){
            TrackedEntityDataValue t = Sdk.d2().trackedEntityModule().trackedEntityDataValues()
                    .value(eventID, metaDelem[i]).blockingGet();
            if(t == null){
                try {
                    Sdk.d2().trackedEntityModule().trackedEntityDataValues().value(eventID, metaDelem[i]).blockingSet(metadata);
                    return;
                } catch (D2Error d2Error) {
                    d2Error.printStackTrace();
                }
            }
        }
    }

    private void addImage(String imgID, String eventID, String millis){
        List<ProgramStageDataElement> psDataElements = Sdk.d2().programModule().programStageDataElements()
                .byProgramStage().eq("E55q4n17CGm")
                .blockingGet();

        Set<String> dataElementUids = UidsHelper.getChildrenUids
                (psDataElements,ps -> Collections.singletonList(ps.dataElement()));

        List<DataElement> imageDataElement = Sdk.d2().dataElementModule().dataElements()
                .byUid().in(dataElementUids)
                .byValueType().eq(ValueType.IMAGE)
                .blockingGet();

        for(DataElement d : imageDataElement){
            String id = d.uid();
            Boolean empty = Sdk.d2().trackedEntityModule().trackedEntityDataValues().byEvent().eq(eventID)
                    .byDataElement().eq(id).blockingIsEmpty();
            Log.d("DATA-ELEMENTS-ID",id);
            Log.d("DATA-ELEMENTS-isEmpty",empty.toString());
            if(empty){
                try {
                    Sdk.d2().trackedEntityModule().trackedEntityDataValues()
                            .value(eventID, id).blockingSet(imgID);
                    String t = Sdk.d2().trackedEntityModule().trackedEntityDataValues()
                            .value(eventID, totDelem).blockingGet().value();
                    Sdk.d2().trackedEntityModule().trackedEntityDataValues()
                            .value(eventID, totDelem).blockingSet(String.valueOf(Integer.valueOf(t)+1));
                    String list = Sdk.d2().trackedEntityModule().trackedEntityDataValues()
                            .value(eventID, imgIDsDelem).blockingGet().value();
                    String newValue = list.concat(millis+";");
                    Log.i("NEW VALUE", newValue);
                    Sdk.d2().trackedEntityModule().trackedEntityDataValues()
                            .value(eventID, imgIDsDelem).blockingSet(newValue);
                    return;
                }catch (Exception e){
                    Log.e("DHIS2 ADDING IMAGE","ERROR");
                    e.printStackTrace();
                }
            }
        }
    }

    private String getEventID(String enrollmentID, String session, String dev, String t){
        String eventID = null;
        try {
            List<Event> events = Sdk.d2().eventModule().events().byEnrollmentUid().eq(enrollmentID).blockingGet();
            if(!events.isEmpty()){
                for(Event e : events){
                    eventID = e.uid();
                    List<TrackedEntityDataValue> dataElemSession = Sdk.d2().trackedEntityModule().trackedEntityDataValues()
                            .byDataElement().eq(sessionDelem)
                            .byEvent().eq(eventID).blockingGet();
                    List<TrackedEntityDataValue> dataElemDevID = Sdk.d2().trackedEntityModule().trackedEntityDataValues()
                            .byDataElement().eq(devIdDelem)
                            .byEvent().eq(eventID).blockingGet();
                    List<TrackedEntityDataValue> dataElemType = Sdk.d2().trackedEntityModule().trackedEntityDataValues()
                            .byDataElement().eq(typeDelem)
                            .byEvent().eq(eventID).blockingGet();

                    Log.d("SESSION ID", dataElemSession.toString());
                    if(dataElemSession == null || dataElemDevID == null || dataElemType == null){
                        Log.d("getEventID ERROR", "SessionID or devID or type = null");
                        return "-1";
                    }
                    String sessionID = dataElemSession.get(0).value();
                    String devID = dataElemDevID.get(0).value();
                    String type = dataElemType.get(0).value();
                    if(sessionID.equals(session) && devID.equals(dev) && type.equals(t)){
                        return eventID;
                    }
                }
            }
        }catch (Exception e){
            Log.e("DHIS2 GETTING EVENTID","ERROR");
            e.printStackTrace();
            return null;
        }
        return "-1";
    }

    private String finalCheck(String pid, String sessionID, String devID, String type){
        String enrollmentID = getEnrrolmentUid(pid);
        String eventID = getEventID(enrollmentID, sessionID, devID, type);
        if(eventID.equals("-1")){
            return "";
        }
        String imgs = Sdk.d2().trackedEntityModule().trackedEntityDataValues()
                .value(eventID, imgIDsDelem).blockingGet().value();
        Log.d("CHECK", imgs);

        return imgs;
    }

    private Date millisToDate(String dateInMillis){
        long millis = Long.valueOf(dateInMillis);
        Date date = new Date(millis);
        return date;
    }

    private String getLastSync(String devID, String type) {
        SharedPreferences sharedpreferences = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        unitUid = sharedpreferences.getString("unit", "");
        List<TrackedEntityInstance> instances = Sdk.d2().trackedEntityModule().trackedEntityInstanceQuery()
                .byProgram().eq(progSync).byAttribute(typeAtri).eq(type)
                .byAttribute(devAtri).eq(devID).byOrgUnits().eq(unitUid).blockingGet();
        Log.d("INSTANCES",instances.toString());
        if(instances.isEmpty()){
            return registerDevice(devID, type);
        }else{
            String lastSync = Sdk.d2().trackedEntityModule().trackedEntityAttributeValues()
                    .byTrackedEntityInstance().eq(instances.get(0).uid())
                    .byTrackedEntityAttribute().eq(dateAtri)
                    .blockingGet().get(0).value();
            if(lastSync == null){
                Log.d("getLastSync ERROR:","lastSync=NULL");
                return registerDevice(devID, type);
            }
            Log.d("GET LAST SYNC",lastSync);
            return lastSync;
        }
    }

    private String registerDevice(String devID, String type){
        String trackedUid = null;
        try {
            SharedPreferences sharedpreferences = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
            unitUid = sharedpreferences.getString("unit", "");
            trackedUid = Sdk.d2().trackedEntityModule().trackedEntityInstances()
                    .blockingAdd(TrackedEntityInstanceCreateProjection.
                            create(unitUid, "zICZI1HYLNF"));
            Sdk.d2().trackedEntityModule().trackedEntityAttributeValues()
                    .value(devAtri, trackedUid).blockingSet(devID);
            Sdk.d2().trackedEntityModule().trackedEntityAttributeValues()
                    .value(typeAtri, trackedUid).blockingSet(type);
            Sdk.d2().trackedEntityModule().trackedEntityAttributeValues()
                    .value(dateAtri, trackedUid).blockingSet("0");
            String enrollmentID = Sdk.d2().enrollmentModule().enrollments().
                    blockingAdd(EnrollmentCreateProjection.create(unitUid,"f0Z8L1MW3Jv", trackedUid));
            Sdk.d2().enrollmentModule().enrollments().uid(enrollmentID).setEnrollmentDate(new Date());
            Log.d("REGISTER DEVICE","END");
        } catch (D2Error d2Error) {
            d2Error.printStackTrace();
            return "-1";
        }
        return "0";
    }

    private void setLastSync(String devID, String type, String date){
        SharedPreferences sharedpreferences = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        unitUid = sharedpreferences.getString("unit", "");
        String syncUid = null;
        List<TrackedEntityInstance> instances = Sdk.d2().trackedEntityModule().trackedEntityInstanceQuery()
                .byProgram().eq(progSync).byAttribute(typeAtri).eq(type)
                .byAttribute(devAtri).eq(devID).byOrgUnits().eq(unitUid).blockingGet();
        if(instances.isEmpty()){
            registerDevice(devID, type);
            Log.d("SET-LAST-SYNC","It wasn't registered");
            instances = Sdk.d2().trackedEntityModule().trackedEntityInstanceQuery()
                    .byProgram().eq(progSync).byAttribute(typeAtri).eq(type)
                    .byAttribute(devAtri).eq(devID).blockingGet();
            //syncUid = instances.get(0).uid();
        }
        try {
            syncUid = instances.get(0).uid();
            Sdk.d2().trackedEntityModule().trackedEntityAttributeValues()
                    .value(dateAtri, syncUid).blockingSet(date);
            Log.d("SYNC:", date);
        } catch (D2Error d2Error) {
            d2Error.printStackTrace();
        }
    }

    private Bitmap toBitmap(String im) throws IOException {
        String base64 = im.split(",")[1];
        BASE64Decoder decoder = new BASE64Decoder();
        byte[] b = decoder.decodeBuffer(base64);
        check = getCRC32Checksum(b);
        InputStream inputStream  = new ByteArrayInputStream(b);
        Bitmap bitmap  = BitmapFactory.decodeStream(inputStream);
        return bitmap;
    }

    private boolean validatePID(String pid){
        if(pid.length()>10 || pid.charAt(2)>3 || pid.charAt(1)>1){
            return false;
        }else{
            return true;
        }
    }

    private boolean validateDate(String date){
        long now = System.currentTimeMillis();
        long d = Long.valueOf(date);
        long year = Long.valueOf("31556952000");
        if(d > now || d < (now - year)){
            return false;
        }else{
            return true;
        }
    }

    private boolean validateDevID(){
        return true;
    }

    private boolean validateTot(String tot){
        if(tot.length()>3){
            return false;
        }else{
            return true;
        }
    }

    private boolean validateType(String tot){
        if(tot.equals("IRIS") || tot.equals("Pocket") || tot.equals("Smartphone")){
            return true;
        }else{
            return false;
        }
    }

    private String orgUnitUid(String pid){

        List<OrganisationUnit> o = Sdk.d2().organisationUnitModule().organisationUnits()
                .byParentUid().eq("obakH4liyr5").blockingGet();
        Log.d("ORGANIZATION UNIT",o.toString());
        orgUnitUid("01258911111");
        String siteCod = pid.substring(0,2);
        Log.d("String:",siteCod);
        String clinicCod = pid.substring(2,5);
        Log.d("String:",clinicCod);
        return siteCod;
    }

    private String longToIp(long i) {
        return ((i & 0xFF) + "." +
                ((i >> 8) & 0xFF) +
                "." + ((i >> 16) & 0xFF) +
                "." + ((i >> 24) & 0xFF));
    }

    private String streamToString(InputStream inputStream){
        Scanner s = new Scanner(inputStream).useDelimiter("\\A");
        if(s.hasNext()){
            return s.next();
        }else{
            return "";
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        SharedPreferences sharedpreferences = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedpreferences.edit();
        edit.putString("unit", unitUids[i]);
        edit.commit();

        unitUid = unitUids[i];
        Log.d("SELECTED UNIT ID",unitUid);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        Context context = getApplicationContext();
        CharSequence text = "Please, select a center from the list!";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
}
