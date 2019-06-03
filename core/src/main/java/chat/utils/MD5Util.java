package chat.utils;
/*    */ 
/*    */ import java.lang.ref.SoftReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.codec.binary.Base64;
/*    */ 
/*    */ public class MD5Util
/*    */ {
/* 13 */   private static final ConcurrentMap<byte[], String> cache = new ConcurrentHashMap<byte[], String>();
/*    */ 
/* 15 */   private static final LinkedList<SoftReference<String>> cachelist = new LinkedList<SoftReference<String>>();
/*    */   private static final int sum = 1000000;
/* 19 */   private static final Lock lock = new ReentrantLock();
/*    */ 
/*    */   private static final String md5s(byte[] src) {
/* 22 */     if (src == null)
/* 23 */       throw new IllegalArgumentException("src can not be  null!");
/* 24 */     MessageDigest md5 = null;
/*    */     try {
/* 26 */       md5 = MessageDigest.getInstance("MD5");
/*    */     } catch (NoSuchAlgorithmException e) {
/* 28 */       e.printStackTrace();
/*    */     }
/* 30 */     if (md5 == null)
/* 31 */       throw new RuntimeException("md5 can not be initialized!");
/* 32 */     md5.update(src);
/*    */ 
/* 34 */     byte[] array = md5.digest();
/* 35 */     StringBuilder sb = new StringBuilder();
/* 36 */     for (int j = 0; j < array.length; ++j) {
/* 37 */       int b = array[j] & 0xFF;
/* 38 */       if (b < 16)
/* 39 */         sb.append('0');
/* 40 */       sb.append(Integer.toHexString(b));
/*    */     }
/* 42 */     return sb.toString();
/*    */   }
/*    */ 
/*    */   public static final String md5(byte[] src) {
/* 46 */     String s = (String)cache.get(src);
/* 47 */     if (s == null) {
/* 48 */       s = md5s(src);
/* 49 */       lock.lock();
/*    */       try {
/* 51 */         if (cachelist.size() >= 1000000)
/* 52 */           cache.remove(cachelist.pop());
/*    */         else
/* 54 */           cachelist.addLast(new SoftReference(src));
/*    */       }
/*    */       finally {
/* 57 */         lock.unlock();
/*    */       }
/* 59 */       cache.put(src, s);
/*    */     }
/* 61 */     return s;
/*    */   }
/*    */ 
/*    */   public static final void clear() {
/* 65 */     cache.clear();
/* 66 */     cachelist.clear();
/*    */   }
/*    */ 
/*    */  
/*    */ }

/* Location:           /Volumes/Data/Aplomb/J2EEServer/workspaces/ttserviceV4.0/war/WEB-INF/lib/tt_uc_V4.0.jar
 * Qualified Name:     com.hisun.tt.uc.util.MD5Util
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.5.3
 */