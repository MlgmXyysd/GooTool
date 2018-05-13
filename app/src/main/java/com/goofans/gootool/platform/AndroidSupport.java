/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft
 * Copyright (c) 2015 Bartosz Skrzypczak
 *
 * All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */
package com.goofans.gootool.platform;

import org.meowcat.gootool.WoGInitData;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * PlatformSupport for android. Based on Windows/Linux implementations.
 */
public class AndroidSupport extends PlatformSupport {

  @Override
  protected String[] doGetProfileSearchPaths() {
    return new String[0];
  }

  @Override
  protected File doGetToolStorageDirectory() throws IOException {
    return WoGInitData.getContext().getExternalFilesDir(null);
  }
}
