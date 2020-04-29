package com.speedata.uhfprinterdemo;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;

import java.io.PrintStream;
import java.util.Locale;

public class CanvasPrint {
    private Canvas canvas;
    public Paint mPaint;
    private Bitmap bitmap;
    private int width;
    private float length = 0.0F;
    private int textSize;
    private float currentY;
    private boolean textExceedNewLine = true;
    private boolean useSplit;
    private String splitStr = " ";
    private boolean textAlignRight;

    public int getLength() {
        return (int) this.length;
    }

    public int getWidth() {
        return this.width;
    }

    public void initCanvas(int w) {
        int h = w * 5;
        this.bitmap = Bitmap.createBitmap(w, h, Config.ARGB_4444);
        this.canvas = new Canvas(this.bitmap);
        this.canvas.drawColor(Color.parseColor("#FFFFFF"));
    }

    public void initPaint() {
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setColor(Color.parseColor("#000000"));
    }

    public void init(int width) {
        this.width = width;
        initCanvas(this.width);
        initPaint();
    }

    public void setFontProperty(FontProperty fp) {
        if (fp.sFace != null) {
            try {
                this.mPaint.setTypeface(fp.sFace);
            } catch (Exception var3) {
                this.mPaint.setTypeface(Typeface.DEFAULT);
            }
        } else {
            this.mPaint.setTypeface(Typeface.DEFAULT);
        }
        if (fp.bBold) {
            this.mPaint.setFakeBoldText(true);
        } else {
            this.mPaint.setFakeBoldText(false);
        }
        if (fp.bItalic) {
            this.mPaint.setTextSkewX(-0.5F);
        } else {
            this.mPaint.setTextSkewX(0.0F);
        }
        if (fp.bUnderLine) {
            this.mPaint.setUnderlineText(true);
        } else {
            this.mPaint.setUnderlineText(false);
        }
        if (fp.bStrikeout) {
            this.mPaint.setStrikeThruText(true);
        } else {
            this.mPaint.setStrikeThruText(false);
        }
        this.mPaint.setTextSize(fp.iSize);
    }

    public void setLineWidth(float w) {
        this.mPaint.setStrokeWidth(w);
    }

    public void setTextSize(int size) {
        this.textSize = size;
        this.mPaint.setTextSize(this.textSize);
    }

    public void setItalic(boolean italic) {
        if (italic) {
            this.mPaint.setTextSkewX(-0.5F);
        } else {
            this.mPaint.setTextSkewX(0.0F);
        }
    }

    public void setStrikeThruText(boolean strike) {
        this.mPaint.setStrikeThruText(strike);
    }

    public void setUnderlineText(boolean underline) {
        this.mPaint.setUnderlineText(underline);
    }

    public void setFakeBoldText(boolean fakeBold) {
        this.mPaint.setFakeBoldText(fakeBold);
    }

    public void setUseSplit(boolean useSplit) {
        this.useSplit = useSplit;
    }

    public void setUseSplitAndString(boolean useSplit, String splitStr) {
        this.useSplit = useSplit;
        this.splitStr = splitStr;
    }

    public void setTextExceedNewLine(boolean newLine) {
        this.textExceedNewLine = newLine;
    }

    public void setTextAlignRight(boolean alignRight) {
        this.textAlignRight = alignRight;
    }

    public void drawText(int text_x, int text_y, int width, int height, String str) throws Exception {
        int f_height = (int) getFontHeight();
        int _x = width / f_height;
        int _xl = _x * 2;
        int _y = height / f_height;
        int _xxl = _xl;
        int ii = 0;
        boolean ver = true;
        int a = str.length();
        byte[] btf = str.getBytes("gbk");
        int al = btf.length;
        if (_y == 1) {
            if (_xl - al >= 0) {
                System.out.println("001" + str);
                drawText(text_x, text_y, str);
            } else {
                int count = 0;
                for (int j = 0; j <= _xl - 1; j++) {
                    if (btf[j] < 0) {
                        count++;
                    }
                }
                String s;
                if (count % 2 == 0) {
                    s = new String(btf, ii, _xl, "gbk");
                } else {
                    s = new String(btf, ii, _xl - 1, "gbk");
                }
                System.out.println("002" + s);
                drawText(text_x, text_y, s);
            }
        } else {
            int b = str.length();
            if (_x == 1) {
                for (int t = 0; t < b; t++) {
                    String s = str.substring(ii, _x);
                    System.out.println("003" + s);
                    drawText(text_x, text_y + f_height * (t + 1) - 24, s);
                    ii++;
                    _x++;
                }
            }
            int tt = 0;
            if ((_xl - al >= 0) && (ver)) {
                System.out.println("004" + str);
                drawText(text_x, text_y, str);
                return;
            }
            if (_xl - al < 0) {
                for (int t = 0; t < _y; t++) {

                    int count = 0;
                    for (int j = ii; j <= _xxl - 1; j++) {
                        if (btf[j] < 0) {
                            count++;
                        }
                    }
                    String s;
                    if (count % 2 == 0) {
                        s = new String(btf, ii, _xl, "gbk");
                        ii += _xl;
                        al -= _xl;
                        _xxl = ii + _xl;
                    } else {
                        s = new String(btf, ii, _xl - 1, "gbk");
                        ii += _xl - 1;
                        al -= _xl - 1;
                        _xxl = ii + _xl;
                    }
                    System.out.println("005" + s);
                    drawText(text_x, text_y + f_height * tt++, s);
                    if ((_xl - al >= 0) && (t < _y - 1)) {
                        s = new String(btf, ii, al, "gbk");
                        System.out.println("006" + s);
                        drawText(text_x, text_y + f_height * tt++, s);
                        return;
                    }
                }
            }
        }
    }

    public void drawText(int x, int y, String string) {
        this.currentY += getFontHeight();
        int validWidth = this.width - x;
        float textWidth = getTextWidth(string);
        if (this.textExceedNewLine) {
            int pos1;
            for (; ((pos1 = getValidStringPos(string, validWidth)) > 0) && (textWidth > 0.0F); this.currentY = (y + getFontHeight())) {
                String printStr = string.substring(0, pos1);
                if (this.textAlignRight) {
                    float tmpWidth = getTextWidth(printStr);
                    this.canvas.drawText(printStr, x + (validWidth - tmpWidth), y, this.mPaint);
                } else {
                    this.canvas.drawText(printStr, x, y, this.mPaint);
                }
                string = string.substring(pos1);
                textWidth -= validWidth;
            }
            this.currentY -= getFontHeight();
        } else if (this.textAlignRight) {
            this.canvas.drawText(string, x + (validWidth - textWidth), y, this.mPaint);
        } else {
            this.canvas.drawText(string, x, y, this.mPaint);
        }
        if (this.length < this.currentY) {
            this.length = this.currentY;
        }
    }

    public void drawText(int x, String string) {
        this.currentY += getFontHeight();
        int validWidth = this.width - x;
        float textWidth = getTextWidth(string);
        if (this.textExceedNewLine) {
            int pos1;
            for (; ((pos1 = getValidStringPos(string, validWidth)) > 0) && (textWidth > 0.0F); this.currentY += getFontHeight()) {
                String printStr = string.substring(0, pos1);
                if (this.textAlignRight) {
                    float tmpWidth = getTextWidth(printStr);
                    this.canvas.drawText(printStr, x + (validWidth - tmpWidth), this.currentY, this.mPaint);
                } else {
                    this.canvas.drawText(printStr, x, this.currentY, this.mPaint);
                }
                string = string.substring(pos1, string.length());
                textWidth -= validWidth;
            }
            this.currentY -= getFontHeight();
        } else if (this.textAlignRight) {
            this.canvas.drawText(string, x + (validWidth - textWidth), this.currentY, this.mPaint);
        } else {
            this.canvas.drawText(string, x, this.currentY, this.mPaint);
        }
        if (this.length < this.currentY) {
            this.length = this.currentY;
        }
    }

    public void drawText(String string) {
        this.currentY += getFontHeight();
        int validWidth = this.width;
        float textWidth = getTextWidth(string);
        if (this.textExceedNewLine) {
            boolean pos = false;
            int pos1;
            while (((pos1 = getValidStringPos(string, validWidth)) > 0) && (textWidth > 0.0F)) {
                String printStr = string.substring(0, pos1);
                if (this.textAlignRight) {
                    float tmpWidth = getTextWidth(printStr);
                    this.canvas.drawText(printStr, validWidth - tmpWidth, this.currentY, this.mPaint);
                } else {
                    this.canvas.drawText(printStr, 0.0F, this.currentY, this.mPaint);
                }
                string = string.substring(pos1, string.length());
                textWidth -= validWidth;
                if (!string.isEmpty()) {
                    this.currentY += getFontHeight();
                }
            }
        } else if (this.textAlignRight) {
            this.canvas.drawText(string, validWidth - textWidth, this.currentY, this.mPaint);
        } else {
            this.canvas.drawText(string, 0.0F, this.currentY, this.mPaint);
        }
        if (this.length < this.currentY) {
            this.length = this.currentY;
        }
    }

    public void drawLine(float startX, float startY, float stopX, float stopY) {
        this.canvas.drawLine(startX, startY, stopX, stopY, this.mPaint);
        float max = 0.0F;
        max = startY > stopY ? startY : stopY;
        if (this.length < max) {
            this.length = max;
        }
    }

    public void drawRectangle(float left, float top, float right, float bottom) {
        this.canvas.drawRect(left, top, right, bottom, this.mPaint);
        float max = 0.0F;
        max = top > bottom ? top : bottom;
        if (this.length < max) {
            this.length = max;
        }
    }

    public void drawEllips(float left, float top, float right, float bottom) {
        RectF re = new RectF(left, top, right, bottom);
        this.canvas.drawOval(re, this.mPaint);
        float max = 0.0F;
        max = top > bottom ? top : bottom;
        if (this.length < max) {
            this.length = max;
        }
    }

    public void drawImage(Bitmap image) {
        this.canvas.drawBitmap(image, 0.0F, this.currentY, (Paint) null);
        this.currentY += image.getHeight();
        if (this.length < this.currentY) {
            this.length = this.currentY;
        }
    }

    public void drawImage(int left, Bitmap image) {
        this.canvas.drawBitmap(image, left, this.currentY, (Paint) null);
        this.currentY += image.getHeight();
        if (this.length < this.currentY) {
            this.length = this.currentY;
        }
    }

    public void drawImage(int left, float top, Bitmap image) {
        this.canvas.drawBitmap(image, left, top, (Paint) null);

        float max = 0.0F;
        max = top + image.getHeight();
        if (this.length < max) {
            this.length = max;
        }
    }

    public Bitmap getCanvasImage() {
        return Bitmap.createBitmap(this.bitmap, 0, 0, this.width, getLength());
    }

    private float getTextWidth(String text) {
        return this.mPaint.measureText(text);
    }

    public float getCurrentPointY() {
        return this.currentY;
    }

    private float getFontHeight() {
        return this.mPaint.getTextSize();
    }

    private float getCharacterWidth() {
        float spacing = this.mPaint.getFontSpacing();
        String lang = Locale.getDefault().getLanguage();
        if ((!lang.equals("ja")) && (!lang.equals("ko")) && (!lang.equals("zh"))) {
            spacing /= 2.0F;
        }
        return spacing;
    }

    private int getValidStringPos(String string, int validWidth) {
        float textWidth = getTextWidth(string);
        while ((textWidth > 0.0F) && (textWidth > validWidth)) {
            int subPos = (int) (validWidth * string.length() / textWidth);
            string = string.substring(0, subPos);
            textWidth = getTextWidth(string);
            if (textWidth <= validWidth) {
                if ((this.useSplit) && (string.contains(this.splitStr))) {
                    subPos = string.lastIndexOf(this.splitStr);
                }
                return subPos;
            }
        }
        return string.length();
    }
}
