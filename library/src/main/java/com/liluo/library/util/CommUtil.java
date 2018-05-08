package com.liluo.library.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@SuppressLint("NewApi")
public class CommUtil {
	private static SimpleDateFormat sf                                = null;
	private static SimpleDateFormat DATE_FORMAT_TILL_SECOND           = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static SimpleDateFormat DATE_FORMAT_TILL_DAY_CURRENT_YEAR = new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat DATE_FORMAT_TILL_DAY_CH           = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * 获得屏幕宽度
	 * 
	 * @param context
	 * @return
	 */
	public static int getScreenWidth(Context context) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);
		return outMetrics.widthPixels;
	}
	/**
	 * bitmap转为base64
	 * @param bitmap
	 * @return
	 */
	public static String bitmapToBase64(Bitmap bitmap) {

		String result = null;
		ByteArrayOutputStream baos = null;
		try {
			if (bitmap != null) {
				baos = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
				baos.flush();
				baos.close();
				byte[] bitmapBytes = baos.toByteArray();
				result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (baos != null) {
					baos.flush();
					baos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	/**
	 * base64转为bitmap
	 * @param base64Data
	 * @return
	 */
	public static Bitmap base64ToBitmap(String base64Data) {
		byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
		return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
	}

	/**
	 * 关闭软键盘
	 * 
	 * @param context
	 * @param activity
	 */
	public static void changeKeybroad(Context context, Activity activity) {
		InputMethodManager inputMethodManager = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);

	}
	/**
	 * 判断系统时间是否为24小时制
	 * 
	 * @param ctx
	 * @return
	 * @author liwei
	 * @创建时间 2016-8-1
	 */
	public static boolean is24(Context ctx) {
		ContentResolver cv = ctx.getContentResolver();
		String strTimeFormat = android.provider.Settings.System.getString(cv,
				android.provider.Settings.System.TIME_12_24);
		if (strTimeFormat != null && strTimeFormat.equals("24")) {// strTimeFormat某些rom12小时制时会返回null
			return true;
		} else
			return false;
	}

	/**
	 * 计算两点间距离
	 * 
	 * @param startCoord
	 *            开始坐标
	 * @param endCoord
	 *            结束坐标
	 * @return
	 * @author liwei
	 * @创建时间 2016-7-27
	 */
	public static double getDistanceFromXtoY(String startCoord, String endCoord) {

		double lat_a = Double.parseDouble(startCoord.split(",")[0]);
		double lng_a = Double.parseDouble(startCoord.split(",")[1]);
		double lat_b = Double.parseDouble(endCoord.split(",")[0]);
		double lng_b = Double.parseDouble(endCoord.split(",")[1]);

		double pk = 180 / 3.14169;
		double a1 = lat_a / pk;
		double a2 = lng_a / pk;
		double b1 = lat_b / pk;
		double b2 = lng_b / pk;
		double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
		double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
		double t3 = Math.sin(a1) * Math.sin(b1);
		double tt = Math.acos(t1 + t2 + t3);
		return 6366000 * tt;
	}
	/**
	 * 过滤特殊字符
	 * 
	 * @param str
	 * @return
	 * @throws PatternSyntaxException
	 * @author liwei
	 * @创建时间 2016-6-22
	 */
	public static String StringFilter(String str) throws PatternSyntaxException {
		// 只允许字母和数字
		// String regEx = "[^a-zA-Z0-9]";
		// 清除掉所有特殊字符
		String regEx = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……& amp;*（）——+|{}【】‘；：”“’。，、？]";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(str);
		return m.replaceAll("");
	}

	public static boolean checkApkExist(Context context, String packageName) {
		if (packageName == null || "".equals(packageName))
			return false;
		try {
			ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName,
					PackageManager.GET_UNINSTALLED_PACKAGES);
			return true;
		} catch (NameNotFoundException e) {
			return false;
		}
	}

	/**
	 * 设置边距
	 * 
	 * @param v
	 * @param l
	 * @param t
	 * @param r
	 * @param b
	 */
	public static void setMargins(View v, int l, int t, int r, int b) {
		if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
			ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
			p.setMargins(l, t, r, b);
			v.requestLayout();
		}
	}

	/**
	 * 判断两个集合是否相等
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static <T extends Comparable<T>> boolean compare(List<T> a, List<T> b) {
		if (a.size() != b.size())
			return false;
		Collections.sort(a);
		Collections.sort(b);
		for (int i = 0; i < a.size(); i++) {
			if (!a.get(i).equals(b.get(i)))
				return false;
		}
		return true;
	}
	/**
	 * 手机号码中间*显示
	 * 
	 * @return
	 */
	public static String replecePhone(String phone) {
		String str = phone;
		String ss = str.substring(0, str.length() - (str.substring(3)).length()) + "****" + str.substring(7);
		return ss;
	}

	/**
	 * 获得屏幕高度
	 * 
	 * @param context
	 * @return
	 */
	public static int getScreenHeight(Context context) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);
		return outMetrics.heightPixels;
	}

	/**
	 * 比较两个数大小
	 * 
	 * @param val1
	 *            可提现金额
	 * @param val2
	 *            提现金额
	 * @return
	 */
	public static boolean compareDouble(BigDecimal val1, BigDecimal val2) {
		boolean result = false;
		if (val1.compareTo(val2) < 0) {
			result = true;
		}
		if (val1.compareTo(val2) == 0) {
			result = true;
		}
		if (val1.compareTo(val2) > 0) {
			result = false;
		}
		return result;
	}

	private static String mYear;
	private static String mMonth;
	private static String mDay;
	private static String mWay;

	/**
	 * 获取当前日期和周几
	 * 
	 * @return
	 * @author liwei
	 * @创建时间 2016-6-4
	 */
	public static String getStringData(int type) {
		Date date=new Date();//取时间
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.add(calendar.DATE,type);//把日期往后增加一天.整数往后推,负数往前移动
		date=calendar.getTime(); //这个时间就是日期往后推一天的结果
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
		String dateString = formatter.format(date);
		// return mYear + "年" + mMonth + "月" + mDay+"日"+"/星期"+mWay;
		return dateString;
	}

	/**
	 * 根据时间判断是否清除缓存地址（缓存时间为5分钟）
	 * 
	 * @param hqtime
	 *            缓存地址时间戳
	 * @return
	 */
	public static boolean isClearCacheTimestamp(Long hqtime) {
		boolean result;
		if (hqtime == 0) {
			result = false;
		} else {
			Long s = (System.currentTimeMillis() - hqtime) / (1000 * 60);
			if (s >= 5) {
				result = true;
			} else {
				result = false;
			}
		}
		return result;

	}

	/**
	 * Resize the bitmap
	 * 
	 * @param bitmap
	 * @param width
	 * @param height
	 * @return
	 */
	public static Bitmap zoomBitmap(Bitmap bitmap, int width, int height) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		Matrix matrix = new Matrix();
		float scaleWidth = ((float) width / w);
		float scaleHeight = ((float) height / h);
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
		return newbmp;
	}

	/**
	 * 超过万转换
	 * 
	 * @param data
	 * @return
	 */
	public static String Wan(String data) {
		String str = "";
		if (Long.parseLong(data) < 10000) {
			str = numberWithDelimiter(data).toString();
		} else {
			double n = Double.parseDouble(data) / 10000;
			str = Double.toString(n) + "万";
		}
		return str;
	}

	/**
	 * 数字转为钱
	 * 
	 * @param str
	 * @return
	 */
	public static BigDecimal numberWithDelimiter(String str) {
		BigDecimal bd = new BigDecimal(str);
		// 设置小数位数，第一个变量是小数位数，第二个变量是取舍方法(四舍五入)
		bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
		return bd;
	}

	/**
	 * 验证手机格式
	 */
	public static boolean isMobileNO(String mobiles) {
		/*
		 * 移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
		 * 联通：130、131、132、152、155、156、185、186 电信：133、153、180、189、（1349卫通）
		 * 总结起来就是第一位必定为1，第二位必定为3或5或8,7，其他位置的可以为0-9
		 */
		String telRegex = "[1][34587]\\d{9}";// "[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
		if (TextUtils.isEmpty(mobiles))
			return false;
		else
			return mobiles.matches(telRegex);
	}

	/**
	 * 验证邮箱
	 * 
	 * @param email
	 * @return
	 */
	public static boolean checkEmail(String email) {
		boolean flag = false;
		try {
			String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
			Pattern regex = Pattern.compile(check);
			Matcher matcher = regex.matcher(email);
			flag = matcher.matches();
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}

	/**
	 * 验证密码
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isPasswordType(String str) {
		boolean isSucess;
		boolean includeNum = false;// 包含数字

		boolean includeLetter = false;// 包含字母
		boolean includeOther = false;// 包含其它字符
		String reg = "^[^\\s]{6,16}$";
		if (str.trim().matches(reg)) {
			// isSucess=true;
			for (int i = 0; i < str.length(); i++) {
				char c = str.charAt(i);
				if (c >= '0' && c <= '9') {
					includeNum = true;
				} else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
					includeLetter = true;
				} else {
					includeOther = true;
				}
			}
			if ((includeNum == true && includeLetter == true) || (includeNum == true && includeOther == true)
					|| (includeOther == true && includeLetter == true)
					|| (includeLetter == true && includeNum == true && includeOther == true)) {
				isSucess = true;
			} else {
				isSucess = false;
			}

		} else {
			isSucess = false;
		}
		return isSucess;
	}

	public final static String get32MD5Str(String str) {
		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.reset();
			messageDigest.update(str.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException e) {
			System.out.println("NoSuchAlgorithmException caught!");
			System.exit(-1);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		byte[] byteArray = messageDigest.digest();
		StringBuffer md5StrBuff = new StringBuffer();
		for (int i = 0; i < byteArray.length; i++) {
			if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
				md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
			else
				md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
		}
		return md5StrBuff.toString();
	}

	/**
	 * 获取当前时间戳
	 * 
	 * @return
	 */
	public static String getCurrentDate() {
		Long time = System.currentTimeMillis() / 1000;
		return time.toString();
	}

	/**
	 * 获取count个随机数
	 * 
	 * @param str
	 *            随机数个数
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static String getRandom(String str) throws NoSuchAlgorithmException {
		String sb = get32MD5Str(str);
		return sb;
	}

	public final static String ALBUM_PATH = Environment.getExternalStorageDirectory() + File.separator + "epco2/";

	/*
	 * 保存文件
	 */
	public static String saveFile(Bitmap bm, String fileName) throws IOException {
		String path;
		File dirFile = new File(ALBUM_PATH);
		if (!dirFile.exists()) {
			dirFile.mkdir();
		}
		File myCaptureFile = new File(ALBUM_PATH + fileName);
		path = myCaptureFile.getAbsolutePath();
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		int options = 100;
		while (baos.toByteArray().length / 1024 > 100) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
			baos.reset();// 重置baos即清空baos
			options -= 10;// 每次都减少10
			bm.compress(CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
		bitmap.compress(CompressFormat.JPEG, 100, bos);
		bos.flush();
		bos.close();
		return path;
	}

	/**
	 * 时间转时间戳
	 * @param s
	 * @return
	 * @throws ParseException
	 */
	public static String dateToStamp(String s) throws ParseException {
		String res;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = simpleDateFormat.parse(s);
		long ts = date.getTime();
		res = String.valueOf(ts);
		return res;
	}

	/**
	 * 图片按比例大小压缩方法（根据Bitmap图片压缩）：
	 * 
	 * @param image
	 * @return
	 * @author liwei
	 * @创建时间 2016-6-24
	 */
	public static Bitmap comp(Bitmap image) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(CompressFormat.JPEG, 100, baos);
		if (baos.toByteArray().length / 1024 > 1024) {// 判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
			baos.reset();// 重置baos即清空baos
			image.compress(CompressFormat.JPEG, 50, baos);// 这里压缩50%，把压缩后的数据存放到baos中
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		// 开始读入图片，此时把options.inJustDecodeBounds 设回true了
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		// 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
		float hh = 800f;// 这里设置高度为800f
		float ww = 480f;// 这里设置宽度为480f
		// 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
		int be = 1;// be=1表示不缩放
		if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
			be = (int) (newOpts.outWidth / ww);
		} else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
			be = (int) (newOpts.outHeight / hh);
		}
		if (be <= 0)
			be = 1;
		newOpts.inSampleSize = be;// 设置缩放比例
		// 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
		isBm = new ByteArrayInputStream(baos.toByteArray());
		bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
		return compressImage(bitmap);// 压缩好比例大小后再进行质量压缩
	}

	public static Bitmap compressImage(Bitmap image) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		int options = 100;
		while (baos.toByteArray().length / 1024 > 100) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
			baos.reset();// 重置baos即清空baos
			image.compress(CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
			options -= 10;// 每次都减少10
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
		return bitmap;
	}

	/**
	 * 设置文件名称
	 * 
	 * @return
	 */
	public static String setImageName() {
		String fileName = "";
		String str = null;
		Date date = null;
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");// 获取当前时间，进一步转化为字符串
		date = new Date();
		str = format.format(date);
		return fileName = str + ".jpg";
	}

	/**
	 * 日期字符串转换为Date
	 * 
	 * @param dateStr
	 * @param format
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	public static Date strToDate(String dateStr, String format) {
		Date date = null;

		if (!TextUtils.isEmpty(dateStr)) {
			DateFormat df = new SimpleDateFormat(format);
			try {
				date = df.parse(dateStr);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return date;
	}

	final static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	/**
	 * 日期逻辑
	 * 
	 * @param dateStr
	 *            日期字符串
	 * @return
	 */
	public static String timeLogic(String dateStr) {
		Calendar calendar = Calendar.getInstance();
		calendar.get(Calendar.DAY_OF_MONTH);
		long now = calendar.getTimeInMillis();
		if (dateStr != null || dateStr.length() > 0) {
			Date date = strToDate(dateStr, DATE_FORMAT);
			calendar.setTime(date);
			long past = calendar.getTimeInMillis();

			// 相差的秒数
			long time = (now - past) / 1000;

			StringBuffer sb = new StringBuffer();
			if (time > 0 && time < 60) { // 1小时内
				return sb.append(time + "秒前").toString();
			} else if (time > 60 && time < 3600) {
				return sb.append(time / 60 + "分钟前").toString();
			} else if (time >= 3600 && time < 3600 * 24) {
				return sb.append(time / 3600 + "小时前").toString();
			} else if (time >= 3600 * 24 && time < 3600 * 48) {
				return sb.append("昨天").toString();
			} else if (time >= 3600 * 48 && time < 3600 * 72) {
				return sb.append("前天").toString();
			} else if (time >= 3600 * 72) {
				return dateToString(dateStr, DATE_FORMAT);
			}
			return dateToString(dateStr, DATE_FORMAT);
		} else {

		}
		return "";
	}

	private static String dateToString(String dateStr, String dateFormat) {
		Date date = strToDate(dateStr, dateFormat);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		// 如果是今年的话，才去“xx月xx日”日期格式
		if (calendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) {
			return DATE_FORMAT_TILL_DAY_CURRENT_YEAR.format(date);
		}

		return DATE_FORMAT_TILL_DAY_CH.format(date);
	}

	/**
	 * 压缩图片质量
	 * 
	 * @param uri
	 *            图片uri
	 * @return 压缩完成的图片路径
	 */
	public synchronized static String compressImage(String uri) {
		Log.e("liluo", "压缩前:" + new File(uri).length());
		if (new File(uri).length() <= 300 * 1024) {
			return uri;
		}

		try {
			String imageType = getImageType(uri).toLowerCase();
			String compressPath = ALBUM_PATH + (Math.random() * 10000) + imageType;

			FileOutputStream out = new FileOutputStream(compressPath);

			if (imageType.equals(".jpg") || imageType.equals(".jpeg")) {
				System.out.println("compress jpg: ");
				Bitmap result = BitmapFactory.decodeFile(uri);
				result.compress(CompressFormat.JPEG, 30, out);
				out.close();
				System.out.println("压缩后： " + new File(compressPath).length());
				return compressPath;
			} else if (imageType.equals(".png")) {
				System.out.println("compress png: ");
				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inJustDecodeBounds = true;
				opts.inSampleSize = 3;
				BitmapFactory.decodeFile(uri, opts);
				opts.inJustDecodeBounds = false;
				Bitmap result = BitmapFactory.decodeFile(uri, opts);
				result.compress(CompressFormat.PNG, 100, out);
				out.close();
				System.out.println("压缩后： " + new File(compressPath).length());
				return compressPath;
			}
			out.close();
			return compressPath;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return uri;
	}
	public static String saveCroppedImage(Bitmap bmp, long time) {
		File file = new File(ALBUM_PATH);
		if (!file.exists())
			file.mkdir();
		// /sdcard/myFolder/temp_cropped.jpg
		String newFilePath = ALBUM_PATH + time + ".jpg";
		file = new File(newFilePath);
		try {
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			bmp.compress(CompressFormat.JPEG, 50, fos);
			fos.flush();
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return newFilePath;

	}

	/**
	 * 获取图片格式
	 * 
	 * @param uri
	 * @return
	 */
	private synchronized static String getImageType(String uri) {
		String[] array = uri.split("\\.");
		return "." + array[array.length - 1].toLowerCase();
	}

	/**
	 * 设置listview高度，解决与scrollview冲突
	 * 
	 * @param listView
	 */
	public static void setListViewHeightBasedOnChildren(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			return;
		}

		int totalHeight = 0;
		for (int i = 0; i < listAdapter.getCount(); i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		listView.setLayoutParams(params);
	}

	public static void setGridViewHeightBasedOnChildren(GridView gridView) {
		// 获取GridView对应的Adapter
		ListAdapter listAdapter = gridView.getAdapter();
		if (listAdapter == null) {
			return;
		}
		int rows;
		int columns = 0;
		int horizontalBorderHeight = 0;
		Class<?> clazz = gridView.getClass();
		try {
			// 利用反射，取得每行显示的个数
			Field column = clazz.getDeclaredField("mRequestedNumColumns");
			column.setAccessible(true);
			columns = (Integer) column.get(gridView);
			// 利用反射，取得横向分割线高度
			Field horizontalSpacing = clazz.getDeclaredField("mRequestedHorizontalSpacing");
			horizontalSpacing.setAccessible(true);
			horizontalBorderHeight = (Integer) horizontalSpacing.get(gridView);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		// 判断数据总数除以每行个数是否整除。不能整除代表有多余，需要加一行
		if (listAdapter.getCount() % columns > 0) {
			rows = listAdapter.getCount() / columns + 1;
		} else {
			rows = listAdapter.getCount() / columns;
		}
		int totalHeight = 0;
		for (int i = 0; i < rows; i++) { // 只计算每项高度*行数
			View listItem = listAdapter.getView(i, null, gridView);
			listItem.measure(0, 0); // 计算子项View 的宽高
			totalHeight += listItem.getMeasuredHeight(); // 统计所有子项的总高度
		}
		ViewGroup.LayoutParams params = gridView.getLayoutParams();
		params.height = totalHeight + horizontalBorderHeight * (rows - 1);// 最后加上分割线总高度
		gridView.setLayoutParams(params);
	}

	/**
	 * 格式化单位
	 * 
	 * @param size
	 * @return
	 */
	public static String getFormatSize(double size) {
		double kiloByte = size / 1024;
		if (kiloByte < 1) {
			return size + "Byte";
		}

		double megaByte = kiloByte / 1024;
		if (megaByte < 1) {
			BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
			return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "KB";
		}

		double gigaByte = megaByte / 1024;
		if (gigaByte < 1) {
			BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
			return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "MB";
		}

		double teraBytes = gigaByte / 1024;
		if (teraBytes < 1) {
			BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
			return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "GB";
		}
		BigDecimal result4 = new BigDecimal(teraBytes);
		return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "TB";
	}

	public static String getCacheSize(File file) throws Exception {
		return getFormatSize(getFolderSize(file));
	}

	// 获取文件
	// Context.getExternalFilesDir() --> SDCard/Android/data/你的应用的包名/files/
	// 目录，一般放一些长时间保存的数据
	// Context.getExternalCacheDir() -->
	// SDCard/Android/data/你的应用包名/cache/目录，一般存放临时缓存数据
	public static long getFolderSize(File file) throws Exception {
		long size = 0;
		try {
			File[] fileList = file.listFiles();
			for (int i = 0; i < fileList.length; i++) {
				// 如果下面还有文件
				if (fileList[i].isDirectory()) {
					size = size + getFolderSize(fileList[i]);
				} else {
					size = size + fileList[i].length();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return size;
	}

	/**
	 * 检查手机上是否安装了指定的软件
	 * 
	 * @param context
	 * @param packageName
	 *            ：应用包名
	 * @return
	 */
	public static boolean isAvilible(Context context, String packageName) {
		// 获取packagemanager
		final PackageManager packageManager = context.getPackageManager();
		// 获取所有已安装程序的包信息
		List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
		// 用于存储所有已安装程序的包名
		List<String> packageNames = new ArrayList<String>();
		// 从pinfo中将包名字逐一取出，压入pName list中
		if (packageInfos != null) {
			for (int i = 0; i < packageInfos.size(); i++) {
				String packName = packageInfos.get(i).packageName;
				packageNames.add(packName);
			}
		}
		// 判断packageNames中是否有目标程序的包名，有TRUE，没有FALSE
		return packageNames.contains(packageName);
	}

	/**
	 * * 获取版本号 * @return 当前应用的版本号
	 * 
	 * @throws NameNotFoundException
	 */
	public static String getVersion(Context context) throws NameNotFoundException {

		PackageManager manager = context.getPackageManager();
		PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
		String version = info.versionName;
		return version;
	}

	private static double x_pi = 3.14159265358979324 * 3000.0 / 180.0;

	/**
	 * 将 GCJ-02 坐标转换成 BD-09 坐标 GoogleMap和高德map用的是同一个坐标系GCJ-02
	 */
	public static double[] bd_encrypt(double gg_lat, double gg_lon) {
		double bd_lat = 0.0;
		double bd_lon = 0.0;
		double location[] = new double[2];
		double x = gg_lon, y = gg_lat;
		double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * x_pi);
		double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * x_pi);
		bd_lon = z * Math.cos(theta) + 0.0065;
		bd_lat = z * Math.sin(theta) + 0.006;
		location[0] = bd_lat;
		location[1] = bd_lon;
		return location;
	}

	/**
	 * 将 BD-09 坐标转换成 GCJ-02 坐标 GoogleMap和高德map用的是同一个坐标系GCJ-02
	 */
	public static double[] bd_decrypt(double bd_lat, double bd_lon) {
		double gg_lat = 0.0;
		double gg_lon = 0.0;
		double location[] = new double[2];
		double x = bd_lon - 0.0065, y = bd_lat - 0.006;
		double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
		double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
		gg_lon = z * Math.cos(theta);
		gg_lat = z * Math.sin(theta);
		location[0] = gg_lat;
		location[1] = gg_lon;
		return location;
	}

	/**
	 * 判断应用是否处于活动状态
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isRunningApp(Context context, String packageName) {
		boolean isAppRunning = false;
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> list = am.getRunningTasks(100);
		for (RunningTaskInfo info : list) {
			if (info.topActivity.getPackageName().equals(packageName)
					&& info.baseActivity.getPackageName().equals(packageName)) {
				isAppRunning = true;
				// find it, break
				break;
			}
		}
		return isAppRunning;
	}

	/**
	 * 判断程序是否处于前台
	 * 
	 * @author 作者:liwei
	 * @version 创建时间：2016-5-10 上午9:44:14
	 * @param context
	 * @return
	 */
	public static boolean isRunningForeground(Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
		String currentPackageName = cn.getPackageName();
		if (!TextUtils.isEmpty(currentPackageName) && currentPackageName.equals(context.getPackageName())) {
			return true;
		}

		return false;
	}

	/**
	 * 判断对象或对象数组中每一个对象是否为空: 对象为null，字符序列长度为0，集合类、Map为empty
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean isNullOrEmpty(Object obj) {
		if (obj == null)
			return true;

		if (obj instanceof CharSequence)
			return ((CharSequence) obj).length() == 0;

		if (obj instanceof Collection)
			return ((Collection) obj).isEmpty();

		if (obj instanceof Map)
			return ((Map) obj).isEmpty();

		if (obj instanceof Object[]) {
			Object[] object = (Object[]) obj;
			if (object.length == 0) {
				return true;
			}
			boolean empty = true;
			for (int i = 0; i < object.length; i++) {
				if (!isNullOrEmpty(object[i])) {
					empty = false;
					break;
				}
			}
			return empty;
		}
		return false;
	}

	public static int getFontSize(Context context, int textSize) {
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		windowManager.getDefaultDisplay().getMetrics(dm);
		int screenHeight = dm.heightPixels;
		int rate = (int) (textSize * (float) screenHeight / 1280);
		return rate;
	}
}
