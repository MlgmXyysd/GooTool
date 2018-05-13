/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.io;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.goofans.gootool.platform.PlatformSupport;
import com.goofans.gootool.util.Utilities;
import com.goofans.gootool.util.XMLUtil;

import java.io.*;
import java.util.logging.Logger;

import org.w3c.dom.Document;


/**
 *
 *  TODO should use source/target platform, not host
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id: GameFormat.java 409 2010-06-23 10:00:01Z david $
 */
public class GameFormat
{
  private static final Logger log = Logger.getLogger(GameFormat.class.getName());
  public static final String DEFAULT_CHARSET = "UTF-8";

  private GameFormat()
  {
  }

  public static byte[] decodeBinFile(File file) throws IOException
  {
    log.finest("decode bin file: " + file);
    return Utilities.readFile(file);
  }

  public static void encodeBinFile(File file, byte[] input) throws IOException
  {
    log.finest("encode bin file: " + file);
    Utilities.writeFile(file, input);
  }

  public static Document decodeXmlBinFile(File file) throws IOException
  {
    byte[] decoded = decodeBinFile(file);
    InputStream is = new ByteArrayInputStream(decoded);
    return XMLUtil.loadDocumentFromInputStream(is);
  }

  public static byte[] decodeProfileFile(File file) throws IOException
  {
    log.finest("decode profile file: " + file);
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public static void encodeProfileFile(File file, byte[] input) throws IOException
  {
    log.finest("encode profile file: " + file);
    throw new UnsupportedOperationException("Not implemented yet");
  }

  // pass File WITHOUT binltl suffix
  public static Bitmap decodeImage(File file) throws IOException
  {
    return BitmapFactory.decodeFile(file.getPath());
  }
}

