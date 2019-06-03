package chat.encrypter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface EncryptService {
	public void encrypt(InputStream is, OutputStream os) throws IOException;
	public void decrypt(InputStream is, OutputStream os) throws IOException;
	public byte[] decrypt(byte[] date) throws IOException;
	public byte[] encrypt(byte[] data) throws IOException;
}
