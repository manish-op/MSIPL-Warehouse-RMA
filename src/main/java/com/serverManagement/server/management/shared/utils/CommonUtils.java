package com.serverManagement.server.management.shared.utils;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.springframework.util.StringUtils;

public class CommonUtils {



	public static boolean validateEmailFormat(String email) {
		String emailPattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
				+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

		Pattern pattern = Pattern.compile(emailPattern);
		Matcher matcher = pattern.matcher(email);
		return matcher.matches();
	}

	public static boolean validateMobileNumber(String mobile) {
		if (!(StringUtils.hasLength(mobile))) {
			return false;
		}
		if (mobile.length() != 10) {
			return false;
		}
		if (mobile.matches("[0-9]+")) {
			return true;
		} else {
			return false;
		}
	}

	public static String getDoubleStringValue(Double value) {
		String plainNumber = "";
		if (value != null) {

			BigDecimal number = new BigDecimal(value);
			plainNumber = number.toPlainString();
		}

		return plainNumber;
	}

	public static long generateOTP() {
		Random random = new Random();
		long otp = 100000 + random.nextInt(99999);
		return otp;
	}

	public static ResourceBundle getResourceBundle(String resourceFileName) {
		return ResourceBundle.getBundle(resourceFileName, Locale.US);
	}

}

