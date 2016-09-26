package com.example.administrator.myapplication;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipException;



import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.StatFs;
import android.os.Build.VERSION;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class FileUtil{
	
	public static final int BUFFER_SIZE = 4096;
	
	public static final String MIME_TYPE_TEXT_PLAIN = "text/plain";
	public static final String MIME_TYPE_TEXT_XML = "text/xml";
	public static final String MIME_TYPE_TEXT_HTML = "text/html";
	
	// http://www.w3school.com.cn/media/media_mimeref.asp
	// http://www.ezloo.com/2008/10/mime.html
	public static final String MIME_TYPE_APK = "application/vnd.android.package-archive";
	public static final String MIME_TYPE_APK2 = "application/vnd.android";
	public static final String MIME_TYPE_APPLICATION_APK = "application/apk";
	public static final String MIME_TYPE_ZIP = "application/zip";
	public static final String MIME_TYPE_7Z = "application/x-7z-compressed";
	public static final String MIME_TYPE_JAR = "application/java-archive";
	public static final String MIME_TYPE_MSWORD = "application/msword";
	public static final String MIME_TYPE_MSPPT = "application/vnd.ms-powerpoint";
	public static final String MIME_TYPE_MSPPT2 = "application/mspowerpoint";
	public static final String MIME_TYPE_MSEXCEL = "application/vnd.ms-excel";
	public static final String MIME_TYPE_PDF = "application/pdf";
	public static final String MIME_TYPE_RAR = "application/x-rar-compressed";
	public static final String MIME_TYPE_TAR = "application/x-tar";
	public static final String MIME_TYPE_GZ = "application/x-gzip";
	public static final String MIME_TYPE_DMG = "application/octet-stream";
	public static final String MIME_TYPE_TGZ = "application/x-compressed";
	public static final String MIME_TYPE_BZ2 = "application/x-bzip2";

	//
	public static final String MIME_TYPE_MKA = "audio/x-matroska";
	public static final String MIME_TYPE_APE = "audio/x-monkeys-audio";

	//Video
	//https://en.wikipedia.org/wiki/Video_file_format
	public static final String MIME_TYPE_RMVB = "application/vnd.rn-realmedia-vbr";
	public static final String MIME_TYPE_VIDEO_MATROSKA = "video/x-matroska";
	public static final String MIME_TYPE_MOV = "video/quicktime";
	public static final String MIME_TYPE_MP4 = "video/mp4";
	public static final String MIME_TYPE_WEBM = "video/webm";
	public static final String MIME_TYPE_FLV = "video/x-flv";


	public static final String MIME_TYPE_MK3D = "video/x-matroska-3d";


	public static final String MIME_TYPE_IMAGE_JPEG = "image/jpeg";
	public static final String MIME_TYPE_IMAGE_PNG = "image/png";
	public static final String MIME_TYPE_IMAGE_GIF = "image/gif";
	public static final String MIME_TYPE_IMAGE_WEBP = "image/webp";
	public static final String MIME_TYPE_IMAGE_SVG = "image/svg+xml";

	public static final String MIME_TYPE_TORRENT = "application/x-bittorrent";

	public static final String MIME_TYPE_TEXT = "text/*";
	public static final String MIME_TYPE_IMAGE = "image/*";
	public static final String MIME_TYPE_VIDEO = "video/*";
	public static final String MIME_TYPE_AUDIO = "audio/*";
	public static final String MIME_TYPE_ALL = "*/*";

	public static final String MIME_TYPE_TTF = "application/font-sfnt";//??? application/font-sfnt  font/opentype
	
	private static final String TAG = "FileUtil";

	public static boolean isSdCardRootPath(Context context, String path) {
		if (TextUtils.isEmpty(path)) {
			return false;
		}
		String singlePath = path;
		File file = new File(path);
		if (file != null && file.exists() && file.isDirectory()) {
			singlePath = file.getAbsolutePath();
		}
		HashSet<String> sdPath = getSdCardPath(context);
		if (sdPath != null) {
			for (String p : sdPath) {
				File sdFile = new File(p);
				if (sdFile != null && sdFile.exists() && sdFile.isDirectory() && sdFile.canWrite()) {
					String absPath = sdFile.getAbsolutePath();
					if (singlePath != null && singlePath.equals(absPath)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static HashSet<String> getSdCardPath(Context context) {
		HashSet<String> pathSet = new HashSet<String>();
		File extDir = Environment.getExternalStorageDirectory();
		if (extDir != null && extDir.exists()) {
			pathSet.add(extDir.getAbsolutePath());
		}
		pathSet.addAll(getExternalMounts());
		String[] path2 = getSdCardPathReflect(context);
		if (path2 != null) {
			for (String path : path2) {
				pathSet.add(path);
			}
		}
		return pathSet;
	}

	public static String getSdCardPathTest(Context context) {
		HashSet<String> pathSet = new HashSet<String>();
		File extDir = Environment.getExternalStorageDirectory();
		if (extDir != null && extDir.exists()) {
			pathSet.add(extDir.getAbsolutePath());
		}
		pathSet.addAll(getExternalMounts());
		StringBuffer sb = new StringBuffer();
		for (String path : pathSet) {
			sb.append(path).append('\n');
		}
		sb.append("=======").append('\n');
		String[] path2 = getSdCardPathReflect(context);
		if (path2 != null) {
			for (String path : path2) {
				sb.append(path).append('\n');
				pathSet.add(path);
			}
		}

		return sb.toString();
	}

	public static String[] getSdCardPathReflect(Context context) {
		String[] paths = null;
		if (context == null) {
			return paths;
		}
		StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
		// 获取sdcard的路径：外置和内置
		try {
			paths = (String[]) sm.getClass().getMethod("getVolumePaths").invoke(sm);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return paths;
	}

	public static HashSet<String> getExternalMounts() {
		final HashSet<String> out = new HashSet<String>();
		String reg = "(?i).*vold.*(vfat|ntfs|exfat|fat32|ext3|ext4).*rw.*";
		String s = "";
		try {
			final Process process = new ProcessBuilder().command("mount")
					.redirectErrorStream(true).start();
			process.waitFor();
			final InputStream is = process.getInputStream();
			final byte[] buffer = new byte[1024];
			while (is.read(buffer) != -1) {
				s = s + new String(buffer);
			}
			is.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}

		// parse output
		final String[] lines = s.split("\n");
		for (String line : lines) {
			if (!line.toLowerCase(Locale.US).contains("asec")) {
				if (line.matches(reg)) {
					String[] parts = line.split(" ");
					for (String part : parts) {
						if (part.startsWith("/"))
							if (!part.toLowerCase(Locale.US).contains("vold"))
								out.add(part);
					}
				}
			}
		}
		return out;
	}

	/**
	 * 新建文件
	 * 
	 * @param f
	 * @return
	 */
	public static boolean newFile(File f)
	{
		try
		{
			f.createNewFile();
		}
		catch (Exception e)
		{
			return false;
		}
		return true;
	}


	
	public static String getSuffix(File f)
	{
		String fName = f.getName();
		String suffix = "";
		if(fName.contains("."))
		{
			suffix = fName.substring(fName.lastIndexOf(".") + 1, fName.length()).toLowerCase();
		}
		return suffix;
	}
	
	public static String getSuffix(String fName)
	{
		if (TextUtils.isEmpty(fName) || !fName.contains("."))
		{
			return "";
		}
		String suffix = fName.substring(fName.lastIndexOf(".") + 1, fName.length()).toLowerCase();
		return suffix;
	}
	
	/** 获取未安装的APK信息
	 * @param context
	 * @param archiveFilePath APK文件的路径。如：/sdcard/download/XX.apk
	 */
	public PackageInfo getUninatllApkInfo(Context context, String archiveFilePath){
		PackageManager pm = context.getPackageManager();
		PackageInfo info = pm.getPackageArchiveInfo(archiveFilePath, PackageManager.GET_ACTIVITIES);
		/*
		if(info != null){
			ApplicationInfo appInfo = info.applicationInfo;
			String appName = pm.getApplicationLabel(appInfo).toString();
			String packageName = appInfo.packageName;
			Drawable icon = pm.getApplicationIcon(appInfo);
		}*/
		return info;
	}
	
	/*
	 * 采用了新的办法获取APK图标，之前的失败是因为android中存在的一个BUG,通过 appInfo.publicSourceDir = apkPath;来修正这个问题，详情参见:
	 * http://code.google.com/p/android/issues/detail?id=9151
	 */
	public static Drawable getApkIcon(Context context, String apkPath)
	{
		PackageManager pm = context.getPackageManager();
		PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
		if (info != null)
		{
			ApplicationInfo appInfo = info.applicationInfo;
			appInfo.sourceDir = apkPath;
			appInfo.publicSourceDir = apkPath;
			try
			{
				return appInfo.loadIcon(pm);
			}
			catch (OutOfMemoryError e)
			{
				Log.e("ApkIconLoader", e.toString());
			}
		}
		return null;
	}

	/**
	 * 获取安装应用apk位置
	 * @param ctt
	 * @param packgename
	 * @return
	 */
	public static String getInstallApkPath(Context ctt, String packgename)
	{
		PackageManager pm = ctt.getPackageManager();
		try
		{
			PackageInfo info = pm.getPackageInfo(packgename, 0);
			if (info != null)
			{
				ApplicationInfo appInfo = info.applicationInfo;
				if(appInfo!=null)
				{
					String sourceDir = appInfo.sourceDir;
					return sourceDir;
				}
			}
		}
		catch (NameNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static Drawable getUninstallAPKIcon(Context ctx, String apkPath)
	{
		Drawable icon = null;

//		final String PATH_PackageParser = "android.content.pm.PackageParser";
//		final String PATH_AssetManager = "android.content.res.AssetManager";
//		try
//		{
//			// apk包的文件路径
//			// 这是一个Package 解释器, 是隐藏的
//			// 构造函数的参数只有一个, apk文件的路径
//			// PackageParser packageParser = new PackageParser(apkPath);
//			Class pkgParserCls = Class.forName(PATH_PackageParser);
//			Class[] typeArgs = new Class[1];
//			typeArgs[0] = String.class;
//			Constructor pkgParserCt = null;
//
//			pkgParserCt = pkgParserCls.getConstructor(typeArgs);
//
//			Object[] valueArgs = new Object[1];
//			valueArgs[0] = apkPath;
//			Object pkgParser = null;
//			pkgParser = pkgParserCt.newInstance(valueArgs);
//			Log.d("ANDROID_LAB", "pkgParser:" + pkgParser.toString());
//
//
//			// 这个是与显示有关的, 里面涉及到一些像素显示等等, 我们使用默认的情况
//			DisplayMetrics metrics = new DisplayMetrics();
//			metrics.setToDefaults();
//			// PackageParser.Package mPkgInfo = packageParser.parsePackage(new File(apkPath), apkPath, metrics, 0);
//			typeArgs = new Class[4];
//			typeArgs[0] = File.class;
//			typeArgs[1] = String.class;
//			typeArgs[2] = DisplayMetrics.class;
//			typeArgs[3] = Integer.TYPE;
//
//			Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod("parsePackage", typeArgs);
//			valueArgs = new Object[4];
//			valueArgs[0] = new File(apkPath);
//			valueArgs[1] = apkPath;
//			valueArgs[2] = metrics;
//			valueArgs[3] = 0;
//			Object pkgParserPkg = null;
//			if (pkgParser != null)
//			{
//				pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser, valueArgs);
//			}
//			// 应用程序信息包, 这个公开的, 不过有些函数, 变量没公开
//			// ApplicationInfo info = mPkgInfo.applicationInfo;
//			Field appInfoFld = pkgParserPkg.getClass().getDeclaredField("applicationInfo");
//			ApplicationInfo info = (ApplicationInfo) appInfoFld.get(pkgParserPkg);
//			// uid 输出为"-1"，原因是未安装，系统未分配其Uid。
//			Log.d("ANDROID_LAB", "pkg:" + info.packageName + " uid=" + info.uid);
//
//			// Resources pRes = getResources();
//			// AssetManager assmgr = new AssetManager();
//			// assmgr.addAssetPath(apkPath);
//			// Resources res = new Resources(assmgr, pRes.getDisplayMetrics(),
//			// pRes.getConfiguration());
//			Class assetMagCls = Class.forName(PATH_AssetManager);
//			Constructor assetMagCt = assetMagCls.getConstructor((Class[]) null);
//			Object assetMag = assetMagCt.newInstance((Object[]) null);
//			typeArgs = new Class[1];
//			typeArgs[0] = String.class;
//			Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod("addAssetPath", typeArgs);
//			valueArgs = new Object[1];
//			valueArgs[0] = apkPath;
//			assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);
//			Resources res = ctx.getResources();
//			typeArgs = new Class[3];
//			typeArgs[0] = assetMag.getClass();
//			typeArgs[1] = res.getDisplayMetrics().getClass();
//			typeArgs[2] = res.getConfiguration().getClass();
//			Constructor resCt = Resources.class.getConstructor(typeArgs);
//			valueArgs = new Object[3];
//			valueArgs[0] = assetMag;
//			valueArgs[1] = res.getDisplayMetrics();
//			valueArgs[2] = res.getConfiguration();
//			res = (Resources) resCt.newInstance(valueArgs);
//			CharSequence label = null;
//			if (info.labelRes != 0)
//			{
//				label = res.getText(info.labelRes);
//			}
//			// if (label == null) {
//			// label = (info.nonLocalizedLabel != null) ? info.nonLocalizedLabel
//			// : info.packageName;
//			// }
//			Log.d("ANDROID_LAB", "label=" + label);
//			// 这里就是读取一个apk程序的图标
//			if (info.icon != 0)
//			{
//				icon = res.getDrawable(info.icon);
//				/*ImageView image = (ImageView) findViewById(R.id.apkIconBySodino);
//				image.setVisibility(View.VISIBLE);
//				image.setImageDrawable(icon);*/
//				return icon;
//			}
//		}
//		catch (Throwable e)
//		{
//			e.printStackTrace();
//		}

		if (icon == null) {
			icon = getApkIcon(ctx, apkPath);
		}
		return icon;
	}
  
	
	public static ArrayList<File> sortFiles2(ArrayList<File> files)
	{
		ArrayList<File> ret = new ArrayList<File>();
		HashMap<String, List<File>> types = new HashMap<String, List<File>>();
		for(File f:files)
		{
			String fn = f.getName();
			//f = f.toLowerCase();
			if(!fn.contains("."))
			{
				List<File> lst = types.get(".");
				if(lst==null)
				{
					types.put(".", new ArrayList<File>());
					lst = types.get(".");
				}
				lst.add(f);
			}
			else
			{
				String suffix = getSuffix(f);
				List<File> lst = types.get(suffix);
				if(lst==null)
				{
					types.put(suffix, new ArrayList<File>());
					lst = types.get(suffix);
				}
				lst.add(f);
			}
		}
		
		for(List<File> list : types.values())
		{
			Collections.sort(list,new Comparator<File>() {
				@Override
				public int compare(File lhs, File rhs)
				{
					String lhsName = lhs.getName();
					String rhsName = rhs.getName();
					return lhsName.compareToIgnoreCase(rhsName);
				}
			});
			ret.addAll(list);
		}
		types.clear();
		
		return ret;
	}
	
	
	public static ArrayList<String> sortFiles(ArrayList<String> files)
	{
		ArrayList<String> ret = new ArrayList<String>();
		HashMap<String, ArrayList<String>> types = new HashMap<String, ArrayList<String>>();
		for(String f:files)
		{
			//f = f.toLowerCase();
			if(!f.contains("."))
			{
				ArrayList<String> lst = types.get(".");
				if(lst==null)
				{
					types.put(".", new ArrayList<String>());
					lst = types.get(".");
				}
				lst.add(f);
			}
			else
			{
				String suffix = getSuffix(f);
				ArrayList<String> lst = types.get(suffix);
				if(lst==null)
				{
					types.put(suffix, new ArrayList<String>());
					lst = types.get(suffix);
				}
				lst.add(f);
			}
		}
		
		for(ArrayList<String> list : types.values())
		{
			Collections.sort(list,new Comparator<String>() {
				@Override
				public int compare(String lhs, String rhs)
				{
					return lhs.compareToIgnoreCase(rhs);
				}
			});
			ret.addAll(list);
		}
		
		return ret;
	}
	
	/**
	 * 缩放图片的方法
	 * 
	 * @return
	 */
	public static Bitmap fitSizePic(File f)
	{
		Bitmap resizeBmp = null;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		//数字越大读出的图片占用的heap越小 不然总是溢出
		if (f.length() < 20480)
		{ //0-20k
			opts.inSampleSize = 1;
		}
		else if (f.length() < 51200)
		{ //20-50k
			opts.inSampleSize = 2;
		}
		else if (f.length() < 307200)
		{ //50-300k
			opts.inSampleSize = 4;
		}
		else if (f.length() < 819200)
		{ //300-800k
			opts.inSampleSize = 6;
		}
		else if (f.length() < 1048576)
		{ //800-1024k
			opts.inSampleSize = 8;
		}
		else
		{
			opts.inSampleSize = 10;
		}
		resizeBmp = BitmapFactory.decodeFile(f.getPath(), opts);
		return resizeBmp;
	}
	
	public static String fileSizeString(long length)
	{
		int sub_index = 0;
		String show = "";
		if (length >= 1073741824)//1024*1024*1024
		{
//			sub_index = (String.valueOf(length / 1073741824f)).indexOf(".");
//			show = (length / 1073741824f + "000").substring(0, sub_index + 3) + "GB";
			show = floatWithTwoPoint(length / 1073741824.0f) + "GB";
		}
		else if (length >= 1048576)
		{
//			sub_index = (String.valueOf(length / 1048576f)).indexOf(".");
//			show = (length / 1048576f + "000").substring(0, sub_index + 3) + "MB";
			show = floatWithTwoPoint(length / 1048576.0f) + "MB";
		}
		else if (length >= 1024)
		{
//			sub_index = (String.valueOf(length / 1024f)).indexOf(".");
//			show = (length / 1024f + "000").substring(0, sub_index + 3) + "KB";
			show = floatWithTwoPoint(length / 1024.0f) + "KB";
		}
		else if (length < 1024)
		{
			show = String.valueOf(length) + "B";
		}
		return show;
	}

	public static String floatWithTwoPoint(float val) {
		String tmpStr = String.format("%.2f", val);
		if (tmpStr.startsWith(".")) {
			tmpStr = "0" + tmpStr;
		}
		return tmpStr;
	}

	public static String floatWithOnePoint(float val) {
		String tmpStr = String.format("%.1f", val);
		if (tmpStr.startsWith(".")) {
			tmpStr = "0" + tmpStr;
		}
		return tmpStr;
	}

  /**
   * 文件大小描述
   * @param f
   * @return
   */
	public static String fileSizeMsg(File f)
	{
		int sub_index = 0;
		String show = "";
		if (f.isFile())
		{
			long length = f.length();
			if (length >= 1073741824)//1024*1024*1024
			{
				sub_index = (String.valueOf((float) length / 1073741824)).indexOf(".");
				show = ((float) length / 1073741824 + "000").substring(0, sub_index + 3) + "GB";
			}
			else if (length >= 1048576)
			{
				sub_index = (String.valueOf((float) length / 1048576)).indexOf(".");
				show = ((float) length / 1048576 + "000").substring(0, sub_index + 3) + "MB";
			}
			else if (length >= 1024)
			{
				sub_index = (String.valueOf((float) length / 1024)).indexOf(".");
				show = ((float) length / 1024 + "000").substring(0, sub_index + 3) + "KB";
			}
			else if (length < 1024)
			{
				show = String.valueOf(length) + "B";
			}
		}
		return show;
	}


	public static void mediaScan(Context context, File file) {
		try {
			MediaScannerConnection.scanFile(context,
                    new String[]{file.getAbsolutePath()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            Log.e(TAG, "scanFile:" + path + ",uri=" + uri);
                        }
                    });
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}


	/**
	 * 删除单个文件
	 * @param file
	 * @return
	 */
	public static boolean delFile(File file)
	{
		boolean ret = false;
		try
		{
			if (file.exists())
			{
				file.delete();
				ret = true;
			}
		}
		catch (Exception e)
		{
			return false;
		}
		return ret;
	}





	public static void copyAssetsFileToPath(Context context, String fileName, String outPath) throws IOException {
		if(context==null)
		{
			return;
		}
		OutputStream myOutput = new FileOutputStream(outPath);
		InputStream myInput = context.getAssets().open(fileName);
		byte[] buffer = new byte[BUFFER_SIZE];
		int length = myInput.read(buffer);
		while (length > 0) {
			myOutput.write(buffer, 0, length);
			length = myInput.read(buffer);
		}

		myOutput.flush();
		myInput.close();
		myOutput.close();
	}


	/**
     * 复制单个文件
     * @param source String 原文件路径 如：/xx
     * @param target String 复制后路径 如：/xx/ss
     * @return boolean
     */
	public static boolean copyFile(String source, String target)
	{
		try
		{
			int bytesum = 0;
			int byteread = 0;
			String f_new = "";
			File sourceFile = new File(source);
			if (target.endsWith(File.separator))
			{
				f_new = target + sourceFile.getName();
			}
			else
			{
				f_new = target + File.separator + sourceFile.getName();
			}
			File tarFile = new File(target);
    		tarFile.mkdirs(); // 如果文件夹不存在 则建立新文件夹
			new File(f_new).createNewFile(); // 如果文件不存在 则建立新文件
			// 文件存在时
			if (sourceFile.exists())
			{
				InputStream inStream = new FileInputStream(source); // 读入原文件
				FileOutputStream fs = new FileOutputStream(f_new);
				byte[] buffer = new byte[BUFFER_SIZE];
				while ((byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread; // 字节数 文件大小
					fs.write(buffer, 0, byteread);
				}
				fs.close();
				inStream.close();
			}
		}
		catch (Exception e)
		{
			return false;

		}
		return true;
	}
	
	public static boolean copyFile2(String source, String target)
	{
		return bufferedReaderCopy(source, target);
	}
	
    private static boolean bufferedReaderCopy(String source, String target) {
    	
        Reader fin = null;
        Writer fout = null;
        try {
        	
        	String f_new = "";
    		File sourceFile = new File(source);
    		if (target.endsWith(File.separator))
    		{
    			f_new = target + sourceFile.getName();
    		}
    		else
    		{
    			f_new = target + File.separator + sourceFile.getName();
    		}
    		File tarFile = new File(target);
    		tarFile.mkdirs(); // 如果文件夹不存在 则建立新文件夹
    		new File(f_new).createNewFile(); // 如果文件不存在 则建立新文件
        	
            fin = new BufferedReader(new FileReader(source));
            fout = new BufferedWriter(new FileWriter(f_new));

            int c;
            while ((c = fin.read()) != -1) {
                fout.write(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            close(fin);
            close(fout);
        }
        
        return true;
    }
    
	private static void close(Closeable closable) {
		if (closable != null) {
			try {
				closable.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
   

	
    /**
     * 移动文件到指定目录
     * @param oldPath String 如：/fqf.txt
     * @param newPath String 如：/xx/fqf.txt
     */
	public static boolean moveFile(String oldPath, String newPath)
	{
		boolean ret = false;
		try
		{
			if (copyFile(oldPath, newPath))
			{
				new File(oldPath).delete();
				ret = true;
			}
		}
		catch (Exception e)
		{
			return false;
		}
		return ret;
	}

	
	private static final String[] STRING_CHECK = new String[] { "\\"/*, "/", ":", "*", "?", "\"", "<", ">", "|" */};
    
	/**
	 * 校验输入的文件名称是否合法
	 * 文件名不能包含任何以下字符：
	 * \ / : * ? " < > |
	 * @param newName
	 * @return 是否可以用于文件名
	 */
	public static boolean checkFilePath(String newName)
	{
		for (String str : STRING_CHECK)
		{
			if (newName.indexOf(str) != -1)
			{
				return false;
			}
		}
		return true;
	}

	public static long[] getStatFs(String path)
	{
		long[] ret = new long[]{0, 0, 0};
		StatFs statfs = new StatFs(path);
		// 获取SDCard上BLOCK总数
		long blockCount = 0;
		// 获取SDCard上每个block的SIZE
		long blockSize = 0;
		// 获取可供程序使用的Block的数量
		long availableBlocks = 0;
		// 获取剩下的所有Block的数量(包括预留的一般程序无法使用的块)
		long freeBlocks = 0;
		
		long availableBytes = 0;
		long freeBytes = 0;
		long totalBytes = 0;
		
		if(VERSION.SDK_INT >= 18)
		{
			blockCount = statfs.getBlockCountLong();
			blockSize = statfs.getBlockSizeLong();
			availableBlocks = statfs.getAvailableBlocksLong();
			freeBlocks = statfs.getFreeBlocksLong();

			totalBytes = statfs.getTotalBytes();
			availableBytes = statfs.getAvailableBytes();
			freeBytes = statfs.getFreeBytes();
			ret[0] = totalBytes;
			ret[1] = availableBytes;
			ret[2] = totalBytes - availableBytes;
		}
		else
		{
			blockCount = statfs.getBlockCount();
			blockSize = statfs.getBlockSize();
			availableBlocks = statfs.getAvailableBlocks();
			freeBlocks = statfs.getFreeBlocks();
		}
		
		Log.i(TAG, "blockCount:" + blockCount);
		Log.i(TAG, "blockSize:" + blockSize);
		Log.i(TAG, "availableBlocks:" + availableBlocks);
		Log.i(TAG, "freeBlocks:" + freeBlocks);
		Log.i(TAG, "availableBytes:" + availableBytes);
		Log.i(TAG, "freeBytes:" + freeBytes);
		Log.i(TAG, "totalBytes:" + totalBytes);
		
		if(VERSION.SDK_INT >= 18)
		{
			Log.i(TAG, "总容量:"+totalBytes/1024/1024+" MB"+" 剩余:"+availableBytes/1024/1024+" MB");
		}
		else
		{
			// 计算SDCard 总容量大小MB
			long nSDTotalSize = blockCount * blockSize / 1024 / 1024;
			// 计算 SDCard 剩余大小MB
			long nSDFreeSize = availableBlocks * blockSize / 1024 / 1024;

			ret[0] = blockCount * blockSize;
			ret[1] = availableBlocks * blockSize;
			ret[2] = blockCount * blockSize - availableBlocks * blockSize;
			
			Log.i(TAG, "SDCard 总容量:"+nSDTotalSize+" MB"+" 剩余:"+nSDFreeSize+" MB");
		}

		return ret;
		// return "SDCard 总容量:"+nSDTotalSize+" MB"+" 剩余:"+nSDFreeSize+" MB";
	}

	public static float getStatFsProgress(String path)
	{
		StatFs statfs = new StatFs(path);
		// 获取SDCard上BLOCK总数
		long blockCount = 0;
		// 获取SDCard上每个block的SIZE
		long blockSize = 0;
		// 获取可供程序使用的Block的数量
		long availableBlocks = 0;
		// 获取剩下的所有Block的数量(包括预留的一般程序无法使用的块)
		long freeBlocks = 0;

		long availableBytes = 0;
		long freeBytes = 0;
		long totalBytes = 0;

		if(VERSION.SDK_INT >= 18)
		{
			blockCount = statfs.getBlockCountLong();
			blockSize = statfs.getBlockSizeLong();
			availableBlocks = statfs.getAvailableBlocksLong();
			freeBlocks = statfs.getFreeBlocksLong();

			availableBytes = statfs.getAvailableBytes();
			freeBytes = statfs.getFreeBytes();
			totalBytes = statfs.getTotalBytes();
		}
		else
		{
			blockCount = statfs.getBlockCount();
			blockSize = statfs.getBlockSize();
			availableBlocks = statfs.getAvailableBlocks();
			freeBlocks = statfs.getFreeBlocks();
		}

		float progress = 0;
		if(VERSION.SDK_INT >= 18)
		{
			progress = 1f * (totalBytes - availableBytes) / totalBytes;
		}
		else
		{
			// 计算SDCard 总容量大小MB
			long nSDTotalSize = blockCount * blockSize / 1024 / 1024;
			// 计算 SDCard 剩余大小MB
			long nSDFreeSize = availableBlocks * blockSize / 1024 / 1024;

			progress = 1f * (nSDTotalSize - nSDFreeSize) / nSDTotalSize;
		}
		return progress;
	}
	
	public static void getStatFs()
	{
		// 取得SDCard当前的状态
		if (Utility.isExternalStorageMounted())
		{
			// 取得sdcard文件路径
			File pathFile = Environment.getExternalStorageDirectory();
			StatFs statfs = new StatFs(pathFile.getPath());
			// 获取SDCard上BLOCK总数
			long blockCount = 0;
			// 获取SDCard上每个block的SIZE
			long blockSize = 0;
			// 获取可供程序使用的Block的数量
			long availableBlocks = 0;
			// 获取剩下的所有Block的数量(包括预留的一般程序无法使用的块)
			long freeBlocks = 0;
			
			
			long availableBytes = 0;
			long freeBytes = 0;
			long totalBytes = 0;
			
			if(VERSION.SDK_INT >= 18)
			{
				blockCount = statfs.getBlockCountLong();
				blockSize = statfs.getBlockSizeLong();
				availableBlocks = statfs.getAvailableBlocksLong();
				freeBlocks = statfs.getFreeBlocksLong();
				
				availableBytes = statfs.getAvailableBytes();
				freeBytes = statfs.getFreeBytes();
				totalBytes = statfs.getTotalBytes();
			}
			else
			{
				blockCount = statfs.getBlockCount();
				blockSize = statfs.getBlockSize();
				availableBlocks = statfs.getAvailableBlocks();
				freeBlocks = statfs.getFreeBlocks();
			}
			
			Log.i(TAG, "blockCount:" + blockCount);
			Log.i(TAG, "blockSize:" + blockSize);
			Log.i(TAG, "availableBlocks:" + availableBlocks);
			Log.i(TAG, "freeBlocks:" + freeBlocks);
			Log.i(TAG, "availableBytes:" + availableBytes);
			Log.i(TAG, "freeBytes:" + freeBytes);
			Log.i(TAG, "totalBytes:" + totalBytes);
			
			if(VERSION.SDK_INT >= 18)
			{
				Log.i(TAG, "SDCard 总容量:"+totalBytes/1024/1024+" MB"+" 剩余:"+availableBytes/1024/1024+" MB");
			}
			else
			{
				// 计算SDCard 总容量大小MB
				long nSDTotalSize = blockCount * blockSize / 1024 / 1024;
				// 计算 SDCard 剩余大小MB
				long nSDFreeSize = availableBlocks * blockSize / 1024 / 1024;
				
				Log.i(TAG, "SDCard 总容量:"+nSDTotalSize+" MB"+" 剩余:"+nSDFreeSize+" MB");
			}
			
			// return "SDCard 总容量:"+nSDTotalSize+" MB"+" 剩余:"+nSDFreeSize+" MB";
		}
		else
		{
			// "SDCard 不存在";
		}
	}
	
	/**
	 * 
	 * @param src
	 * @return
	 */
	public static String bytesToHexString(byte[] src)
	{
		if (src == null || src.length < 1)
		{
			return null;
		}
		StringBuilder strBuilder = new StringBuilder();
		for (int i = 0; i < src.length; i++)
		{
			int v = src[i] & 0xFF;
			String strhs = Integer.toHexString(v);
			if (strhs.length() < 2)
			{
				strBuilder.append(0);
			}
			strBuilder.append(strhs);
		}
		return strBuilder.toString();
	}


	
	public static File getParentFile(String path)
	{
		File tmpFile = null;
		try
		{
			tmpFile = new File(path);
			if (tmpFile != null && tmpFile.exists())
			{
				return tmpFile.getParentFile();
			}
			else
			{
				return null;
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tmpFile;
	}



	
	public static boolean isFile(String path)
	{
		File tmpFile = null;
		try
		{
			tmpFile = new File(path);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tmpFile != null && tmpFile.exists();
	}

	public static boolean isDirectory(String path)
	{
		File tmpFile = null;
		try
		{
			tmpFile = new File(path);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tmpFile != null && tmpFile.exists() && tmpFile.isDirectory();
	}
	
	public static boolean isCanReadFile(String path)
	{
		File tmpFile = null;
		try
		{
			tmpFile = new File(path);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tmpFile != null && tmpFile.exists() && tmpFile.canRead();
	}





	public static boolean isSymlink(File file)
	{
		try
		{
			if (file == null)
			{
				return false;
			}
			File canon;
			if (file.getParent() == null)
			{
				canon = file;
			}
			else
			{
				File canonDir = file.getParentFile().getCanonicalFile();
				canon = new File(canonDir, file.getName());
			}
			File file1 = canon.getCanonicalFile();
			File file2 = canon.getAbsoluteFile();
//			String file1Path = file1.getCanonicalPath();
//			String file2Path = file2.getAbsolutePath();
//			Log.e(TAG, "file:" + file.getAbsolutePath() + "    file1:" + file1Path + "   file2: " + file2Path);
			return !file1.equals(file2);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	public static String getSymlink(File file)
	{
		String linkPath = null;

		try
		{
			if (file == null)
			{
				return linkPath;
			}
			File canon;
			if (file.getParent() == null)
			{
				canon = file;
			}
			else
			{
				File canonDir = file.getParentFile().getCanonicalFile();
				canon = new File(canonDir, file.getName());
			}
			File file1 = canon.getCanonicalFile();
			String file1Path = file1.getCanonicalPath();
			// Log.e(TAG, "file:" + file.getAbsolutePath() + "    file1:" + file1Path + "   file2: "
			// + file2Path);
			return file1Path;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return linkPath;
	}

	public static boolean isInCacheDir(String absPath, Context context) {
		if (context != null && !TextUtils.isEmpty(absPath)) {
			String pkg = context.getPackageName();
			File cacheDir = null;
			cacheDir = context.getExternalCacheDir();
			if (cacheDir != null) {
				String path = cacheDir.getAbsolutePath();// /storage/emulated/0/Android/data/com.folderv.file/cache
				if (path.contains(pkg)) {
					path = path.substring(0, path.indexOf(pkg));// /storage/emulated/0/Android/data/
				}
				if (absPath.startsWith(path)) {
					return true;
				}
			}
			cacheDir = context.getCacheDir();
			if (cacheDir != null) {
				String path = cacheDir.getAbsolutePath();//   /data/data/com.folderv.file/cache
				if (path.contains(pkg)) {
					path = path.substring(0, path.indexOf(pkg));// data/data/
				}
				if (absPath.startsWith(path)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 如果有缩略图,返回缩略图路径
	 *
	 * @param context
	 * @param path
	 * @return
	 */
	public static String getThumbnailPath(Context context, String path) {
		long time = System.currentTimeMillis();
		String thumbnail = null;
		if (path == null) {
			return thumbnail;
		}
		if (context == null) {
			return thumbnail;
		}

		ContentResolver cr = context.getContentResolver();
		//TODO path may be Symlink path

		int origId = Integer.MIN_VALUE;
		int thumbId = Integer.MIN_VALUE;
		String[] args = new String[]{path};
		Cursor cs = null;
		try {
			cs = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.MINI_THUMB_MAGIC},
					MediaStore.Images.Media.DATA + "= ?",
					args,
					null);

			if (cs != null && cs.moveToFirst()) {
				origId = cs.getInt(cs.getColumnIndex(MediaStore.Images.Media._ID));
				thumbId = cs.getInt(cs.getColumnIndex(MediaStore.Images.Media.MINI_THUMB_MAGIC));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cs != null) {
				cs.close();
				cs = null;
			}
		}


		if (origId != Integer.MIN_VALUE || thumbId != Integer.MIN_VALUE) {
			try {
				args = new String[]{String.valueOf(origId), String.valueOf(thumbId)};
				cs = cr.query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
						new String[]{MediaStore.Images.Thumbnails.DATA}, // Which columns to return
						MediaStore.Images.Thumbnails.IMAGE_ID + " = ? or " + MediaStore.Images.Thumbnails._ID + " = ?",
						args,
						null);
				if (cs != null && cs.moveToFirst()) {
					thumbnail = cs.getString(cs.getColumnIndex(MediaStore.Images.Thumbnails.DATA));
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cs != null) {
					cs.close();
					cs = null;
				}
			}
		}
		Log.e(TAG, "getThumbnailPath time:" + (System.currentTimeMillis() - time) + ", " + path + ">>" + thumbnail);
		return thumbnail;
	}

	public static Bitmap getArtwork(Context context, long song_id, long album_id) {
		if (album_id < 0) {
			// This is something that is not in the database, so get the album art directly
			// from the file.
			if (song_id >= 0) {
				Bitmap bm = getArtworkFromFile(context, song_id, -1);
				if (bm != null) {
					return bm;
				}
			}
			return null;
		}
		ContentResolver res = context.getContentResolver();
		Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
		if (uri != null) {
			InputStream in = null;
			try {
				in = res.openInputStream(uri);
				return BitmapFactory.decodeStream(in, null, sBitmapOptions);
			} catch (FileNotFoundException ex) {
				// The album art thumbnail does not actually exist. Maybe the user deleted it, or
				// maybe it never existed to begin with.
				Bitmap bm = getArtworkFromFile(context, song_id, album_id);
				if (bm != null) {
					if (bm.getConfig() == null) {
						bm = bm.copy(Bitmap.Config.RGB_565, false);
						if (bm == null) {
							return null;
						}
					}
				}
				return bm;
			}
			catch(SecurityException e)
			{
				e.printStackTrace();
			}
			finally {
				try {
					if (in != null) {
						in.close();
					}
				} catch (IOException ex) {
				}
			}
		}

		return null;
	}

	private static Bitmap getArtworkFromFile(Context context, long songid, long albumid) {
		Bitmap bm = null;
		byte[] art = null;
		String path = null;
		if (albumid < 0 && songid < 0) {
			throw new IllegalArgumentException("Must specify an album or a song id");
		}
		try {
			if (albumid < 0) {
				Uri uri = Uri.parse("content://media/external/audio/media/" + songid + "/albumart");
				ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
				if (pfd != null) {
					FileDescriptor fd = pfd.getFileDescriptor();
					bm = BitmapFactory.decodeFileDescriptor(fd);
				}
			} else {
				Uri uri = ContentUris.withAppendedId(sArtworkUri, albumid);
				ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
				if (pfd != null) {
					FileDescriptor fd = pfd.getFileDescriptor();
					bm = BitmapFactory.decodeFileDescriptor(fd);
				}
			}
		} catch (FileNotFoundException ex) {

		}

		return bm;
	}

	private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
	private static final BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();


	/**
	 * Parses AndroidManifest of the given apkFile and returns the value of
	 * minSdkVersion using undocumented API which is marked as
	 * "not to be used by applications"
	 *
	 * @param apkFile
	 * @return minSdkVersion or -1 if not found in Manifest
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	@Deprecated
	public static int getMinSdkVersion(File apkFile) throws IOException,
			XmlPullParserException {

		XmlResourceParser parser = getParserForManifest(apkFile);
		while (parser.next() != XmlPullParser.END_DOCUMENT) {

			if (parser.getEventType() == XmlPullParser.START_TAG
					&& parser.getName().equals("uses-sdk")) {
				for (int i = 0; i < parser.getAttributeCount(); i++) {
					if (parser.getAttributeName(i).equals("minSdkVersion")) {
						return parser.getAttributeIntValue(i, -1);
					}
				}
			}
		}
		return -1;

	}

	/**
	 * Tries to get the parser for the given apkFile from {@link AssetManager}
	 * using undocumented API which is marked as
	 * "not to be used by applications"
	 *
	 * @param apkFile
	 * @return
	 * @throws IOException
	 */
	private static XmlResourceParser getParserForManifest(final File apkFile)
			throws IOException {
		final Object assetManagerInstance = getAssetManager();
		final int cookie = addAssets(apkFile, assetManagerInstance);
		return ((AssetManager) assetManagerInstance).openXmlResourceParser(
				cookie, "AndroidManifest.xml");
	}

	/**
	 * Get the cookie of an asset using an undocumented API call that is marked
	 * as "no to be used by applications" in its source code
	 *
	 * @see <a
	 *      href="http://androidxref.com/5.1.1_r6/xref/frameworks/base/core/java/android/content/res/AssetManager.java#612">AssetManager.java#612</a>
	 * @return the cookie
	 */
	private static int addAssets(final File apkFile,
								 final Object assetManagerInstance) {
		try {
			Method addAssetPath = assetManagerInstance.getClass().getMethod(
					"addAssetPath", new Class[] { String.class });
			return (Integer) addAssetPath.invoke(assetManagerInstance,
					apkFile.getAbsolutePath());
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * Get {@link AssetManager} using reflection
	 *
	 * @return
	 */
	private static Object getAssetManager() {
		Class assetManagerClass = null;
		try {
			assetManagerClass = Class
					.forName("android.content.res.AssetManager");
			Object assetManagerInstance = assetManagerClass.newInstance();
			return assetManagerInstance;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getFileMimeType(String filename) {
		if (TextUtils.isEmpty(filename)) {
			return null;
		}
		int lastDotIndex = filename.lastIndexOf('.');
		String mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
				filename.substring(lastDotIndex + 1).toLowerCase());
		//Log.i(TAG, "getFileMimeType mimeType = " + mimetype);
		return mimetype;
	}


}
