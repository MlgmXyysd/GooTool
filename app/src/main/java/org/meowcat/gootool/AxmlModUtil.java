/*
 * Copyright (c) 2013-2018 MeowCat Studio Powered by MlgmXyysd All Rights Reserved.
 */

package org.meowcat.gootool;

import com.goofans.gootool.wog.WorldOfGooAndroid;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AxmlModUtil {

  public static void modifyFiles() {
    File location = WorldOfGooAndroid.get().TEMP_MODDED_DIR;
    if (modifyAndroidManifest(location)) {
      modifyResources(location);
    }

  }

  private static boolean modifyAndroidManifest(File location) {
    File androidManifest = new File(location, "AndroidManifest.xml");

    //Java - y u so verbose?
    InputStream in = null;
    ByteArrayOutputStream baos;

    try {
      in = new FileInputStream(androidManifest);
      baos = new ByteArrayOutputStream();
      modifyAxml(in, baos);
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    } finally {
      if (in != null)
        try {
          in.close();
        } catch (IOException e) {
          e.printStackTrace();
          return false;
        }
    }

    OutputStream fos = null;
    try {
      fos = new FileOutputStream(androidManifest);
      fos.write(baos.toByteArray());
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    } finally {
      if (fos != null)
        try {
          fos.close();
        } catch (IOException e) {
          e.printStackTrace();
          return false;
        }
    }
    return true;
  }

  private static void modifyResources(File location) {
    File androidManifest = new File(location, "resources.arsc");

    //Java - y u so verbose?
    InputStream in = null;
    ByteArrayOutputStream baos;

    try {
      in = new FileInputStream(androidManifest);
      baos = new ByteArrayOutputStream();
      modifyResources(in, baos);
    } catch (IOException e) {
      e.printStackTrace();
      return;
    } finally {
      if (in != null)
        try {
          in.close();
        } catch (IOException e) {
          e.printStackTrace();
          return;
        }
    }

    OutputStream fos = null;
    try {
      fos = new FileOutputStream(androidManifest);
      fos.write(baos.toByteArray());
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (fos != null)
        try {
          fos.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
    }
  }

  private static void modifyResources(InputStream in, OutputStream os) throws IOException {
    if (!in.markSupported()) {
      in = new BufferedInputStream(in, 1024);
    }
    Assert.that(in.markSupported());
    int read;
    String toReplace = "World of Goo";
    String replaceWith = "  WoG Mods  ";
    Assert.that(toReplace.length() == replaceWith.length(), "length of the new name must be the same as the original name");
    while ((read = in.read()) != -1) {
      //assume that it contains only ASCII characters
      //it will unless it's some weird modified version that I won't support anyway
      boolean foundString = false;
      if (read == toReplace.charAt(0)) {
        //assume we found if for now
        foundString = true;
        in.mark(toReplace.length());
        for (int i = 1; i < toReplace.length(); i++) {
          //if it's the end of the stream we will break here and reset the stream. We will eventually reach that point in the outer loop
          if (in.read() != toReplace.charAt(i)) {
            //we didn't really find it :(
            foundString = false;
            in.reset();
            break;
          }
        }
      }
      if (foundString) {
        //write it to output
        for (int i = 0; i < replaceWith.length(); i++) {
          os.write(replaceWith.charAt(i));
        }
      } else {
        os.write(read);
      }
    }
  }

  @SuppressWarnings("ConstantConditions")
  private static void modifyAxml(InputStream in, OutputStream os) throws IOException {
    //this expects the original World of Goo Manifest.xml file. It probably won't work with any other file
    //
    Integer unknown1, fileSize, unknown3, textEndm4, preTextIntsNum, unknown6, unknown7, textStartm8, unknown9;
    unknown1 = readLeUint(in);
    fileSize = readLeUint(in);
    //System.out.println(fileSize);
    unknown3 = readLeUint(in);
    textEndm4 = readLeUint(in);
    //System.out.println(textEndm4);
    preTextIntsNum = readLeUint(in);
    unknown6 = readLeUint(in);
    unknown7 = readLeUint(in);
    textStartm8 = readLeUint(in);
    unknown9 = readLeUint(in);

    if (anyEqual(unknown1, fileSize, unknown3, textEndm4, preTextIntsNum, unknown6, unknown7, textStartm8, unknown9)) {
      throw new IOException("Unexpected end of file");
    }
    Integer[] preTextInts = new Integer[preTextIntsNum];

    for (int i = 0; i < preTextInts.length; i++) {
      preTextInts[i] = readLeUint(in);
    }
    if (anyEqual((Object)preTextInts)) {
      throw new IOException("Unexpected end of file");
    }

    LeUtf16String[] strings = new LeUtf16String[preTextInts.length];

    for (int i = 0; i < preTextInts.length; i++) {
      strings[i] = readNextString(in);
    }

    byte unknown[] = readUntilEof(in);

    LeUtf16String[] newStrings = new LeUtf16String[preTextInts.length];

    int[] newPreText = new int[preTextInts.length];
    //characters
    int lenIncrease = 0;
    int changedStrings = 0;
    for (int i = 0; i < strings.length; i++) {
      newStrings[i] = strings[i];
      newPreText[i] = preTextInts[i] + lenIncrease * 2;
      //System.out.println(strings[i].toString());
      if (strings[i].toString().equals("com.twodboy.worldofgoofull")) {
        lenIncrease += 8;
        newStrings[i] = new LeUtf16String("com.twodboy.worldofgoofull.mod0000");
        changedStrings++;
      }
      if (strings[i].toString().equals("World.Of.Goo")) {
        lenIncrease += 8;
        newStrings[i] = new LeUtf16String("World.Of.Goo.Mod0000");
        changedStrings++;
      }
      if (strings[i].toString().equals(".WorldOfGooFull")) {
        lenIncrease += "com.twodboy.worldofgoofull".length();
        newStrings[i] = new LeUtf16String("com.twodboy.worldofgoofull.WorldOfGooFull");
        changedStrings++;
      }
    }
    if (changedStrings != 3) {
      throw new IllegalStateException("Expected changing 3 strings. Changed " + changedStrings);
    }

    int bytesLenIncrease = lenIncrease * 2;

    int padBytes = 0;
    if ((bytesLenIncrease & 3) != 0) {
      padBytes = 4 - (lenIncrease & 3);
    }
    bytesLenIncrease += padBytes;
    System.out.println(padBytes);
    System.out.println(bytesLenIncrease);
    //write initial bytes
    writeLeUint(os, unknown1);
    writeLeUint(os, fileSize + bytesLenIncrease);
    writeLeUint(os, unknown3);
    writeLeUint(os, textEndm4 + bytesLenIncrease);
    writeLeUint(os, preTextIntsNum);
    writeLeUint(os, unknown6);
    writeLeUint(os, unknown7);
    writeLeUint(os, textStartm8);
    writeLeUint(os, unknown9);
    for (int i : newPreText) {
      writeLeUint(os, i);
    }
    for (LeUtf16String str : newStrings) {
      char[] ca = str.getRawData();
      System.out.println(str + ", " + str.length());
      for (char c : ca) {
        writeChar(os, c);
      }
    }
    for (int i = 0; i < padBytes; i++) {
      os.write(0);
    }
    os.write(unknown);
  }

  private static byte[] readUntilEof(InputStream in) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    int read;
    while ((read = in.read()) != -1) {
      bos.write(read);
    }
    return bos.toByteArray();
  }

  @SuppressWarnings("ConstantConditions")
  private static LeUtf16String readNextString(InputStream in) throws IOException {
    char c = readChar(in);
    char[] arr = new char[((c & 0xFF) << 8 | c >>> 8) + 2];
    arr[0] = c;
    //c+1: read null
    for (int i = 0; i < ((c & 0xFF) << 8 | c >>> 8) + 1; i++) {
      arr[i + 1] = readChar(in);
    }
    return new LeUtf16String(arr);
  }

  private static boolean anyEqual(Object... objs) {
    for (Object obj : objs) {
      if (obj == null) {
        return true;
      }
    }
    return false;
  }

  private static Integer readLeUint(InputStream in) throws IOException {
    int byte1 = in.read();
    if (byte1 == -1) {
      return null;
    }
    int byte2 = in.read();
    if (byte2 == -1) {
      return null;
    }
    int byte3 = in.read();
    if (byte3 == -1) {
      return null;
    }
    int byte4 = in.read();
    if (byte4 == -1) {
      return null;
    }

    return byte1 | byte2 << 8 | byte3 << 16 | byte4 << 24;
  }

  private static void writeLeUint(OutputStream out, int num) throws IOException {
    for (int i = 0; i < 4; i++) {
      out.write((num >>> (i * 8)) & 0xFF);
    }
  }

  private static void writeChar(OutputStream out, char num) throws IOException {
    out.write(num >>> 8);
    out.write(num & 0xFF);
  }

  private static Character readChar(InputStream in) throws IOException {
    int byte1 = in.read();
    if (byte1 == -1) {
      return null;
    }
    int byte2 = in.read();
    if (byte2 == -1) {
      return null;
    }
    return (char) (byte1 << 8 | byte2);
  }

}
