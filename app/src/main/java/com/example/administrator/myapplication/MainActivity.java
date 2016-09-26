package com.example.administrator.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public class MainActivity extends FragmentActivity {

    private static final String TAG = "MainActivity";

    public static int SCREEN_WIDTH ;
    public static int SCREEN_HEIGHT ;
    public static String DEVICE_ID = "";//15位数字
    public static String MODEL;
    public static String MANUFACTURER;
    public static String WIFI_MAC_ADDRESS;


    TextView infoTv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        infoTv = (TextView) findViewById(R.id.info);
        infoTv.setText("Test");

        findViewById(R.id.title).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadInfo();
            }
        });

    }

    private void loadInfo()
    {
        Point pt = Utility.getScreenSize(this);
        String info = getMobilInforStr();
        infoTv.setText(pt.x + "x" + pt.y + "\n" + Utility.dip2px(this,1) + "x" + "\n" + info.replaceAll("#", "\n"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadInfo();
    }

    /**
     * 手机信息
     * @return
     */
    public String getMobilInforStr()
    {
        Map<String, String> MobileInfor = new LinkedHashMap<>();

        boolean hasReadPhoneStatePermission = false;
        if(SupportVersion.M())
        {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                    == PackageManager.PERMISSION_GRANTED) {
                hasReadPhoneStatePermission = true;
            }
        }
        else {
            hasReadPhoneStatePermission = true;
        }

        if(hasReadPhoneStatePermission)
        {
            TelephonyManager tm = ((TelephonyManager) getSystemService(TELEPHONY_SERVICE));
            DEVICE_ID = "" + tm.getDeviceId();
            MobileInfor.put("DeviceId", DEVICE_ID);//IMEI
            MobileInfor.put("Line1Number", "" + tm.getLine1Number());//MSISDN
            MobileInfor.put("DeviceSoftwareVersion","" +  tm.getDeviceSoftwareVersion());
            MobileInfor.put("SimOperatorName", "" + tm.getSimOperatorName());//ICCID:ICC
            MobileInfor.put("SimSerialNumber", "" + tm.getSimSerialNumber());

            MobileInfor.put("NetworkType", "" + tm.getNetworkType());
            MobileInfor.put("PhoneType", "" + tm.getPhoneType());

            MobileInfor.put("Operator", "" + tm.getSimOperator());
            MobileInfor.put("NetworkOperatorName", "" + tm.getNetworkOperatorName());
            MobileInfor.put("SubId", "" + tm.getSubscriberId());//IMSI
            MobileInfor.put("Country", "" + tm.getNetworkCountryIso());

            MobileInfor.put("PhoneType", "" + tm.getPhoneType());
        }

        String[] abis = new String[]{};
        if(SupportVersion.Lollipop())
        {
            abis = Build.SUPPORTED_ABIS;
        }
        else
        {
            abis = new String[]{Build.CPU_ABI,Build.CPU_ABI2};
        }
        StringBuilder abiSB = new StringBuilder();
        for(String abi:abis)
        {
            abiSB.append(abi);
            abiSB.append(',');
        }
        String abiStr = null;
        if (abiSB.length() > 0)
            abiStr = abiSB.substring(0, abiSB.length() - 1);
        else {
            abiStr = abiSB.toString();
        }

        MobileInfor.put("ABIS", "" + abiStr);
        MobileInfor.put("BOARD", "" + Build.BOARD);
        MobileInfor.put("BRAND", "" + Build.BRAND);
        MobileInfor.put("DEVICE", "" + Build.DEVICE);
        MobileInfor.put("DISPLAY", "" + Build.DISPLAY);
        MobileInfor.put("FINGERPRINT", "" + Build.FINGERPRINT);
        MobileInfor.put("ID", "" + Build.ID);
        MANUFACTURER = Build.MANUFACTURER;
        MobileInfor.put("MANUFACTURER", "" + MANUFACTURER);
        MODEL = Build.MODEL;
        MobileInfor.put("MODEL", "" + MODEL);
        MobileInfor.put("PRODUCT", "" + Build.PRODUCT);
        MobileInfor.put("INCREMENTAL", "" + Build.VERSION.INCREMENTAL);
        MobileInfor.put("RELEASE", "" + Build.VERSION.RELEASE);
        MobileInfor.put("SDK", "" + Build.VERSION.SDK);
        MobileInfor.put("HOST", "" + Build.HOST);
        MobileInfor.put("TAGS", "" + Build.TAGS);
        MobileInfor.put("TYPE", "" + Build.TYPE);
        MobileInfor.put("USER", "" + Build.USER);
        MobileInfor.put("TIME", "" + Build.TIME);
        MobileInfor.put("SERIAL", "" + Build.SERIAL);

        MobileInfor.put("isMIUI", "" + MIUIUtils.isMIUI());


        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        WIFI_MAC_ADDRESS = info.getMacAddress();
        MobileInfor.put("wifiMacAddress", "" + WIFI_MAC_ADDRESS);//MAC?
        MobileInfor.put("wifiState", "" + wifi.getWifiState());
        InetAddress address = getLocalIpAddress();
        MobileInfor.put("IP", "" + address.getHostAddress());
        //MobileInfor.put("UserAgent", "" + new WebView(this).getSettings().getUserAgentString());

        MobileInfor.put("versionName", "" + Utility.getVerstionStr(this));

        HashSet<String> sdPath = FileUtil.getSdCardPath(this);
        HashSet<String> sdCardPath = new HashSet<String>();
        if (sdPath != null) {
            for (String path : sdPath) {
                File sdFile = new File(path);
                if (sdFile != null && sdFile.exists() && sdFile.isDirectory() && sdFile.canWrite()) {
                    String absPath = sdFile.getAbsolutePath();
                    sdCardPath.add(absPath);
                }
            }
        }

        int counter = 0;
        for (String path : sdCardPath) {
            FileUtil.getStatFs(path);
            MobileInfor.put("sdcard" + counter++, "" + path);
        }

        String marketPkg = getPackageManager().getInstallerPackageName(getPackageName());
        if (marketPkg != null) {
            MobileInfor.put("market", "" + marketPkg);
        }

        MobileInfor.put("Mac", "" + getMac());

        try {
            test();
        } catch (SocketException e) {
            e.printStackTrace();
        }

        StringBuffer sb = new StringBuffer();//"A:"

//		SCREEN_WIDTH = getWindowManager().getDefaultDisplay().getWidth();
//		int heght = getWindowManager().getDefaultDisplay().getHeight();
//		sb.append(SCREEN_WIDTH + "*" + heght + "#");

        for (String key : MobileInfor.keySet())
        {
            String v = MobileInfor.get(key);
            if(v!=null && v.length()>0)
            {
                sb.append(key).append(':').append(v).append('#');
            }
        }
        String mobileInforStr;
        if (sb.length() > 0)
            mobileInforStr = sb.substring(0, sb.length() - 1);
        else {
            mobileInforStr = sb.toString();
        }
        //mobileInforStr += ("\nLength:" + (mobileInforStr.length()-mobileInforStr.split("#").length));
        Log.i(TAG, mobileInforStr);
        return mobileInforStr;
    }



    public static InetAddress getLocalIpAddress()
    {
        try
        {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();)
            {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
                {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress())
                    {
                        return inetAddress;
                    }
                }
            }
        }
        catch (SocketException ex)
        {
            Log.e(TAG, ex.toString());
        }
        return null;
    }

    private String getMac()
    {
        String macSerial = null;
        String str = "";

        try
        {
            Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address ");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            for (; null != str;)
            {
                str = input.readLine();
                if (str != null)
                {
                    macSerial = str.trim();// 去空格
                    break;
                }
            }
        } catch (IOException ex) {
            // 赋予默认值
            ex.printStackTrace();
        }
        return macSerial;
    }

    private void test() throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface iF = interfaces.nextElement();

            byte[] addr = iF.getHardwareAddress();
            if (addr == null || addr.length == 0) {
                continue;
            }

            StringBuilder buf = new StringBuilder();
            for (byte b : addr) {
                buf.append(String.format("%02X:", b));
            }
            if (buf.length() > 0) {
                buf.deleteCharAt(buf.length() - 1);
            }
            String mac = buf.toString();
            Log.e(TAG, "interfaceName="+iF.getName()+", mac="+mac);
        }
    }




}
