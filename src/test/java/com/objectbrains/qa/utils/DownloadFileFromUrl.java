package com.objectbrains.qa.utils;


import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;

public class DownloadFileFromUrl {
	public void downloadFileFromUrl(URL url, String filePathToDownloadTo) throws IOException {
		File file = new File(filePathToDownloadTo);
		FileUtils.copyURLToFile(url, file);
	}
}
