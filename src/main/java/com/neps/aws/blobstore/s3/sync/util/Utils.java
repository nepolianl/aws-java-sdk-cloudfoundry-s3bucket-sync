package com.neps.aws.blobstore.s3.sync.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class Utils {
	private static DecimalFormat decimalFormat = new DecimalFormat("#.##");
	private Utils() {
		decimalFormat.setRoundingMode(RoundingMode.UP);
	}
	
	public static double convertToMib(long byteValue) {
		// Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
		double fileSizeInKB = byteValue / 1024;
		
		// Convert the KB to MegaBytes (1 MB = 1024 KBytes)
		return fileSizeInKB / 1024;
	}
	
	public static double convertToGib(long byteValue) {
		// Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
		double fileSizeInKB = byteValue / 1024;
		
		// Convert the KB to MegaBytes (1 MB = 1024 KBytes)
		double fileSizeInMb = fileSizeInKB / 1024;
		
		// Convert the MB to GigaBytes (1 GB = 1024 MBytes)
		return fileSizeInMb / 1024;
	}
	
	public static String toHumanReadable(long bytes) {
		if (bytes <= 1024) return bytes +" Bytes";
		
		double inKb = (bytes / 1024);
		if (inKb <= 1024) {
			return decimalFormat.format(inKb) + " KiB";
		} else {
			double inMb = (inKb / 1024);
			if (inMb <= 1024) {
				return decimalFormat.format(inMb) + " MiB";
			} else {
				double inGb = (inMb / 1024);
				return decimalFormat.format(inGb) + " GiB";
			}
		}
	}
}
