package imageEdit;


/**
 * 
 * @author �κ��		�๦�� : ͼ���˾���(����ģʽ)			date: 8.8
 * 
 * 1.ͼ��Ŀ�¡				clone()
 * 2.��ȡͼ���������ɫ		getPixelColor(int x, int y)
 * 3.��ȡͼ��ĺ�ɫ�ɷ�		getRComponent�Ķ�����غ���
 * 4.��ȡͼ�����ɫ�ɷ�		getGComponent�Ķ�����غ���
 * 5.��ȡͼ��ĺ�ɫ�ɷ�		getBComponent�Ķ�����غ���
 * 6.����ͼ���������ɫ		setPixelColor(int x, int y, int rgbcolor)
 * 7.��ȡ��ɫ����			getColorArray()
 * 8.��ȡĿ��ͼ��			getDstBitmap()
 * 
 */

public class ImageFilter {

	private static ImageFilter filterInstance = new ImageFilter();			// �˾�ʵ��
	public float BrightnessFactor = 0.25f;
	public float ContrastFactor = 0f;										// ��������,��ΧΪ-1��1
	
	/**
	 * 	���� �������˾�ʵ��
	 * @return ImageFilter
	 * 
	 */
	public static ImageFilter getInstance()
	{
		if ( filterInstance == null)
		{
			filterInstance = new ImageFilter();
		}

		return filterInstance;
	}
	
	
	/**
	 * 	���� ��  �����Աȶ���Ч
	 * @return ImageData
	 * 
	 */
	public ImageData BrightFilter(Bitmap olgImg) {
		
		ImageData image = new ImageData(olgImg);
		int width = image.getWidth();
		int height = image.getHeight();
		int r, g, b;
		// Convert to integer factors
		int bfi = (int) (BrightnessFactor * 255);
		float cf = 1f + ContrastFactor;
		cf *= cf;
		int cfi = (int) (cf * 32768) + 1;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				r = image.getRComponent(x, y);
				g = image.getGComponent(x, y);
				b = image.getBComponent(x, y);
				// Modify brightness (addition)
				if (bfi != 0) {
					// Add brightness
					int ri = r + bfi;
					int gi = g + bfi;
					int bi = b + bfi;
					// Clamp to byte boundaries
					r = ri > 255 ? 255 : (ri < 0 ? 0 : ri);
					g = gi > 255 ? 255 : (gi < 0 ? 0 : gi);
					b = bi > 255 ? 255 : (bi < 0 ? 0 : bi);
				}
				// Modifiy contrast (multiplication)
				if (cfi != 32769) {
					// Transform to range [-128, 127]
					int ri = r - 128;
					int gi = g - 128;
					int bi = b - 128;

					// Multiply contrast factor
					ri = (ri * cfi) >> 15;
					gi = (gi * cfi) >> 15;
					bi = (bi * cfi) >> 15;

					// Transform back to range [0, 255]
					ri = ri + 128;
					gi = gi + 128;
					bi = bi + 128;

					// Clamp to byte boundaries
					r = ri > 255 ? 255 : (ri < 0 ? 0 : ri);
					g = gi > 255 ? 255 : (gi < 0 ? 0 : gi);
					b = bi > 255 ? 255 : (bi < 0 ? 0 : bi);
				}
				image.setPixelColor(x, y, r, g, b);
			}
		}
		return image;
	}
	
	
	/**
	 * 	���� �� ���˾�
	 * @return ImageData
	 * 
	 */
	public ImageData IceFilter(Bitmap olgImg) {
		
		ImageData image = new ImageData(olgImg); 
		int width = image.getWidth();
		int height = image.getHeight();
		int R, G, B, pixel;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				R = image.getRComponent(x, y); // ��ȡRGB��ԭɫ
				G = image.getGComponent(x, y);
				B = image.getBComponent(x, y);

				pixel = R - G - B;
				pixel = pixel * 3 / 2;
				if (pixel < 0)
					pixel = -pixel;
				if (pixel > 255)
					pixel = 255;
				R = pixel; 

				pixel = G - B - R;
				pixel = pixel * 3 / 2;
				if (pixel < 0)
					pixel = -pixel;
				if (pixel > 255)
					pixel = 255;
				G = pixel;

				pixel = B - R - G;
				pixel = pixel * 3 / 2;
				if (pixel < 0)
					pixel = -pixel;
				if (pixel > 255)
					pixel = 255;
				B = pixel;
				image.setPixelColor(x, y, R, G, B);
			} // x
		} // y
		
		return image;
	}
	
	
	/**
	 * 	���� �� ����
	 * @return ImageData
	 * 
	 */
	public ImageData MoltenFilter(Bitmap olgImg) {
			
		ImageData image = new ImageData( olgImg ); 
		int width = image.getWidth();
		int height = image.getHeight();
		int R, G, B, pixel;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				R = image.getRComponent(x, y); // ��ȡRGB��ԭɫ
				G = image.getGComponent(x, y);
				B = image.getBComponent(x, y);

				pixel = R * 128 / (G + B + 1);
				if (pixel < 0)
					pixel = 0;
				if (pixel > 255)
					pixel = 255;
				R = pixel;

				pixel = G * 128 / (B + R + 1);
				if (pixel < 0)
					pixel = 0;
				if (pixel > 255)
					pixel = 255;
				G = pixel;

				pixel = B * 128 / (R + G + 1);
				if (pixel < 0)
					pixel = 0;
				if (pixel > 255)
					pixel = 255;
				B = pixel;
				image.setPixelColor(x, y, R, G, B);
			} // x
		} // y
		
		return image;
	}
	
	/**
	 * 	���� �� ������Ե
	 * @return ImageData
	 * 
	 */
	public ImageData GlowingEdgeFilter( Bitmap olgImg) {
		
		ImageData image = new ImageData( olgImg ); 
		int width = image.getWidth();
		int height = image.getHeight();
		// ͼ��ʵ�ʴ�������
		// ���������� 1 �У������� 1 ��
		int rectTop = 0;
		int rectBottom = height - 1;
		int rectLeft = 0;
		int rectRight = width - 1;
		int pixel;

		int R, G, B;
		for (int y = rectTop; y < rectBottom; y++) {
			for (int x = rectLeft; x < rectRight; x++) {
				{
					pixel = (int) (Math.pow((image.getBComponent(x, y) - image.getBComponent(x, y, width)), 2) + Math
							.pow((image.getBComponent(x, y) - image.getBComponent(x, y, 1)), 2));
					pixel = (int) (Math.sqrt(pixel) * 2);

					if (pixel < 0)
						pixel = 0;
					if (pixel > 255)
						pixel = 255;

					B = pixel;
				}
				{
					pixel = (int) (Math.pow((image.getGComponent(x, y) - image.getGComponent(x, y, width)), 2) + Math
							.pow((image.getGComponent(x, y) - image.getGComponent(x, y, 1)), 2));
					pixel = (int) (Math.sqrt(pixel) * 2);

					if (pixel < 0)
						pixel = 0;
					if (pixel > 255)
						pixel = 255;

					G = pixel;
				}
				{
					pixel = (int) (Math.pow((image.getRComponent(x, y) - image.getRComponent(x, y, width)), 2) + Math
							.pow((image.getRComponent(x, y) - image.getRComponent(x, y, 1)), 2));
					pixel = (int) (Math.sqrt(pixel) * 2);

					if (pixel < 0)
						pixel = 0;
					if (pixel > 255)
						pixel = 255;

					R = pixel;
				}

				image.setPixelColor(x, y, R, G, B);
			} // x
		} // y

		return image;
	}
	
	
	/**
	 * 	���� �� �������˾�
	 * @return ImageData
	 * 
	 */
	public ImageData ComicFilter( Bitmap olgImg) {
		
		ImageData image = new ImageData( olgImg ); 
		int width = image.getWidth();
		int height = image.getHeight();
		int R, G, B, pixel;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				R = image.getRComponent(x, y); // ��ȡRGB��ԭɫ
				G = image.getGComponent(x, y);
				B = image.getBComponent(x, y);

				// R = |g �C b + g + r| * r / 256;
				pixel = G - B + G + R;
				if (pixel < 0)
					pixel = -pixel;
				pixel = pixel * R / 256;
				if (pixel > 255)
					pixel = 255;
				R = pixel;

				// G = |b �C g + b + r| * r / 256;
				pixel = B - G + B + R;
				if (pixel < 0)
					pixel = -pixel;
				pixel = pixel * R / 256;
				if (pixel > 255)
					pixel = 255;
				G = pixel;

				// B = |b �C g + b + r| * g / 256;
				pixel = B - G + B + R;
				if (pixel < 0)
					pixel = -pixel;
				pixel = pixel * G / 256;
				if (pixel > 255)
					pixel = 255;
				B = pixel;
				image.setPixelColor(x, y, R, G, B);
			}
		}
		Bitmap bitmap = image.getDstBitmap();
		bitmap = toGrayscale( bitmap ); 	// ͼƬ�ҶȻ�����
		image = new ImageData(bitmap);
		return image;
	}
	
	/**
	 * 	���� ��  ͼƬ�ҶȻ�
	 * @param bmpOriginal
	 * @return ImageData
	 * 
	 */
	private Bitmap toGrayscale(Bitmap bmpOriginal) {
		
		int width, height;
		height = bmpOriginal.getHeight();
		width = bmpOriginal.getWidth();

		Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		Canvas c = new Canvas(bmpGrayscale);
		Paint paint = new Paint();
		ColorMatrix cm = new ColorMatrix();
		cm.setSaturation(0); // ��ɫ
		ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
		paint.setColorFilter(f);
		c.drawBitmap(bmpOriginal, 0, 0, paint);
		return bmpGrayscale;
	}
}
