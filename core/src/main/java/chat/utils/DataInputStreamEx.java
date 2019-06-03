package chat.utils;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

public class DataInputStreamEx {
	private static final byte HASVALUE = 1;
	private static final byte NOVALUE = 0;
	private DataInputStream dis;
	private InputStream inputStream;

	public DataInputStreamEx(InputStream arg0) {
		dis = new DataInputStream(arg0);
		inputStream = arg0;
	}

	public DataInputStream original() {
		return dis;
	}
	
	public boolean hasValue() throws IOException {
		byte hasValue = dis.readByte();
		if(hasValue == HASVALUE) {
			return true;
		}
		return false;
	}
	
	public void close() throws IOException {
		dis.close();
	}

	public Boolean readBoolean() throws IOException {
		if(hasValue()) {
			return dis.readBoolean();
		}
		return null;
	}

	public Byte readByte() throws IOException {
		if(hasValue()) {
			return dis.readByte();
		}
		return null;
	}

	public Short readShort() throws IOException {
		if(hasValue()) {
			return dis.readShort();
		}
		return null;
	}

	public Character readChar() throws IOException {
		if(hasValue()) {
			return dis.readChar();
		}
		return null;
	}

	public Integer readInt() throws IOException {
		if(hasValue()) {
			return dis.readInt();
		}
		return null;
	}

	public Long readLong() throws IOException {
		if(hasValue()) {
			return dis.readLong();
		}
		return null;
	}

	public Float readFloat() throws IOException {
		if(hasValue()) {
			return dis.readFloat();
		}
		return null;
	}

	public Double readDouble() throws IOException {
		if(hasValue()) {
			return dis.readDouble();
		}
		return null;
	}

	public String readUTF() throws IOException {
		if(hasValue()) {
			return dis.readUTF().intern();
		}
		return null;
	}
	public void readFully(byte[] buf) throws IOException {
		if(hasValue()) {
			dis.readFully(buf);
		}
	}
	
	public Date readDate(String format) throws IOException {
		if(hasValue()) {
			String str = dis.readUTF();
			DateFormat formatDate = new SimpleDateFormat(format);
			try {
				return formatDate.parse(str);
			} catch (ParseException e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}
	
	public String readLongString() throws IOException {
		int size = 0;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		size = dis.readInt();
		if(size <= 0)
			return null;
		int readed, total = 0;
		byte[] data = new byte[size];
		while (total < size) {
			readed = dis.read(data, 0, size - total);
			if (readed > 0) {
				baos.write(data, 0, readed);
				total += readed;
			}
//			System.out.println("readed total = " + total);
		}
		return new String(baos.toByteArray());
	}
	
	public String[] readUTFArray() throws IOException {
		int length = dis.readInt();
		if (length != 0) {
			String[] strs = new String[length];
			for (int i = 0; i < length; i++) {
				strs[i] = dis.readUTF().intern();
			}
			return strs;
		}
		return null;
	}
	
	public void readCollectionString(Collection<String> collectionStrings) throws IOException {
		int length = dis.readInt();
		for (int i = 0;i < length;i++) {
			String str = dis.readUTF().intern();
			collectionStrings.add(str);
		}
	}
	
	public <T extends BinarySerializable> T[] readBinaryObjectArray(Class<T> clazz) throws IOException {
		int length = dis.readInt();
		if(length != 0) {
			T[] ts = (T[]) Array.newInstance(clazz, length);
			for(int i = 0; i < length;i++) {
				try {
					ts[i] = (T) clazz.newInstance();
					ts[i].resurrect(dis);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			return ts;
		}
		return null;
	}
	
	public <T extends BinarySerializable> void readBinaryObjects(IteratorEx<T> iterator, Class<T> clazz) throws IOException {
		int length = dis.readInt();
		if(length != 0 && iterator != null) {
			for(int i = 0; i < length;i++) {
				try {
					T t = (T) clazz.newInstance();
					t.resurrect(dis);
					if(!iterator.iterate(t))
						break;
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}
	public <T extends BinarySerializable> void readCollectionBinaryObject(Collection<T> collectionAcuObjects, Class<T> clazz) throws IOException {
		int length = dis.readInt();
		if(length != 0) {
			for(int i = 0; i < length;i++) {
				try {
					T t = (T) clazz.newInstance();
					t.resurrect(dis);
					collectionAcuObjects.add(t);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public <T extends BinarySerializable> void readMapBinaryObject(Map<String, T> acuObjectMap, Class<T> clazz) throws IOException {
		int length = dis.readInt();
		if(length != 0) {
			for(int i = 0; i < length;i++) {
				try {
					String key = dis.readUTF();
					T t = (T) clazz.newInstance();
					t.resurrect(dis);
					acuObjectMap.put(key, t);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public <T extends BinarySerializable> T readBinaryObject(Class<T> clazz) throws IOException {
		if(hasValue()) {
			try {
				T object = clazz.newInstance();
				object.resurrect(dis);
				return object;
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

    public InputStream getInputStream() {
        return inputStream;
    }

    public DataInputStream getDataInputStream() {
    	return dis;
    }
}
