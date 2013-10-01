package imageCache;

/**
 * Copyright (c) 2012,UIT-ESPACE( TEAM: UIT-GEEK) All rights reserved.
 * 
 * @Title: ImageCacheToSDCard.java
 * @Package: imageCache
 * @Author: �κ��(Mr.Simple)
 * @E-mail: bboyfeiyu@gmail.com
 * @Version: V1.0
 * @Date�� 2012-11-8 ����7:40:40
 * @Description: ͼƬ������ 1.
 * 
 */

public class ImageCacheToSDCard {

	private final String TAG = "ͼƬ����";
	private String mBigImgCachePath = null;
	private String mSmallImgCachePath = null;
	private final int MB = 1024 * 1024;
	private final int FREE_SD_SPACE = 40;
	private final int CACHE_SIZE = 40;
	private int mCacheType = 1; // Сͼ����
	private final long mTimeDiff = 604800000; // �������춯��ͼƬ��ɾ��
	private Bitmap mBitmap = null;
	private String mFileName = null;
	private static ImageCacheToSDCard mImageCache = new ImageCacheToSDCard();

	/**
	 * 
	 * @Constructor: ���캯��
	 * @
	 * @Description:
	 */
	private ImageCacheToSDCard() {
		mBigImgCachePath = Environment.getExternalStorageDirectory()
				+ File.separator + "a_sns_cache";
		mSmallImgCachePath = Environment.getExternalStorageDirectory()
				+ File.separator + "a_sns_small_cache";
		// ����Ŀ¼
		File file = new File(mSmallImgCachePath);
		if (!file.exists()) {
			file.mkdir();
		}

		file = null ;
		System.gc() ;
		
		file = new File(mBigImgCachePath);
		if (!file.exists()) {
			file.mkdir();
		}

		try{
			// �Զ������ڵ�ͼƬ����
			removeExpiredCache( mSmallImgCachePath );
			removeExpiredCache( mBigImgCachePath ) ;
			// ����ڴ濨��ʣ������̫С,�����40%�Ļ���
			remove40PercentCache( mSmallImgCachePath ) ;
			remove40PercentCache( mBigImgCachePath ) ;
		}catch(Exception e){
			e.printStackTrace() ;
		}
		Log.d(TAG, "���湹�캯��");
		
	}

	/**
	 * @Method: getInstance
	 * @Description: ����ģʽ, ��ȡImageCacheToSDCardʵ��
	 * @return ImageCacheToSDCard ��������
	 * @throws
	 */
	public static ImageCacheToSDCard getInstance() {
		if (mImageCache == null) {
			mImageCache = new ImageCacheToSDCard();
		}

		return mImageCache;
	}

	/**
	 * 
	 * @Method: saveBmpToSd
	 * @Description:   ��ͼƬ���浽SD����
	 * @param bm 	        Ҫ�����ͼƬ
	 * @param fileName ͼƬ�� #param type Ҫ�����ͼƬ����,��ͼ����Сͼ
	 * @return void ��������
	 * @throws
	 */
	public void saveBmpToSd(Bitmap bmp, String fileName, int type)
			throws Exception {

		mBitmap = bmp;
		mFileName = fileName;
		mCacheType = type;
		// ͬ����
		synchronized (this) {
			if (mBitmap == null) {
				Log.w(TAG, "����-->Ҫ�����ͼƬΪ��");
				return;
			}

			// �ж�sdcard�ϵĿռ�,�ڴ濨�ϵĿռ�С��50MB,�򲻻���
			if (FREE_SD_SPACE > freeSpaceOnSd()) {
				Log.w(TAG, "�ڴ濨��ʣ������̫С,�����л���");
				return;
			}

			// Ĭ����дСͼ����
			String dirPath = mSmallImgCachePath;
			if (mCacheType == 2) {
				dirPath = mBigImgCachePath;
			}
			File file = new File(dirPath + File.separator + mFileName);
			try {
				if (!file.exists()) {

					file.createNewFile();
					OutputStream outStream = new FileOutputStream(file);
					// ��ͼƬѹ�����������
					mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
					// ǿ��ˢ��,ʹ���д�뵽�ļ�����
					outStream.flush();
					outStream.close();
					Log.i(TAG, mFileName + "ͼƬ�Ѿ����浽�ļ�");
					// �����ļ�������޸�ʱ��
					updateFileTime( dirPath, mFileName);
				} else {
					Log.i(TAG, mFileName + "ͼƬ�Ѿ�����");
				}

			} catch (Exception e) {
				Log.w(TAG, "���滺��ͼƬ����");
				e.printStackTrace();
			}
		}


	}

	
	/**
	 * @Method: getImageFromSD
	 * @Description:
	 * @param path     �Ӳ�ͬ��·����ȡ��ͬ�Ļ���
	 * @param fileName Ҫ��ȡ���ļ���
	 * @return Bitmap ��������
	 * @throws
	 */
	public synchronized Bitmap getImageFromSD(int type, String fileName) {
		
		mCacheType = type;
		mFileName = fileName;
		mBitmap = Bitmap.createBitmap(5, 5, Bitmap.Config.ARGB_4444);
		
		String path = mSmallImgCachePath;
		if (2 == mCacheType) {
			path = mBigImgCachePath;
		}
		try {
			File file = new File(path, mFileName);
			InputStream input = new FileInputStream(file);
			// ͨ���ļ�������ͼƬ
			mBitmap = BitmapFactory.decodeStream(input);
			input.close();
			input = null;
			System.gc() ;
			if (mBitmap.getHeight() > 10) {
				Log.d(TAG, mFileName + "�ӻ����ж�ȡ�ɹ�");
			}
		} catch (Exception e) {
			Log.d(TAG, "�ӻ����ж�ȡʧ��") ;
			e.printStackTrace();
		}

		return mBitmap;
	}

	
	/**
	 * @Method: freeSpaceOnSd
	 * @Description: ����sdcard�ϵ�ʣ��ռ�
	 * @return int ��������
	 * @throws
	 */
	private int freeSpaceOnSd() {
		StatFs stat = new StatFs(Environment.getExternalStorageDirectory()
				.getPath());
		double sdFreeMB = ((double) stat.getAvailableBlocks() * (double) stat
									.getBlockSize()) / MB;
		Log.d(TAG, "�ڴ濨ʣ���ڴ�Ϊ������" + sdFreeMB);
		return (int) sdFreeMB;
	}

	
	/**
	 * @Method: updateFileTime
	 * @Description: �޸��ļ�������޸�ʱ��
	 * @param fileName
	 * @return void ��������
	 * @throws
	 */
	private void updateFileTime(String path, String fileName) {
		File file = new File( path, fileName );
		long newModifiedTime = System.currentTimeMillis();
		file.setLastModified( newModifiedTime );
	}

	
	/**
	 * @Method: removeCache
	 * @Description:  ����洢Ŀ¼�µ��ļ���С�����ļ��ܴ�С���ڹ涨��40MB,����sdcardʣ��ռ�С��40MB�Ĺ涨
	 *                ��ôɾ��40%���û�б�ʹ�õ��ļ�
	 * @return void ��������
	 * @throws
	 */
	public void remove40PercentCache(String path) throws Exception{

		File dir = new File( path );
		// ��ȡ�ļ��б�
		File[] files = dir.listFiles();
		if ( files.length == 0) {
			return;
		}

		int dirSize = 0;
		// �����ļ�,��ȡ�����ļ����ܴ�С
		for (int i = 0; i < files.length; i++) {
			dirSize += files[i].length();
		}

		if (dirSize > CACHE_SIZE * MB || FREE_SD_SPACE > freeSpaceOnSd()) {
			int removeFactor = (int) ((0.4 * files.length) + 1);
			// ������޸�ʱ������
			Arrays.sort(files, new FileLastModifSort());

			Log.i(TAG, "���40%���ڵĻ���ͼƬ");
			for (int i = 0; i < removeFactor; i++) {
				files[i].delete();
			}

		}

	}

	
	/**
	 * @Method: removeExpiredCache
	 * @Description: ɾ������ļ�,mTimeDiffΪ�趨�ĳ�ʱ������,�������ʱ�����Զ�ɾ��
	 * @return void ��������
	 * @throws
	 */
	private void removeExpiredCache( String path ) throws Exception{

		File file = new File( path );
		File[] files = file.listFiles();
		for (int i = 0; i < file.length(); i++) {
			if (System.currentTimeMillis() - file.lastModified() > mTimeDiff) {

				Log.i(TAG, "�����ڻ���!");
				files[i].delete();

			}
		}
	}

	
	/**
	 * @Method: clearImageCache
	 * @Description: �ֶ�ɾ�����л���
	 * @return void ��������
	 * @throws
	 */
	public void clearImageCache() {
		try{
			File file = new File( mBigImgCachePath );
			File[] files = file.listFiles();
			for (File fs : files) {
				fs.delete();
			}
			
			file = new File( mSmallImgCachePath );
			files = file.listFiles();
			for(File fss : files){
				fss.delete() ;
			}
			
		}catch(Exception e){
			e.printStackTrace() ;
		}
		
	}

	
	/**
	 * @ClassName: FileLastModifSort
	 * @Description: ����ļ�������޸�ʱ���������
	 * @Author: Mr.Simple
	 * @E-mail: bboyfeiyu@gmail.com
	 * @Date 2012-11-8 ����8:01:13
	 * 
	 */
	class FileLastModifSort implements Comparator<File> {
		@Override
		public int compare(File arg0, File arg1) {
			if (arg0.lastModified() > arg1.lastModified()) {
				return 1;
			} else if (arg0.lastModified() == arg1.lastModified()) {
				return 0;
			} else {
				return -1;
			}
		}
	}

}
