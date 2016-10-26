/*
 * Copyright 2008-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.anyframe.ide.command.common.util;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.salt.ZeroSaltGenerator;
import org.jasypt.util.text.BasicTextEncryptor;

/**
 * Basic Encryption / Decryption Util for String, without salt/initial vector
 * 
 * @author Juyong Lim
 */
public class EncryptUtil {
	private static final String ONLINE_PASSWORD_KEY = "%ONLINE_^%#D%_PBEWithMD5AndDES#%";
	private static final String ENCRYPTION_PREFIX = "ENC_";
	private static StandardPBEStringEncryptor textEncryptor = new StandardPBEStringEncryptor();
	static {
		textEncryptor.setPassword(ONLINE_PASSWORD_KEY);
		textEncryptor.setSaltGenerator(new ZeroSaltGenerator());
	}

	/**
	 * 입력된 문자열을 encrypt하여 반환한다.
	 * 
	 * @param String
	 *            To Encrypt
	 * @return Encrypted String
	 */
	public static String encrypt(String stringToEncrypt) {
		if (stringToEncrypt == null) {
			return null;
		}
		return ENCRYPTION_PREFIX + textEncryptor.encrypt(stringToEncrypt);
	}

	/**
	 * 입력된 암호화 문자열을 decrypt하여 반환한다.
	 * 
	 * @param Encrypted
	 *            String
	 * @return Decrypted String
	 */
	public static String decrypt(String stringToDecrypt) {
		if (stringToDecrypt == null) {
			return null;
		} else {
			if (stringToDecrypt.startsWith(ENCRYPTION_PREFIX)) {
				String decrypt = internalDecrypt(stringToDecrypt);
				String encrypt = EncryptUtil.encrypt(decrypt);
				if (stringToDecrypt.equals(encrypt)) {
					try {
						return internalDecrypt(stringToDecrypt);
					} catch (EncryptionOperationNotPossibleException eope) {
						// bonobono TODO : temporary code for compatibility with
						// old
						// version (encrypted text with salt)
						BasicTextEncryptor tempDecryptor = new BasicTextEncryptor();
						tempDecryptor.setPassword(ONLINE_PASSWORD_KEY);
						return tempDecryptor.decrypt(stringToDecrypt.substring(ENCRYPTION_PREFIX.length()));
					}
				} else {
					return stringToDecrypt;
				}
			} else {
				return stringToDecrypt;
			}
		}
	}

	private static String internalDecrypt(String stringToDecrypt) {
		return textEncryptor.decrypt(stringToDecrypt.substring(ENCRYPTION_PREFIX.length()));
	}

}
