package chat.utils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

public class DataOutputStreamEx {
	private static final byte HASVALUE = 1;
	private static final byte NOVALUE = 0;
	private DataOutputStream dos;
	private OutputStream outputStream;
	
	public DataOutputStreamEx(OutputStream arg0) {
		dos = new DataOutputStream(arg0);
		outputStream = arg0;
	}
	
	public DataOutputStream original() {
		return dos;
	}
	
	private boolean writeValueStatus(Object value) throws IOException {
		if(value != null) {
			dos.writeByte(HASVALUE);
			return true;
		} else {
			dos.writeByte(NOVALUE);
			return false;
		}
	}
	
	public void write(Integer paramInt)
    throws IOException{
		if(writeValueStatus(paramInt))
			dos.write(paramInt);
	}

	public void writeCollectionString(Collection<String> collectionStrings) throws IOException {
		if(collectionStrings == null) {
			  dos.writeInt(NOVALUE);
		  } else {
			  String[] array = new String[collectionStrings.size()];
			  collectionStrings.toArray(array);
			  dos.writeInt(array.length);
			  for(String str : array) {
				  dos.writeUTF(str);
			  }
		  }
	}
	
//  public void write(byte[] paramArrayOfByte)
//    throws IOException{
//	  writeValueStatus(paramArrayOfByte);
//	  dos.write(paramArrayOfByte);
//  }
//
//  public void write(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
//    throws IOException{
//	  writeValueStatus(paramArrayOfByte);
//	  dos.write(paramArrayOfByte, paramInt1, paramInt2);
//  }

  public void writeBoolean(Boolean paramBoolean)
    throws IOException {
	  if(writeValueStatus(paramBoolean))
		  dos.writeBoolean(paramBoolean);
  }

  public void writeByte(Integer paramInt)
    throws IOException {
	  if(writeValueStatus(paramInt))
		  dos.writeByte(paramInt);
  }

  public void writeShort(Integer paramInt)
    throws IOException {
	  if(writeValueStatus(paramInt))
		  dos.writeShort(paramInt);
  }

  public void writeChar(Integer paramInt)
    throws IOException {
	  if(writeValueStatus(paramInt))
		  dos.writeChar(paramInt);
  }
  
  public void writeInt(Integer paramInt)
    throws IOException{
	  if(writeValueStatus(paramInt))
		  dos.writeInt(paramInt);
  }

  public void writeLong(Long paramLong)
    throws IOException {
	  if(writeValueStatus(paramLong))
		  dos.writeLong(paramLong);
  }

  public void writeFloat(Float paramFloat)
    throws IOException {
	  if(writeValueStatus(paramFloat))
		  dos.writeFloat(paramFloat);
  }

  public void writeDouble(Double paramDouble)
    throws IOException {
	  if(writeValueStatus(paramDouble))
		  dos.writeDouble(paramDouble);
  }

  public void writeBytes(String paramString)
    throws IOException {
	  if(writeValueStatus(paramString))
		  dos.writeBytes(paramString);
  }
  
  public void write(byte[] paramString)
		  throws IOException {
	  if(writeValueStatus(paramString))
		  dos.write(paramString);
  }

  public void writeChars(String paramString)
    throws IOException {
	  if(writeValueStatus(paramString))
		  dos.writeChars(paramString);
  }

  public void writeUTF(String paramString)
    throws IOException {
	  if(writeValueStatus(paramString))
		  dos.writeUTF(paramString);
  }
  
  public void writeDate(Date date, String format) throws IOException {
	  if(format == null) {
		  format = "yyyy-MM-dd";
	  }
	  if(writeValueStatus(date)){
		  DateFormat formatDate = new SimpleDateFormat(format);  
		  String str = formatDate.format(date);
		  dos.writeUTF(str);
	  }
  }
  
  public void writeUTFArray(String[] strs) throws IOException {
	  if(strs == null) {
		  dos.writeInt(NOVALUE);
		  return;
	  } else {
		  dos.writeInt(strs.length);
	  }
	  for(String str : strs) {
		  dos.writeUTF(str);
	  }
  }
  
  public void writeLongString(String str) throws IOException {
	  if(str == null) {
		  dos.writeInt(NOVALUE);
		  return;
	  } else {
		  dos.writeInt(str.getBytes().length);
	  }
	  dos.write(str.getBytes());
  }
  
  public <T extends BinarySerializable> void writeBinaryObjectArray(T[] array) throws IOException {
	  if(array == null) {
		  dos.writeInt(NOVALUE);
		  return;
	  } else {
		  dos.writeInt(array.length);
	  }
	  for(T t : array) {
		  t.persistent(dos);
	  }
  }
  
  public <T extends BinarySerializable> void writeCollectionBinaryObject(Collection<T> collectionAcuObjects) throws IOException {
		if(collectionAcuObjects == null) {
			  dos.writeInt(NOVALUE);
			  return;
		  } else {
			  dos.writeInt(collectionAcuObjects.size());
		  }
		for(T t : collectionAcuObjects) {
			  t.persistent(dos);
		  }
	}
  
  public <T extends BinarySerializable> void writeMapBinaryObject(Map<String, T> acuObjectMap) throws IOException {
	  if(acuObjectMap == null) {
		  dos.writeInt(NOVALUE);
		  return;
	  } else {
		  dos.writeInt(acuObjectMap.size());
	  }
	  for(String key : acuObjectMap.keySet()) {
		  BinarySerializable t = acuObjectMap.get(key);
		  dos.writeUTF(key);
		  t.persistent(dos);
	  }
  }
  
  public void writeBinaryObject(BinarySerializable object) throws IOException {
	  if(writeValueStatus(object))
		  object.persistent(dos);
  }
  
  public void close() throws IOException {
	  dos.close();
  }

  public OutputStream getOutputStream() {
      return outputStream;
  }

  public DataOutputStream getDataOutputStream() {
	  return dos;
  }
}
