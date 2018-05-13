/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.movie;


import java.util.Map;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id: KeyFrameColor.java 389 2010-05-02 18:03:02Z david $
 */
public class KeyFrameColor extends KeyFrame
{
  public KeyFrameColor(byte[] contents, int offset, int stringTableOffset)
  {
    super(contents, offset, stringTableOffset);
  }

  @Override
  protected void setFrameXMLAttributes(Map<String, String> attributes)
  {
    attributes.put("color", String.valueOf(color));
  }
}